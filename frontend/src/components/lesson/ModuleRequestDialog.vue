<script setup>
import {inject, ref, watch} from 'vue'
import {useLocale} from 'vuetify'
import {studentModuleApi} from '@/service/api'
import {moduleRequestIsHelp} from '@/plugins/constants'

const appState = inject('appState')
const reloadHook = inject('reloadHook')
const { t } = useLocale()

const lesson = inject('lesson')
const module = inject('module')
const requestDialog = inject('requestDialog')
const requestAnswerCallbacks = inject('requestAnswerCallbacks')

const requestText = ref("")
const evalToggle = ref(false)
const evalPercent = ref(0)
const loading = ref(false)

const requestType = () => moduleRequestIsHelp(module.value) ? 'HELP' : 'EVALUATE'
const tkey = () => {
  const type = moduleRequestIsHelp(module.value) ? 'help' : 'eval'
  if (!module.value.teacher)
    return type
  if (!module.value.studentRequest)
    return 'teacher_comment'
  return `teacher_${type}`
}
const translate = (key, param = undefined) => {
  return t(`$vuetify.request_${tkey()}.${key}`, param)
}

const loadStudentRequest = async () => {
  await studentModuleApi.getStudentModuleRequest(lesson.value.id, module.value.id)
      .then((response) => {
        module.value.studentRequest = response
        reloadHook.value = new Date().getTime()
      })
}

const submitRequest = () => {
  if (!requestText.value.length) // must have text
    return
  loading.value = true

  if (module.value.teacher) {
    submitRequestTeacher()
    return
  }

  studentModuleApi.putStudentModuleRequest(lesson.value.id, module.value.id, {
    requestText: requestText.value,
    requestType: requestType()
  }).then(() => {
    // Update the data
    loadStudentRequest()
    requestDialog.value = false // clean dialog
    // Show notification
    appState.value.notifications.push({
      type: "success", title: translate('title'), text: translate('text')
    })
  }).catch((err) => {
    appState.value.notifications.push({
      type: "error", title: translate('title_fail'),
      text: translate('text_fail', err.code)
    })
  }).finally(() => { loading.value = false })
}
const submitRequestTeacher = () => {
  studentModuleApi.putStudentModuleRequestTeacher(lesson.value.id, module.value.id, module.value.teacher, {
    responseText: requestText.value,
    evaluation: evalToggle.value,
    percent: module.value.type === 'ASSIGNMENT' ? evalPercent.value : null
  }).then(async () => {
    // Update the data
    await studentModuleApi.getStudentModuleRequestTeacher(lesson.value.id, module.value.id, module.value.teacher)
        .then((response) => {
          module.value.studentRequest = response
          reloadHook.value = new Date().getTime()
        })

    // Call the callbacks
    for (const callback of requestAnswerCallbacks.value) {
      await callback()
    }

    requestDialog.value = false // clean dialog
    reloadHook.value = new Date().getTime()
    if (module.value.studentRequest)
      module.value.studentRequest.codeCommentsHook = new Date().getTime()

    // Show notification
    appState.value.notifications.push({
      type: "success", title: translate('title'), text: translate('text')
    })
  }).catch((err) => {
    appState.value.notifications.push({
      type: "error", title: translate('title_fail'),
      text: translate('text_fail', err.code)
    })
  }).finally(() => { loading.value = false })
}

const handleEnterKey = (event) => {
  if (event.shiftKey) return
  event.preventDefault()
  submitRequest()
}

watch(requestDialog, (value) => { if(!value) {
  requestText.value = ''; evalToggle.value = false; evalPercent.value = 0
} })
</script>

<template>
  <v-dialog v-model="requestDialog" width="500">
    <v-card class="pb-6" :title="translate('dialog_title')">
      <v-card-item v-if="loading" class="flex justify-center">
        <v-progress-circular indeterminate />
      </v-card-item>
      <v-card-item v-else>
        <v-form @submit.prevent="submitRequest">
          <v-textarea v-model="requestText" :rows="3" :autofocus="true"
                      :label="translate('textarea')" @keydown.enter="handleEnterKey" />
          <v-slider v-if="module.teacher && module.manualEval && module.type === 'ASSIGNMENT'" v-model="evalPercent"
                    :label="translate('percent', evalPercent)" color="primary" min="0" max="100" step="1"
                    class="me-6" />
          <v-switch v-if="module.teacher && module.manualEval && module.studentRequest?.requestType !== 'HELP'" v-model="evalToggle" class="ps-2" color="primary"
                    :label="translate('toggle')" />
          <div class="d-flex justify-space-between">
            <v-btn type="button" color="red" variant="tonal"
                   @click="requestText = ''; requestDialog = false">
              {{ t('$vuetify.close') }}
            </v-btn>
            <v-btn type="button" color="green" variant="tonal"
                   :disabled="!requestText.length" @click="submitRequest">
              {{ translate('action') }}
            </v-btn>
          </div>
        </v-form>
      </v-card-item>
    </v-card>
  </v-dialog>
</template>
