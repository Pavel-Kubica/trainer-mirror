<script setup>
import { inject, onMounted, provide } from 'vue'
import { useLocale } from 'vuetify'
import { onBeforeRouteLeave } from 'vue-router'

import * as Code from '@/plugins/code'
import {TAB_MODULE_DETAIL, TAB_MODULE_RATING, TEST_COMPILATION_ID} from '@/plugins/constants'
import { useUserStore } from '@/plugins/store'

import CodeEditor from '@/components/custom/CodeEditor.vue'
import CodeModuleBottomOverlay from '@/components/modules/code/CodeModuleBottomOverlay.vue'
import CodeModuleRightDrawer from '@/components/modules/code/CodeModuleRightDrawer.vue'

defineProps(['teacher'])

const tabList = inject('tabList')
const selectedTab = inject('selectedTab')
const userStore = useUserStore()
const { t } = useLocale()

const runtime = inject('runtime')
const moduleData = inject('module')
const codeData = inject('codeData')
const saveResults = inject('saveResults')
provide('codeData',codeData)

const resetTests = () => {
  runtime.value.running = true
  runtime.value.openedTests = []
  runtime.value.workerHosts = []
  for (let test of runtime.value.tests) {
    test.total = 0
    test.inputText = test.outputText = test.refText = test.errorText = test.error = test.diffHtml = ""
  }
}

const testFailed = (test, error, wasmResult = null, idx = TEST_COMPILATION_ID) => {
  test.error = error

  if (error === t('$vuetify.code_module.error_invalid_output')) {
    if (!test.hidden) {
      test.inputText = wasmResult.input.replace(/\n/g, '↵\n')
      test.outputText = wasmResult.output.replace(/\n/g, '↵\n')
      test.refText = wasmResult.ref.replace(/\n/g, '↵\n')
      test.diffHtml = test.outputText !== test.refText
    }
  }
  else {
    test.passed = 0
    test.total = 1
  }

  runtime.value.openedTests.push(idx)
  if (idx === TEST_COMPILATION_ID)
    runtime.value.running = false
}

const compile = async () => {
  let compilationTest = runtime.value.tests.find((test) => test.id === TEST_COMPILATION_ID)

  for (let file of codeData.value.files) {
    if (file.content.length > file.codeLimit) {
      testFailed(compilationTest, t('$vuetify.code_module.error_code_too_large',
          file.content.length, file.codeLimit))
      return null
    }
  }

  let startTime = new Date().getTime()
  let output, result

  try {
    [output, result] = await Code.compile(codeData.value)
  }
  catch (error) {
    output = error.message
    result = null
  }

  let endTime = new Date().getTime()

  console.log(`Compilation took: ${endTime - startTime} ms`)
  console.log({ result: result ?? false, output })

  compilationTest.errorText = output
  if (!result) {
    testFailed(compilationTest, t('$vuetify.code_module.error_compiling'))
    return null
  }
  return result
}
const link = async (compileResult) => {
  let compilationTest = runtime.value.tests.find((test) => test.id === TEST_COMPILATION_ID)
  let [stdout, stderr, result] = await Code.link(compileResult, codeData.value)
  console.log({ result: result ?? false, stdout, stderr })

  if (!result) {
    compilationTest.errorText = stderr
    testFailed(compilationTest, t('$vuetify.code_module.error_linking'))
    return false
  }
  compilationTest.passed = 1
  compilationTest.total = 1
  runtime.value.wasmFile = result
  return true
}

/**
* @param {{exitCode: number, allocCalls: number, freeCalls: number,
            testsPassed: number, testsTotal: number, input: string, output: string,
            ref: string, fs: any}} wasmResult
*/
const checkTestOutput = (test, wasmResult, idx, programTimedOut) => {
  test.total = wasmResult?.testsTotal ?? 1
    if(programTimedOut == true){
        return t("$vuetify.code_module.error_program_timeout", Code.posixSignals[(wasmResult?.exitCode ?? 144) - 128])
    }
  if (!wasmResult || (wasmResult.exitCode > 128 && wasmResult.exitCode !== 134))
    return t('$vuetify.code_module.error_ret_code_not_ok', Code.posixSignals[(wasmResult?.exitCode ?? 144) - 128])

  if (codeData.value.codeType === 'WRITE_ASSERT') {
    if (test.shouldFail && wasmResult.exitCode !== 134)
      return t('$vuetify.code_module.error_should_fail_didnt_fail')
    if (!test.shouldFail && wasmResult.exitCode === 134)
      return t('$vuetify.code_module.error_shouldnt_fail_did_fail')

    test.total = test.passed = 1
    return null // ok
  }
  test.passed = wasmResult.testsPassed

  if (wasmResult.testsPassed !== wasmResult.testsTotal)
    return t('$vuetify.code_module.error_invalid_output')

  if (wasmResult.exitCode === 134) {
    test.total = test.passed + 1
    return t('$vuetify.code_module.error_ret_code_not_ok', Code.posixSignals[6])
  }

  if (test.checkMemory && wasmResult.freeCalls !== wasmResult.allocCalls)
    return t('$vuetify.code_module.error_memory_not_freed', wasmResult.freeCalls, wasmResult.allocCalls)

  if (codeData.value.codeType === 'SHOWCASE') {
    test.inputText = wasmResult.input.replace(/\n/g, '↵\n')
    test.diffHtml = `<pre>${wasmResult.output.replace(/\n/g, '↵\n')}</pre>`
    runtime.value.openedTests.push(idx)
  }
  return null // success
}

