<script setup>
import {topicApi} from "@/service/api";
import {getErrorMessage} from "@/plugins/constants";
import {useLocale} from "vuetify";
import {inject} from "vue";

const { t } = useLocale()
const deleteDialog = inject('deleteDialog')
const props = defineProps(['deleteTopic', 'callback'])
const appState = inject('appState')

const translate = (key) => t(`$vuetify.topic_list_delete_${key}`)

const deleteCallback = (entityId) => {
  deleteDialog.value = false
  const promise = topicApi.topicDelete(entityId)
  promise
      .then(() => {
        appState.value.notifications.push({
          type: "success", title: translate('success_title'), text: translate('success_text')
        })
        props.callback()
      })
      .catch((err) => {
        appState.value.notifications.push({
          type: "error", title: t(`$vuetify.error_deleting_topic`),
          text: getErrorMessage(t, err.code)
        })
      })
}

</script>

<template>
  <v-dialog v-model="deleteDialog" width="300px">
    <v-card :title="translate('title')">
      <v-card-item>
        {{ translate('confirm') }} <strong>{{ deleteTopic.name }}</strong>?
      </v-card-item>
      <v-card-actions>
        <div style="width: 100%" class="d-flex flex-row justify-space-between pa-2">
          <v-btn color="primary" @click="deleteDialog = false">
            {{ t('$vuetify.dialog_close') }}
          </v-btn>
          <v-btn color="red" @click="deleteCallback(deleteTopic.id)">
            {{ t('$vuetify.dialog_delete') }}
          </v-btn>
        </div>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>
