<script setup>
import {onMounted, provide, inject, ref, watch} from 'vue'
import { useLocale } from 'vuetify'
import { onBeforeRouteLeave } from 'vue-router'
import { studentModuleApi } from '@/service/api'
import codeApi from '@/service/codeApi'
import { TarReader, TarWriter } from '@gera2ld/tarjs'
import {TEST_COMPILATION} from '@/plugins/constants'

import LoadingScreen from '@/components/custom/LoadingScreen.vue'
import CodeModuleErrorTab from '@/components/modules/code/CodeModuleErrorTab.vue'
import CodeModuleBase from '@/components/modules/code/CodeModuleBase.vue'
import {useUserStore} from "@/plugins/store";

const props = defineProps(['teacher'])
const lesson = inject('lesson')
const moduleData = inject('module')
const requestAnswerCallbacks = inject('requestAnswerCallbacks')
const userStore = useUserStore()
const { t } = useLocale()

const userAuthor = ref(null)
const codeData = ref(null)
const runtime = ref({running: false, wasmFile: '', workerHosts: [], tests: [], openedTests: []})
const error = ref(null)
provide('codeData', codeData)
provide('runtime', runtime)

const reloadHook = inject('reloadHook')
const saveResults = async (percent = null) => {
  if (props.teacher || codeData.value.codeModuleIdCheck !== moduleData.value.id) // switched tabs, teacher
    return
  if (codeData.value.codeHidden !== codeData.value.defaultCodeHidden)
    await codeApi.patchCode(moduleData.value.id, {codeHidden: codeData.value.codeHidden})
  await saveStudentModuleFile().catch((err) => console.log(err.code)) // save code & tests
  if (percent) { // save percent
    await studentModuleApi.putStudentModule(lesson.value.id, moduleData.value.id, {percent: percent})
    reloadHook.value = new Date().getTime() // update left menu
  }
}
provide('saveResults', saveResults)

const strip = (text, len) => text?.substring(0, len ?? 1024)

const loadStudentModuleFile = async () => {
  if (!moduleData.value.hasStudentFile) return [] // no file
  const fileTar = props.teacher ? await studentModuleApi.getStudentModuleFileTeacher(lesson.value.id, moduleData.value.id, props.teacher)
      : await studentModuleApi.getStudentModuleFile(lesson.value.id,  moduleData.value.id)
  const reader = new TarReader()
  userAuthor.value = userStore.user.username
  let files = {};
  (await reader.readFile(fileTar)).forEach((file) => files[file.name] = reader.getTextFile(file.name))
  return files
}
const saveStudentModuleFile = async () => {
  if (props.teacher || codeData.value.codeModuleIdCheck !== moduleData.value.id) return // switched tabs, teacher
  const writer = new TarWriter()
  for (const file of codeData.value.files)
    writer.addFile(file.name, file.content.substring(0, file.codeLimit))
  for (const test of runtime.value.tests)
    writer.addFile(`test${test.id}.json`, JSON.stringify({
      passed: test.passed, total: test.total, error: test.error,
      errorText: strip(test.errorText ?? ''), inputText: strip(test.inputText ?? ''),
      outputText: strip(test.outputText ?? ''), refText: strip(test.refText ?? ''),
      diffHtml: test.diffHtml !== true ? strip(test.diffHtml, 2048) : test.diffHtml
    }))
  const blob = await writer.write()
  userAuthor.value = userStore.user.username
  await studentModuleApi.putStudentModuleFile(lesson.value.id, moduleData.value.id, blob)
      .catch((err) => error.value = err)
}

const sendCodeComments = async () => {
  if (!codeData.value.creatingComments || !codeData.value.newComments || !moduleData.value.studentRequest?.requestId) return
  await postCodeComments()
  loadCodeComments()
}

const postCodeComments = async () => {
  for (const [fileName, comments] of Object.entries(codeData.value.newComments)) {
    for (const [rowNumber, comment] of Object.entries(comments)) {
      studentModuleApi.postStudentModuleRequestCodeComment(moduleData.value.studentRequest.requestId, {
        fileName: fileName, rowNumber: rowNumber, comment: comment
      }).catch((err) => error.value = err.code)
    }
  }
}