const finishedTests = async (passed, total) => {
  runtime.value.running = false
  const progress = 100.0 * passed / total
  moduleData.value.progress = Math.max(moduleData.value.progress, progress)
  if (!moduleData.value.manualEval && moduleData.value.progress >= moduleData.value.minPercent)
    moduleData.value.completed = new Date().toUTCString()
  saveResults(progress)
}

const run = async () => {
  if (runtime.value.running) return
  resetTests() // Reset
  let compileResult
  try { compileResult = await compile() }
  catch (exc) {
    console.log(exc)
    compileResult = null
  }
  if (!compileResult || !await link(compileResult)) { // Compilation, Linking
    await finishedTests(0, 1)
    return
  }

  // Run individual tests
  let total = runtime.value.tests.length - 1, ran = 0, passed = 0
  for (const idx in runtime.value.tests) {
    let test = runtime.value.tests[idx]
    if (test.id === TEST_COMPILATION_ID) continue

    /**
    * @param {{exitCode: number, allocCalls: number, freeCalls: number,
    testsPassed: number, testsTotal: number, input: string, output: string, ref: string, fs: any}} result
    */
    const checkResult = (result, programTimedOut) => {
      const testError = checkTestOutput(test, result, idx, programTimedOut)
      if (testError) testFailed(test, testError, result, idx)
      else passed += 1
      ran += 1
      if (ran === total)
        finishedTests(passed, total)
    }
    const timeLimit = parseInt(test.timeLimit)
    const workerHost = await Code.run(runtime.value.wasmFile, (o) => { console.log(o) }, checkResult,
        [test.parameter],timeLimit)
    workerHost.pushEOF().then(() => {}) // ensure program doesn't freeze
    runtime.value.workerHosts.push(workerHost)
    setTimeout(() => {console.log('manually killing workerHost due to timeout'); workerHost.kill()},timeLimit ? timeLimit*1000 + 1000 : 30000)
  }
  if (total === 0)
    await finishedTests(0, 1)
}
provide('run', run)

// Handle Ctrl+R
const compileKeyPress = (event) => {
  if ((!event.ctrlKey && !event.metaKey) || event.key !== "r") // ignore
    return
  event.preventDefault()
  event.stopPropagation()
  run()
  return false
}

const isRefTabShown = () => !userStore.anonymous && userStore.isTeacher(moduleData.value.lesson.week.course)

onBeforeRouteLeave(() => { document.removeEventListener('keydown', compileKeyPress) })
onMounted(() => {
  if (!userStore.isTeacher(moduleData.value.lesson.week.course) && codeData.value.interactionType !== 'EDITOR' ) return // don't show
  for (let ix in codeData.value.files)
    tabList.value[TAB_MODULE_DETAIL + ix] = codeData.value.files[ix].name
  if (moduleData.value.teacher)
    selectedTab.value = TAB_MODULE_DETAIL + "0"
  if (userStore.isTeacher(moduleData.value.lesson.week.course) && !userStore.anonymous)
    tabList.value[TAB_MODULE_DETAIL + codeData.value.files.length] = t('$vuetify.code_module.tab_teacher')
  if (userStore.isTeacher(moduleData.value.lesson.week.course))
    tabList.value[TAB_MODULE_RATING] = t('$vuetify.tab_rating')
  document.addEventListener('keydown', compileKeyPress)
})
</script>

<template v-if="moduleData.teacher || codeData.interactionType === 'EDITOR'">
  <v-window-item v-for="fileId in Object.keys(codeData.files)" :key="TAB_MODULE_DETAIL + fileId"
                 :transition="false" :reverse-transition="false"
                 :value="TAB_MODULE_DETAIL + fileId"
                 :class="moduleData.teacher ? 'full-cm-editor' : ''">
    <v-card flat class="my-2">
      <CodeEditor :file-id="fileId" file-key="content" :disabled="codeData.codeType === 'SHOWCASE'" />
      <CodeModuleBottomOverlay v-if="selectedTab >= TAB_MODULE_DETAIL" class="pt-2" :file-id="fileId" />
    </v-card>
  </v-window-item>
  <v-window-item v-if="isRefTabShown() && !userStore.anonymous" :key="TAB_MODULE_DETAIL + codeData.files.length" :transition="false" :reverse-transition="false"
                 class="full-cm-editor" :value="TAB_MODULE_DETAIL + codeData.files.length">
    <v-card flat class="my-2">
      <CodeEditor code-key="codeHidden" :disabled="true" />
    </v-card>
  </v-window-item>
  <CodeModuleBottomOverlay v-if="selectedTab < TAB_MODULE_DETAIL" :file-id="codeData.files.length === 1 ? 0 : undefined" />
  <CodeModuleRightDrawer />
</template>
