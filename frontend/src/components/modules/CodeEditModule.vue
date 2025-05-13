<script setup>
import {ref, onMounted, inject, provide, watch, computed} from 'vue'
import { onBeforeRouteLeave } from 'vue-router'
import { useLocale } from 'vuetify'
import codeApi from '@/service/codeApi'
import CodeEditor from '@/components/custom/CodeEditor.vue'
import {
  MODULE_EDIT_ITEMS, MODULE_EDIT_CODE_INFO, MODULE_EDIT_CODE_STUD,
  MODULE_EDIT_CODE_TEAC, MODULE_EDIT_CODE_ENV,
  MODULE_EDIT_CODE_TEST, DEFAULT_CODE_FILENAME_C, DEFAULT_CODE_CONTENT_C
} from "@/plugins/constants";
import LoadingScreen from "@/components/custom/LoadingScreen.vue";
import CodeEditModulePartInformation from '@/components/modules/code/edit/CodeEditModulePartInformation.vue'
import CodeEditModulePartTest from '@/components/modules/code/edit/CodeEditModulePartTest.vue'
import CodeEditModulePartFile from '@/components/modules/code/edit/CodeEditModulePartFile.vue'
import CodeEditModulePartEnvelope from "@/components/modules/code/edit/CodeEditModulePartEnvelope.vue";
import DeleteDialog from "@/components/custom/DeleteDialog.vue";
import * as Nav from "@/service/nav";
import {TEACHER_GUIDE_ID} from "@/resources/guides";
const appState = inject('appState')
const { t } = useLocale()
const props = defineProps(['moduleId', 'readOnly'])

const deleteDialog = ref(false)
const testToDelete = ref(null)
const fileToDelete = ref(null)
const doDelete = ref(() => {
  if (testToDelete.value)
    deleteTest(testToDelete.value)
  else if (fileToDelete.value)
    deleteFile(fileToDelete.value)
  deleteDialog.value = false;
})
provide('testToDelete', testToDelete)
provide('fileToDelete', fileToDelete)
provide('deleteDialog', deleteDialog)

const deleteDialogTexts = computed(() => {
  if (testToDelete.value)
    return {
      itemName: testToDelete.value.name,
      title: '$vuetify.code_module.delete_test',
      start: '$vuetify.code_module.delete_test_dialog_start',
      middle: '$vuetify.code_module.delete_test_dialog_middle',
      end: '$vuetify.irreversible_action',
    }
  else if (fileToDelete.value)
    return {
      itemName: fileToDelete.value.name,
      title: "$vuetify.code_module.delete_file",
      start: '$vuetify.code_module.delete_file_dialog_start',
      middle: '$vuetify.code_module.delete_file_dialog_middle',
      end: '$vuetify.irreversible_action',
    }
  else
    return {
      itemName: '',
      title: '',
      start: '',
      middle: '',
      end: '',
    }
})

const reloadTestNames = () => {
  codeData.value.tests[codeData.value.tests.length - 1].name = t('$vuetify.code_module.new_test')
  moduleEditTabList.value[MODULE_EDIT_CODE_TEST].tabList = codeData.value.tests.map(
      (test, ix) => ({id: ix + 100, name: test.name})
  )
  codeData.value.tests[codeData.value.tests.length - 1].name = ""
}
const reloadFileNames = () => {
  codeData.value.files[codeData.value.files.length - 1].name = t('$vuetify.code_module.new_file')
  moduleEditTabList.value[MODULE_EDIT_CODE_STUD].tabList = codeData.value.files.map(
      (file, ix) => ({id: ix + 1000, name: file.name})
  )
  codeData.value.files[codeData.value.files.length - 1].name = ""
}

const reloadModuleTestTab = () => {
  codeData.value.tests.push({name: ""})
  reloadTestNames()
}
provide('reloadModuleTestTab', reloadModuleTestTab)
const reloadModuleFileTab = () => {
  if (codeData.value.files[codeData.value.files.length - 1]?.name !== "") {
    codeData.value.files.push({
      name: "",
      codeLimit: 1024,
      content: DEFAULT_CODE_CONTENT_C,
      reference: '',
      headerFile: false
    })
    reloadFileNames()
  }
}
provide('reloadModuleFileTab', reloadModuleFileTab)
const deleteTest = async (test, automatic = false) => {
  if (!test.id || test.tmp) { // delete only local
    codeData.value.tests.splice(codeData.value.tests.indexOf(test), 1)
    codeData.value.tests.splice(codeData.value.tests.length - 1, 1) // remove "createTest"
    reloadModuleTestTab()
    return
  }

  // delete from api
  return codeApi.deleteTest(test.id)
      .then(() => {
        if (!automatic) {
          reload()
          appState.value.notifications.push({
            type: "success", title: t(`$vuetify.code_module.delete_test_title`),
            text: t(`$vuetify.code_module.delete_test_text`),
          })
        }
      })
}
const deleteFile = async (file, automatic = false) => {
  if (!file.id || file.tmp) { // delete only local
    codeData.value.files.splice(codeData.value.files.indexOf(file), 1)
    codeData.value.files.splice(codeData.value.files.length - 1, 1) // remove "createFile"
    reloadModuleFileTab()
    return
  }


  // delete from api
  return codeApi.deleteFile(file.id)
      .then(() => {
        if (!automatic) {
          reload()
          appState.value.notifications.push({
            type: "success", title: t(`$vuetify.code_module.delete_file_title`),
            text: t(`$vuetify.code_module.delete_file_text`),
          })
        }
      })
      .catch((err) => { error.value = err.code })
}

provide('deleteTest', deleteTest);
provide('deleteFile', deleteFile);

