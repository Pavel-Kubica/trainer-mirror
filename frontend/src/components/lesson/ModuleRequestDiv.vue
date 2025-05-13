<script setup>
import { inject } from 'vue'
import { useLocale } from 'vuetify'
import { studentModuleApi } from '@/service/api'
import { useUserStore } from "@/plugins/store";
import {
  moduleRequestInfo,
  moduleRequestIsHelp,
  moduleRequestTeacher,
  unsatisfiedModuleRequest
} from '@/plugins/constants'

const appState = inject('appState')
const reloadHook = inject('reloadHook')
const requestDialog = inject('requestDialog')
const userStore = useUserStore()
const { t } = useLocale()

const moduleData = inject('module')
const lesson = inject('lesson')

const isHelpRequest = () => moduleRequestIsHelp(moduleData.value)
const translate = (key, param = undefined) => {
  return t(`$vuetify.request_${isHelpRequest() ? 'help' : 'eval'}.cancel_${key}`, param)
}

const requestToggle = () => {
  if (!unsatisfiedModuleRequest(moduleData.value)) {
    requestDialog.value = true
    return
  }
  studentModuleApi.deleteStudentModuleRequest(lesson.value.id, moduleData.value.id).then(() => {
    moduleData.value.studentRequest = null
    reloadHook.value = new Date().getTime()
    appState.value.notifications.push({
      type: "success", title: translate('title'), text: translate('text')
    })
  }).catch((err) => {
    // check if the request was already satisfied
    let alreadySatisfied = false
    studentModuleApi.getStudentModuleRequests(lesson.value.id, moduleData.value.id).then((response) => {
      if (response && response.satisfied) {
        appState.value.notifications.push({
          type: "error", title: translate('title_fail'),
          text: translate('text_fail_satisfied')
        })
        alreadySatisfied = true
        moduleData.value.studentRequest = response
        reloadHook.value = new Date().getTime()
      }
    }).then(() => {
      if (!alreadySatisfied) {
        appState.value.notifications.push({
          type: "error", title: translate('title_fail'),
          text: translate('text_fail', err.code)
        })
      }
    })
  })
}

const loadStudentRequest = () => {
  studentModuleApi.getStudentModuleRequestsTeacher(lesson.value.id, moduleData.value.id, moduleData.value.teacher)
      .then((response) => {
        moduleData.value.studentRequest = response
        reloadHook.value = new Date().getTime()
      })
      .catch((err) => {
        appState.value.notifications.push({
          type: "error", title: t('$vuetify.request_load_fail'),
          text: t('$vuetify.request_load_text_fail', err.code)
        })
      })
}

const deleteTeacherAnswer = () => {
  studentModuleApi.deleteStudentModuleRequestAnswer(lesson.value.id, moduleData.value.id, moduleData.value.teacher)
      .then(() => {
        loadStudentRequest()
        reloadHook.value = new Date().getTime()
        appState.value.notifications.push({
          type: "success", title: t('$vuetify.request_answer_deleted_title'),
          text: t('$vuetify.request_answer_deleted_text')
        })
      })
      .catch((err) => {
        appState.value.notifications.push({
          type: "error", title: t('$vuetify.request_answer_deleted_title_fail'),
          text: t('$vuetify.request_answer_deleted_text_fail', err.code)
        })
      })
}

const isRelevantTeacherAnswer = () => {
  return Boolean(moduleData.value.studentRequest?.teacherComment)
}

</script>

<template>
  <div class="d-flex flex-column">
    <div class="mx-4 mt-4 mb-2 overflow-y-auto max-height-tab-third">
      <v-alert v-if="(isRelevantTeacherAnswer() || unsatisfiedModuleRequest(moduleData)) && !userStore.anonymous" variant="tonal"
               :type="isRelevantTeacherAnswer() ? 'info' : 'warning'"
               :icon="isRelevantTeacherAnswer() ? 'mdi-information-outline' : 'mdi-alert'">
        <p style="white-space: pre-wrap;">
          {{ moduleRequestInfo(t, moduleData) }}
        </p>
        <v-btn v-if="moduleData.teacher && isRelevantTeacherAnswer()" class="mt-2" prepend-icon="mdi-delete"
               variant="tonal" @click="deleteTeacherAnswer">
          {{ t('$vuetify.request_answer_delete_btn') }}
        </v-btn>
      </v-alert>
    </div>

    <div class="d-flex justify-space-between px-4 pb-2 pt-4">
      <v-btn v-if="!moduleData.teacher && moduleData.manualEval && !isHelpRequest()" class="flex-grow-1 ms-4" variant="tonal"
             :color="unsatisfiedModuleRequest(moduleData) ? 'red' : 'green'" @click="requestToggle">
        {{ t(`$vuetify.request_eval_${unsatisfiedModuleRequest(moduleData) ? 'cancel_' : ''}btn`) }}
      </v-btn>
      <v-btn v-if="!moduleData.teacher && isHelpRequest()" class="ms-4" variant="tonal"
             :color="moduleData.requestType ? 'red' : 'orange'" @click="requestToggle">
        {{ t(`$vuetify.request_help_${unsatisfiedModuleRequest(moduleData) ? 'cancel_' : ''}btn`) }}
      </v-btn>
      <v-btn v-if="moduleData.teacher" class="flex-grow-1 ms-4" variant="tonal"
             color="green" @click="requestDialog = true">
        {{ t(`$vuetify.request_teacher_${moduleRequestTeacher(moduleData)}_btn`) }}
      </v-btn>
    </div>
  </div>
</template>
