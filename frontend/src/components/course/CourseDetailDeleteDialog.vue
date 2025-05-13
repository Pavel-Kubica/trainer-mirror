<script setup>
import { inject } from 'vue'
import { lessonApi, weekApi } from '@/service/api'
import { useLocale } from 'vuetify'
import { getErrorMessage } from '@/plugins/constants'

const appState = inject('appState')
const props = defineProps(['deleteLesson', 'deleteWeek', 'callback'])
const deleteDialog = inject('deleteDialog')
const { t } = useLocale()

const translate = (key) => t(`$vuetify.course_detail_${props.deleteLesson ? 'lesson' : 'week'}_delete_${key}`)
const deleteCallback = (entityId) => {
  deleteDialog.value = false
  const promise = props.deleteLesson ? lessonApi.deleteLesson(entityId) : weekApi.deleteWeek(entityId)
  promise
      .then(() => {
        appState.value.notifications.push({
          type: "success", title: translate('success_title'), text: translate('success_text')
        })
        props.callback()
      })
      .catch((err) => {
        appState.value.notifications.push({
          type: "error", title: t(`$vuetify.error_deleting_${props.deleteLesson ? 'lesson' : 'week'}`),
          text: getErrorMessage(t, err.code)
        })
      })
}
</script>

<template>
  <v-dialog v-model="deleteDialog" width="auto">
    <v-card :title="translate('title')">
      <v-card-item>
        {{ translate('confirm') }} <strong>{{ deleteLesson ? deleteLesson.name : deleteWeek.name }}</strong>?
      </v-card-item>
      <v-card-actions>
        <div style="width: 100%" class="d-flex flex-row justify-space-between pa-2">
          <v-btn color="primary" @click="deleteDialog = false">
            {{ t('$vuetify.dialog_close') }}
          </v-btn>
          <v-btn color="red" @click="deleteCallback(deleteLesson ? deleteLesson.id : deleteWeek.id)">
            {{ t('$vuetify.dialog_delete') }}
          </v-btn>
        </div>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>
