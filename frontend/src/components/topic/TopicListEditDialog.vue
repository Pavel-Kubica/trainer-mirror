<script setup>
import {inject, onMounted, ref, watch} from "vue";
import {useLocale} from "vuetify";
import {topicApi} from "@/service/api";
import LoadingScreen from "@/components/custom/LoadingScreen.vue";

const {t} = useLocale();
const editCreateDialog = inject('editDialog');
const props = defineProps(['editTopic', 'callback']);
const appState = inject('appState');

const error = ref(null)
const loading = ref(false)
const translate = (key) => t(`$vuetify.topic_edit_${key}`)

const topicData = ref(null)


const submitAllowed = () => {
  return topicData.value && topicData.value.name
}
const submit = () => {
  if (!submitAllowed())
    return false

  loading.value = true
  const promise = props.editTopic ? topicApi.editTopic(props.editTopic.id, topicData.value) : topicApi.createTopic(topicData.value)
  promise
      .then(async () => {
        const key = props.editTopic ? 'edit' : 'create'
        appState.value.notifications.push({
          type: "success", title: t(`$vuetify.topic_${key}_notification_title`),
          text: t(`$vuetify.topic_${key}_notification_text`),
        })
        props.callback()
        loading.value = false
        editCreateDialog.value = false
      })
      .catch((err) => {
        error.value = err.code
      })
  topicData.value.name = ""
}

const loaded = () => {
  if (props.editTopic)
    topicData.value = Object.assign({}, props.editTopic)
  else
    topicData.value = {
      name: ""
    }
  loading.value = false
}

watch(props, loaded)
onMounted(loaded)

</script>

<template>
  <v-dialog v-model="editCreateDialog" width="400px">
    <v-card :title="translate(`dialog_${editTopic ? 'edit' : 'create'}`)">
      <LoadingScreen :items="!loading" :error="error">
        <template #content>
          <v-form @submit.prevent="submit">
            <v-text-field v-model="topicData.name" :label="translate('name')" autofocus clearable />
            <v-btn class="my-4" variant="tonal" color="info" type="submit"
                   :block="true" :disabled="!submitAllowed()">
              {{ t(`$vuetify.action_${editTopic ? 'edit' : 'create'}`) }}
            </v-btn>
          </v-form>
        </template>
      </LoadingScreen>
      <v-card-actions>
        <div style="width: 100%" class="d-flex flex-row justify-space-between pa-2">
          <v-btn v-if="error" :block="true" variant="tonal" color="warning" @click="error = null; loading = false">
            {{ t('$vuetify.dialog_try_again') }}
          </v-btn>
          <v-btn v-else :block="true" variant="tonal" color="error" @click="editCreateDialog = false">
            {{ t('$vuetify.dialog_close') }}
          </v-btn>
        </div>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>
