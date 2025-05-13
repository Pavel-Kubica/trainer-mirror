<script setup>
import {ref, onMounted, inject, provide, watch} from 'vue'
import {onBeforeRouteLeave} from 'vue-router'
import {useLocale} from 'vuetify'
import codeApi from '@/service/codeApi'
import CodeEditor from '@/components/custom/CodeEditor.vue'
import {
  MODULE_EDIT_ITEMS, MODULE_EDIT_CODE_INFO,
  MODULE_EDIT_CODE_TEAC, MODULE_EDIT_CODE_ENV,
  MODULE_EDIT_CODE_TEST, DEFAULT_CODE_CONTENT, MODULE_FILE_CODE
} from "@/plugins/constants";
import LoadingScreen from "@/components/custom/LoadingScreen.vue";
import CodeEditModulePartInformation from '@/components/modules/code/edit/CodeEditModulePartInformation.vue'
import CodeEditModulePartTest from '@/components/modules/code/edit/CodeEditModulePartTest.vue'
import CodeEditModulePartEnvelope from "@/components/modules/code/edit/CodeEditModulePartEnvelope.vue";
import {templateApi} from "@/service/api";

const appState = inject('appState')
const {t} = useLocale()

const reloadTestNames = () => {
  codeData.value.tests[codeData.value.tests.length - 1].name = t('$vuetify.code_module.new_test')
  moduleEditTabList.value[MODULE_EDIT_CODE_TEST].tabList = codeData.value.tests.map(
      (test, ix) => ({id: ix + 100, name: test.name})
  )
  codeData.value.tests[codeData.value.tests.length - 1].name = ""
}

const reloadModuleTestTab = () => {
  codeData.value.tests.push({name: ""})
  reloadTestNames()
}
const deleteTest = (test) => {
  if (test.tmp) { // delete only local
    codeData.value.tests.splice(codeData.value.tests.indexOf(test), 1)
    codeData.value.tests.splice(codeData.value.tests.length - 1, 1) // remove "createTest"
    reloadModuleTestTab()
    return
  }

  // delete from api
  codeApi.deleteTest(test.id)
      .then(() => {
        reload()
        appState.value.notifications.push({
          type: "success", title: t(`$vuetify.code_module.delete_test_title`),
          text: t(`$vuetify.code_module.delete_test_text`),
        })
      })
      .catch((err) => {
        error.value = err.code
      })
}
const addTest = (test) => {
  test.id = codeData.value.tests.length
  test.tmp = true
  reloadModuleTestTab()
}

const codeData = ref(null)
const originalCodeData = ref(null)
const error = ref(null)
provide('codeData', codeData)

const moduleEditItem = inject('moduleEditItem')
const moduleEditTabList = inject('moduleEditTabList')
const createEditCallback = inject('createEditCallback', () => {
})

createEditCallback.value = (module) => {
  codeData.value.tests.splice(codeData.value.tests.length - 1, 1) // remove "createTest"
  const template = {
    name: module.name, codeType: codeData.value.codeType, author: module.author.id, libraryType: codeData.value.libraryType,
    interactionType: codeData.value.interactionType, envelopeType: codeData.value.envelopeType, customEnvelope: codeData.value.customEnvelope,
    codeHidden: codeData.value.codeHidden, fileLimit: codeData.value.fileLimit, tests: codeData.value.tests
  }
  console.log(module)
  console.log(codeData.value)
  templateApi.createTemplate(template)
}

// tab switch
watch(moduleEditItem, (now, old) => {
  if (old.id >= 100 && old.id < 1000)
    reloadTestNames()
})

const reload = async () => {
  moduleEditTabList.value[MODULE_EDIT_CODE_INFO] = MODULE_EDIT_ITEMS[MODULE_EDIT_CODE_INFO]
  moduleEditTabList.value[MODULE_EDIT_CODE_TEST] = MODULE_EDIT_ITEMS[MODULE_EDIT_CODE_TEST]
  moduleEditTabList.value[MODULE_EDIT_CODE_ENV] = MODULE_EDIT_ITEMS[MODULE_EDIT_CODE_ENV]
  moduleEditTabList.value[MODULE_EDIT_CODE_TEAC] = MODULE_EDIT_ITEMS[MODULE_EDIT_CODE_TEAC]
  originalCodeData.value = null
  codeData.value = {
    codeType: 'TEST_ASSERT',
    interactionType: 'EDITOR',
    codeHidden: '',
    fileLimit: 10240,
    referencePublic: false,
    envelopeType: 'ENV_C',
    customEnvelope: 'namespace __STUDENT_NAMESPACE__ {\n\t__STUDENT_FILE__\n}',
    libraryType: 'LIB_C',
    tests: [],
    files: [{
      name: MODULE_FILE_CODE,
      tmp: true,
      id: -1,
      codeLimit: 1024,
      content: DEFAULT_CODE_CONTENT,
      reference: '',
      headerFile: ''
    }],
  }
  reloadModuleTestTab()
}
onMounted(async () => {
  onBeforeRouteLeave((to, from, next) => {
    next()
  })
  await reload()
})
</script>

<template>
  <LoadingScreen :items="codeData" :error="error">
    <template #items>
      <CodeEditModulePartInformation v-if="moduleEditItem.id === MODULE_EDIT_CODE_INFO" />
      <CodeEditModulePartTest v-else-if="moduleEditItem.id >= 100 && moduleEditItem.id < 1000"
                              :test-id="moduleEditItem.id - 100"
                              :add-test="addTest" :delete-test="deleteTest" />
      <CodeEditModulePartEnvelope v-if="moduleEditItem.id === MODULE_EDIT_CODE_ENV" />
      <CodeEditor v-if="moduleEditItem.id === MODULE_EDIT_CODE_TEAC" class="mb-4" code-key="codeHidden" />
    </template>
  </LoadingScreen>
</template>
