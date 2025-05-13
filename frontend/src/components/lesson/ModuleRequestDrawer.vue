<script setup>
import {computed, onBeforeMount, ref, inject, onBeforeUnmount} from "vue";
import {studentModuleApi} from "@/service/api";
import {useLocale} from "vuetify";
import {moduleRequestIsHelp} from "@/plugins/constants";
import {useUserStore} from "@/plugins/store";

const {t} = useLocale()
const appState = inject('appState')
const userStore = useUserStore()

const lesson = inject('lesson')
const module = inject('module')
const requestAnswerCallbacks = inject('requestAnswerCallbacks')

const requests = ref(null)

const requestText = ref(userStore.requestText)
const evalPercent = ref(0)
const completed = ref(false)

const modulesReloadHook = inject('modulesReloadHook')
const usersReloadHook = inject('usersReloadHook', {})

const lastRequest = computed(() => requests.value?.length ? requests.value[requests.value.length - 1] : null) // The most recent request is last because request array is reversed
const hasOpenRequest = computed(() => lastRequest.value && !lastRequest.value.satisfied) // Is there an unsatisfied request?
const readyForManEval = computed(() => module.value.type === 'ASSIGNMENT' || (module.value.manualEval && module.value.progress >= module.value.minPercent))

const requestType = computed(() => moduleRequestIsHelp(module.value) ? 'HELP' : 'EVALUATE')

const user = inject('lessonUser')

const submitButtonColor = computed(() => {
  if (module.value.teacher || !hasOpenRequest.value) { // Teacher, no open requests
    return 'green'
  }
  else {
    return 'red'
  }
})

const submitButtonText = computed(() => {
  if (module.value.teacher) {
    if (hasOpenRequest.value) {
      if (lastRequest.value.requestType === 'HELP') { // Teacher, pending help request
        return '$vuetify.request_teacher_help_btn'
      }
      else { // Teacher, pending evaluation request
        return '$vuetify.request_teacher_eval_btn'
      }
    }
    else { // Teacher, no unsatisfied request
      return '$vuetify.request_teacher_comment_btn'
    }
  }
  else { // Student view
    if (hasOpenRequest.value) {
      if (lastRequest.value.requestType === 'HELP') { // Student, pending help request
        return '$vuetify.request_help_cancel_btn'
      }
      else { // Student, pending evaluation request
        return '$vuetify.request_eval_cancel_btn'
      }
    }
    else {
      if (readyForManEval.value) { // Student, no unsatisfied request, completed but requires manual evaluation
        return '$vuetify.request_eval_btn'
      }
      else { // Student, no unsatisfied request, no manual evaluation
        return '$vuetify.request_help_btn'
      }
    }
  }
})

const reloadRequests = async () => {
  const promise = user.value ? studentModuleApi.getStudentModuleRequestsTeacher(lesson.value.id, module.value.id, user.value.id) : studentModuleApi.getStudentModuleRequests(lesson.value.id, module.value.id)
  requests.value = (await promise).reverse() // Reverse array to have most recent request at the bottom
  module.value.studentRequest = lastRequest;
  module.value.codeCommentSourceRequest = lastRequest.value?.satisfied ? requests.value[requests.value.length - 1] : requests.value[requests.value.length - 2];
}

const reloadLeftSidebar = () => {
  modulesReloadHook.value = new Date().getTime()
  usersReloadHook.value = new Date().getTime()
}

const clearText = () => {
  requestText.value = ""
  userStore.requestText = ""
}

