<script setup>
import {computed, inject, ref} from 'vue'
import {logApi} from "@/service/api";
import {useLocale} from "vuetify";
import {getErrorMessage} from "@/plugins/constants";
const displayDialog = ref(false)
const logData = ref(null)
const { t } = useLocale()

const appState = inject('appState')
logData.value = {
  date: "",
}

 const fetchLogs = inject('fetchLogs')
const deleteLogs = async () => {
  if(!logData.value.date)
    return
  try{
    await logApi.deleteLogs(logData.value.date)
    displayDialog.value = false
    appState.value.notifications.push({
      type: "success", title: t('$vuetify.log_delete_title'), text: t('$vuetify.log_delete_success_text')
    })
    fetchLogs()
  } catch (err){
    appState.value.notifications.push({
      type: "error", title: t('$vuetify.log_delete_title'),
      text: getErrorMessage(t, err.code)
    })
  }
}

const dateRules = computed(() => {
  return [
    (v) => (v ? true : `${t('$vuetify.log_date_required')}`),
  ];
});


</script>


<template>
  <v-btn color="light-green-darken-1" @click="displayDialog = true">
    {{ t('$vuetify.log_delete') }}
  </v-btn>
  <v-dialog v-model="displayDialog">
    <v-form @submit.prevent="deleteLogs">
      <v-card :title="t('$vuetify.log_delete')" class="mx-auto px-6 py-8" width="380px">
        <v-card-text>
          <v-text-field v-model="logData.date" :rules="dateRules" :label="t('$vuetify.log_delete_older_than')" type="date" />
        </v-card-text>
        <v-card-actions class="d-flex justify-center">
          <v-btn class="mr-15" variant="tonal" color="deep-purple-darken-1" @click="displayDialog=false">
            {{ t('$vuetify.dialog_close') }}
          </v-btn>
          <v-btn class="ml-15" variant="tonal" color="red" type="submit">
            {{ t('$vuetify.action_delete') }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-form>
  </v-dialog>
</template>
