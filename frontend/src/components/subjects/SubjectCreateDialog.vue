<script setup>
import {inject, ref} from 'vue'
import {useLocale} from "vuetify";
import {subjectApi} from "@/service/api";
import {getErrorMessage} from "@/plugins/constants";

const createEditDialog = inject('createEditDialog')
const subjectData = ref(null)
const {t} = useLocale()
const appState = inject('appState')
const loadSemesterDetail = inject('loadSemesterDetail')

subjectData.value = {
  code: "",
  name: ""
}

const createSemester = async () => {
  if (!subjectData.value.code || !subjectData.value.name)
    return
  try {
    await subjectApi.createSubject(subjectData.value)
    createEditDialog.value = false
    appState.value.notifications.push({
      type: "success", title: t('$vuetify.subject_create_notification_title'), text: t('$vuetify.subject_create_notification_text')
    })
    await loadSemesterDetail()
  } catch (err) {
    appState.value.notifications.push({
      type: "error", title: t('$vuetify.subject_create_notification_title'),
      text: getErrorMessage(t, err.code)
    })
  }
}

</script>


<template>
  <v-dialog v-model="createEditDialog">
    <v-form @submit.prevent="createSemester">
      <v-card :title="t('$vuetify.subject_create_new')" class="mx-auto px-6 py-8" width="380px">
        <v-card-text>
          <v-text-field v-model="subjectData.code" :label="t('$vuetify.subject_create_edit_code')" />
          <v-text-field v-model="subjectData.name" :label="t('$vuetify.subject_create_edit_name')" />
        </v-card-text>
        <v-card-actions class="d-flex justify-center">
          <v-btn class="mr-15" variant="tonal" color="deep-purple-darken-1" @click="createEditDialog=false">
            {{ t('$vuetify.dialog_close') }}
          </v-btn>
          <v-btn class="ml-15" variant="tonal" color="blue" type="submit">
            {{ t('$vuetify.dialog_save') }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-form>
  </v-dialog>
</template>