const addTest = (test) => {
  if (!test.parameter || !test.name || !test.description) return
  test.id = codeData.value.tests.length
  test.tmp = true
  reloadModuleTestTab()
}
const addFile = (file) => {
  file.id = codeData.value.files.length
  file.tmp = true
  reloadModuleFileTab()
}

const codeData = ref(null)
const originalCodeData = ref(null)
const error = ref(null)
provide('codeData', codeData)

const routerNext = inject('routerNext')
const unsavedChangesDialog = inject('unsavedChangesDialog')

const moduleEditItem = inject('moduleEditItem')
const moduleEditTabList = inject('moduleEditTabList')
const createEditCallback  = inject('createEditCallback', () => {})

createEditCallback.value = (module) => {
  // Arguable UX, if user was creating a test and didn't add it, we do it automatically
  addTest(codeData.value.tests[codeData.value.tests.length - 1]) // it's fine if the test is not addable
  codeData.value.tests.splice(codeData.value.tests.length - 1, 1) // remove "createTest"
  codeData.value.files.splice(codeData.value.files.length - 1, 1) // remove "createFile"
  const promise = props.moduleId ? codeApi.patchCode(props.moduleId, codeData.value) :
    codeApi.postCode(module.id, codeData.value)
  reloadModuleTestTab()
  reloadModuleFileTab()
  return promise
}

// tab switch
watch(moduleEditItem, (now, old) => {
  if (old.id >= 100 && old.id < 1000)
    reloadTestNames()
  if (old.id >= 1000)
    reloadFileNames()
})

const reload = async () => {
  moduleEditTabList.value[MODULE_EDIT_CODE_INFO] = {...MODULE_EDIT_ITEMS[MODULE_EDIT_CODE_INFO], guideLink: new Nav.GuideMarkdown(TEACHER_GUIDE_ID, {id: 3}).routerPath()}
  moduleEditTabList.value[MODULE_EDIT_CODE_TEST] = MODULE_EDIT_ITEMS[MODULE_EDIT_CODE_TEST]
  moduleEditTabList.value[MODULE_EDIT_CODE_STUD] = MODULE_EDIT_ITEMS[MODULE_EDIT_CODE_STUD]
  moduleEditTabList.value[MODULE_EDIT_CODE_ENV] = MODULE_EDIT_ITEMS[MODULE_EDIT_CODE_ENV]
  moduleEditTabList.value[MODULE_EDIT_CODE_TEAC] = MODULE_EDIT_ITEMS[MODULE_EDIT_CODE_TEAC]
  if (props.moduleId) {
    await codeApi.codeDetail(props.moduleId)
        .then((response) => {
          codeData.value = response
          reloadModuleTestTab()
          reloadModuleFileTab()
          originalCodeData.value = JSON.parse(JSON.stringify(codeData.value))
        })
        .catch((err) => {
          error.value = err
        })
  } else {
    originalCodeData.value = null
    codeData.value = {
      codeType: 'SHOWCASE',
      interactionType: 'EDITOR',
      codeHidden: '',
      fileLimit: 10240,
      hideCompilerOutput: false,
      referencePublic: false,
      envelopeType: 'ENV_C',
      customEnvelope: 'namespace __STUDENT_NAMESPACE__ {\n\t__STUDENT_FILE__\n}',
      libraryType: 'LIB_C',
      assignment: '',
      tests: [],
      files: [{
        name: DEFAULT_CODE_FILENAME_C,
        tmp: true,
        id: -1,
        codeLimit: 1024,
        content: DEFAULT_CODE_CONTENT_C,
        reference: '',
        headerFile: false
      }],
    }
    reloadModuleTestTab()
    reloadModuleFileTab()
  }
}

onMounted(async () => {
  onBeforeRouteLeave((to, from, next) => {
    const newCodeDataComparable = Object.fromEntries(Object.entries(codeData.value).filter(([key]) => key !== 'templateEnvelope'))
    if (props.readOnly || JSON.stringify(originalCodeData.value) === JSON.stringify(newCodeDataComparable)) { // ok
      next()
      return
    }
    routerNext.value = next
    unsavedChangesDialog.value = true
  })
  await reload()
})
</script>

<template>
  <DeleteDialog :item-name="deleteDialogTexts.itemName"
                :title="deleteDialogTexts.title"
                :text-start="deleteDialogTexts.start" :text-before-line-break="deleteDialogTexts.middle" :text-second-line="deleteDialogTexts.end"
                :on-cancel="() => deleteDialog = false"
                :on-confirm="doDelete"
                :text-confirm-button="'$vuetify.action_delete'" />
  <LoadingScreen :items="codeData" :error="error">
    <template #items>
      <CodeEditModulePartInformation v-if="moduleEditItem.id === MODULE_EDIT_CODE_INFO" :read-only="props.readOnly" :module-id="moduleId" :add-file="addFile" />
      <CodeEditModulePartTest v-else-if="moduleEditItem.id >= 100 && moduleEditItem.id < 1000" :test-id="moduleEditItem.id - 100"
                              :add-test="addTest" :read-only="props.readOnly" />
      <CodeEditModulePartFile v-else-if="moduleEditItem.id >= 1000" :file-id="moduleEditItem.id - 1000"
                              :add-file="addFile" :read-only="props.readOnly" />
      <CodeEditModulePartEnvelope v-if="moduleEditItem.id === MODULE_EDIT_CODE_ENV" :read-only="props.readOnly" />
      <CodeEditor v-if="moduleEditItem.id === MODULE_EDIT_CODE_TEAC" class="mb-4" code-key="codeHidden" :disabled="readOnly" />
    </template>
  </LoadingScreen>
</template>