const studentRequest = () => {
  if (hasOpenRequest.value) { // Cancelling existing request
    const textKeyBase = lastRequest.value.requestType === 'HELP' ? '$vuetify.request_help.' : '$vuetify.request_eval.';
    requests.value = null; // Show progress
    studentModuleApi.deleteStudentModuleRequest(lesson.value.id, module.value.id).then(async () => {
      appState.value.notifications.push({
        type: "success", title: t(textKeyBase + 'cancel_title'),
        text: t(textKeyBase + 'cancel_text')
      })
      await reloadRequests()
      reloadLeftSidebar()
    }).catch((err) => {
      appState.value.notifications.push({
        type: "error", title: t(textKeyBase + 'title_fail'),
        text: t(textKeyBase + 'text_fail', err.code)
      })
    })
  }
  else {
    requests.value = null; // Show progress
    const textKeyBase = requestType.value === 'HELP' ? '$vuetify.request_help.' : '$vuetify.request_eval.';
    studentModuleApi.putStudentModuleRequest(lesson.value.id, module.value.id, {
      requestText: requestText.value,
      requestType: requestType.value
    }).then(async () =>
    {
      appState.value.notifications.push({
        type: "success", title: t(textKeyBase + 'title'),
        text: t(textKeyBase + 'text')
      })
      await reloadRequests();
      reloadLeftSidebar(); // Left sidebar depends on accurate requests
      clearText();
    }).catch((err) =>
    {
      appState.value.notifications.push({
        type: "error", title: t(textKeyBase + 'title_fail'),
        text: t(textKeyBase + 'text_fail', err.code)
      })
    })
  }
}

const teacherRequest = () => {
  requests.value = null; // Show progress
  let successTitle = '$vuetify.request_teacher_help.title';
  let successText = '$vuetify.request_teacher_help.text';
  let failTitle = '$vuetify.request_teacher_help.title_fail';
  let failText = '$vuetify.request_teacher_help.text_fail';
  if (!hasOpenRequest.value) {
    successTitle = '$vuetify.request_teacher_comment.title';
    successText = '$vuetify.request_teacher_comment.text';
    failTitle = '$vuetify.request_teacher_comment.title_fail';
    failText = '$vuetify.request_teacher_comment.text_fail';
  }
  else if (lastRequest.value.requestType === 'EVALUATE') {
    successTitle = '$vuetify.request_teacher_eval.title';
    successText = '$vuetify.request_teacher_eval.text';
    failTitle = '$vuetify.request_teacher_eval.title_fail';
    failText = '$vuetify.request_teacher_eval.text_fail';
  }
  studentModuleApi.putStudentModuleRequestTeacher(lesson.value.id, module.value.id, user.value.id, {
    responseText: requestText.value,
    evaluation: completed.value,
    percent: module.value.type === 'ASSIGNMENT' ? evalPercent.value : null
  }).then(async () => {
    appState.value.notifications.push({
      type: "success", title: t(successTitle), text: t(successText)
    })
    await reloadRequests();
    reloadLeftSidebar(); // Left sidebar depends on accurate requests
    clearText()

    for (const callback of requestAnswerCallbacks.value) {
      await callback()
    }
    if (module.value.studentRequest)
      module.value.studentRequest.codeCommentsHook = new Date().getTime()

  }).catch((err) => {
    appState.value.notifications.push({
      type: "error", title: t(failTitle), text: t(failText, err.code)
    })
  })
}

const deleteTeacherAnswer = async () => {
  requests.value = null; // Show progress
  studentModuleApi.deleteStudentModuleRequestAnswer(lesson.value.id, module.value.id, user.value.id)
      .then(async () => {
        appState.value.notifications.push({
          type: "success", title: t('$vuetify.request_answer_deleted_title'),
          text: t('$vuetify.request_answer_deleted_text')
        })
        await reloadRequests();
        reloadLeftSidebar(); // Left sidebar depends on accurate requests
      })
      .catch((err) => {
        appState.value.notifications.push({
          type: "error", title: t('$vuetify.request_answer_deleted_title_fail'),
          text: t('$vuetify.request_answer_deleted_text_fail', err.code)
        })
      })
}

const deleteDialog = inject('deleteDialog')
const deleteDialogTexts = inject('deleteDialogTexts')
const doDelete = inject('doDelete')
if (doDelete) {
  doDelete.value = () =>
  {
    deleteTeacherAnswer();
    deleteDialog.value = false;
  };
}

const promptAnswerDelete = (request) => {
  if (request.requestType === 'COMMENT') {
    deleteDialogTexts.value.title = '$vuetify.request_comment_delete_btn'
    deleteDialogTexts.value.start = '$vuetify.lesson_solutions_delete_dialog_start_comment'
  }
  else {
    deleteDialogTexts.value.title = '$vuetify.request_answer_delete_btn'
    deleteDialogTexts.value.start = '$vuetify.lesson_solutions_delete_dialog_start_answer'
  }
  deleteDialog.value = true;
}