requestAnswerCallbacks.value.push(sendCodeComments)
const loadCodeComments = async () => {
  if (!moduleData?.value?.studentRequest?.requestId) return
  await studentModuleApi.getStudentModuleRequestCodeComments(moduleData.value.studentRequest.requestId)
      .then((comments) => codeData.value.comments = comments)
      .catch((err) => { error.value = err.code })
}
watch(() => moduleData?.value?.studentRequest?.requestId, async () => await loadCodeComments())
watch(() => moduleData?.value?.studentRequest?.teacherComment, async () => await loadCodeComments())
watch(() => moduleData?.value?.studentRequest?.codeCommentsHook, async () => await loadCodeComments())

const loadModule = async () => {
  moduleData.value.lesson = lesson.value
  moduleData.value.teacher = props.teacher

  codeData.value = null
  await Promise.all([loadStudentModuleFile(), codeApi.codeDetail(moduleData.value.id)])
      .then((results) => {
        let [moduleFiles, codeDataRaw] = results
        runtime.value.tests = [TEST_COMPILATION(t)].concat(codeDataRaw.tests)
        for (let test of runtime.value.tests) {
          const testFile = JSON.parse(moduleFiles[`test${test.id}.json`] ?? '{}')
          test.passed = testFile.passed ?? 0; test.total = testFile.total ?? 0
          test.error = testFile.error ?? ''; test.errorText = testFile.errorText
          test.inputText = testFile.inputText; test.outputText = testFile.outputText
          test.refText = testFile.refText; test.diffHtml = testFile.diffHtml
        }

        codeDataRaw.codeModuleIdCheck = moduleData.value.id
        codeDataRaw.defaultCode = codeDataRaw.codeShown
        codeDataRaw.defaultCodeHidden = codeDataRaw.codeHidden
        codeDataRaw.originalFiles = codeDataRaw.files.map((f) => Object.assign({}, f))
        for (let file of codeDataRaw.files) // saved code
          if (moduleFiles[file.name] && codeDataRaw.codeType !== 'SHOWCASE')
            file.content = moduleFiles[file.name]
        codeData.value = codeDataRaw

        codeData.value.creatingComments = Boolean(moduleData.value.teacher)
        codeData.value.comments = []
        codeData.value.newComments = {}
      })
      .catch((err) => { error.value = err.code })

  if (moduleData.value.studentRequest?.teacherComment) {
    await loadCodeComments()
  }
}
onMounted(async () => await loadModule())

// === HANDLE WEBPAGE CLOSE (START)

const filesChanged = () => {
  for (const index in codeData.value.originalFiles)
    if (codeData.value.originalFiles[index].content !== codeData.value.files[index].content)
      return true
  return false
}

const backupAndKill = async () => {
  if (codeData.value && filesChanged() && userAuthor.value=== userStore.user.username) // only if code changed
    await saveStudentModuleFile() // save before leave
  if (runtime.value.workerHosts.length)
    for (let workerHost of runtime.value.workerHosts)
      await workerHost.kill()
}

// Handle Ctrl+S
const saveKeyPress = async (event) => {
  if ((!event.ctrlKey && !event.metaKey) || event.key !== "s") // ignore
    return
  event.preventDefault()
  event.stopPropagation()

  // save code (if changed)
  if (filesChanged())
    await saveStudentModuleFile()
  return false
}

onBeforeRouteLeave(async () => {
  window.removeEventListener('beforeunload', backupAndKill)
  document.removeEventListener('keydown', saveKeyPress)
  await backupAndKill()
})
onMounted(() => {
  window.addEventListener('beforeunload', backupAndKill)
  document.addEventListener('keydown', saveKeyPress)
})
// === HANDLE WEBPAGE CLOSE (END)
</script>

<template>
  <LoadingScreen :items="codeData" :error="error">
    <template #content>
      <CodeModuleBase />
    </template>
    <template #error>
      <CodeModuleErrorTab :error="error" />
    </template>
  </LoadingScreen>
</template>
