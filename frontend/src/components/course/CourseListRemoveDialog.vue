<script setup>
import { inject } from 'vue'
import { useLocale } from 'vuetify'
import { courseUserApi } from '@/service/api'

const appState = inject('appState')
const props = defineProps(['courseId'])
const { t } = useLocale()

const deleteCourseUserDialog = inject('deleteCourseUserDialog')
const deleteCourseUser = inject('deleteCourseUser')
const loadCourseUsers = inject('loadCourseUsers')

const deleteCourseUserFn = (userId) => {
  courseUserApi.courseUserDelete(props.courseId, userId)
      .then(() => {
        deleteCourseUserDialog.value = false
        appState.value.notifications.push({
          type: "success", title: t(`$vuetify.course_user_delete_success_title`),
          text: t(`$vuetify.course_user_delete_success_text`),
        })
        loadCourseUsers()
      })
      .catch((err) => {
        appState.value.notifications.push({
          type: "error", title: t(`$vuetify.course_user_delete_error_title`),
          text: t(`$vuetify.course_user_delete_error_text`, err.code),
        })
      })
}
</script>

<template>
  <v-dialog v-model="deleteCourseUserDialog" width="auto">
    <v-card :title="t('$vuetify.course_user_delete_dialog_title')">
      <v-card-item v-html="t('$vuetify.course_user_delete_dialog_text', deleteCourseUser.name)" />
      <v-card-actions>
        <div class="d-flex flex-row justify-space-between fill-width pa-2">
          <v-btn color="primary" @click="deleteCourseUserDialog = false">
            {{ t('$vuetify.dialog_close') }}
          </v-btn>
          <v-btn color="red" @click="deleteCourseUserFn(deleteCourseUser.id)">
            {{ t('$vuetify.action_remove') }}
          </v-btn>
        </div>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<style scoped>
.fill-width {
  width: 100%
}
</style>