onBeforeMount(async () => {
  await reloadRequests()
})

onBeforeUnmount(() => userStore.requestText = requestText.value)

</script>

<template>
  <div style="height: 90%; display: flex; flex-direction: column; justify-content: flex-end; align-items: center">
    <v-progress-circular v-if="!requests" indeterminate style="flex: 0 0 1" />
    <template v-else>
      <div style="width: 100%; height: 60%; display: flex; flex-direction: column; justify-content: safe flex-end; align-items: center; overflow-y: auto">
        <template v-for="(request, index) in requests" :key="request.id">
          <div v-if="request.requestType !== 'COMMENT'" class="pa-2"
               style="width: 60%; align-self: start; margin: 12px;
               display: flex; flex-direction: column;
               background-color: #fef1e0; color: orange;
               overflow-wrap: break-word">
            <span style="align-self: end; color: grey; margin-right: 8px">
              {{ new Date(Date.parse(request.requestedOn)).toLocaleString([], {year: 'numeric', month: 'numeric', day: 'numeric', hour: '2-digit', minute: '2-digit'}) }}
            </span>
            <span>
              <strong>{{ request.requestType === 'HELP' ? t('$vuetify.pending_request_help') : t('$vuetify.pending_request_eval') }}:</strong>
              {{ request.requestText.length ? request.requestText : t('$vuetify.request_no_text') }}
            </span>
          </div>
          <div v-if="request.satisfied" class="pa-2"
               style="width: 60%; align-self: end; margin: 12px;
               display: flex; flex-direction: column;
               background-color: #e4f2fd; color: blue;
               overflow-wrap: break-word">
            <span style="align-self: end; color: grey; margin-right: 8px">
              {{ new Date(Date.parse(request.satisfiedOn)).toLocaleString([], {year: 'numeric', month: 'numeric', day: 'numeric', hour: '2-digit', minute: '2-digit'}) }}
            </span>
            <span>
              <strong>{{ request.requestType === 'COMMENT' ? t('$vuetify.request_teacher_commented', request.teacherComment.name) : t('$vuetify.request_teacher_answered', request.teacherComment.name) }}:</strong>
              {{ request.teacherComment.text.length ? request.teacherComment.text : t('$vuetify.request_no_text') }}
            </span>
            <v-btn v-if="module.teacher && index === requests.length - 1" class="mt-2" prepend-icon="mdi-delete"
                   variant="tonal" @click="() => promptAnswerDelete(request)">
              {{ request.requestType === 'COMMENT' ? t('$vuetify.request_comment_delete_btn') : t('$vuetify.request_answer_delete_btn') }}
            </v-btn>
          </div>
        </template>
        <span v-if="!requests?.length" style="align-self: start; margin: 12px 24px">
          {{ t('$vuetify.request_none') }}
        </span>
      </div>
      <div v-if="module.teacher || !hasOpenRequest" style="width: 90%; flex-grow: 0; box-sizing: content-box; position: relative">
        <v-textarea v-model="requestText" :rows="5" hide-details />
      </div>
      <template v-if="module.teacher && module.manualEval && lastRequest?.requestType === 'EVALUATE' && !lastRequest?.satisfied">
        <v-slider v-model="evalPercent"
                  :label="`${t('$vuetify.request_teacher_eval.percent')} ${evalPercent} %`"
                  color="primary" min="0" max="100" step="1"
                  style="display: block; box-sizing: content-box; width: 90%;flex-grow: 0"
                  class="mt-2" hide-details />
        <v-switch v-model="completed" color="primary"
                  :label="t('$vuetify.request_teacher_eval.toggle')" hide-details />
      </template>
      <v-btn variant="tonal" style="width: 90%" class="ma-6" :color="submitButtonColor" :disabled="false"
             @click="() => {module.teacher ? teacherRequest() : studentRequest()}">
        {{ t(submitButtonText) }}
      </v-btn>
    </template>
  </div>
</template>

<style scoped>

</style>