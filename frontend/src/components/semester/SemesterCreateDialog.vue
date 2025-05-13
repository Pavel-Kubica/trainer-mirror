<script setup>
import {inject, ref} from 'vue'
import {semesterApi} from "@/service/api";
import {useLocale} from "vuetify";
import {getErrorMessage} from "@/plugins/constants";
import LocaleAwareDatepicker from "@/components/custom/LocaleAwareDatepicker.vue";
const displayDialog = ref(false)
const semesterData = ref(null)
const { t } = useLocale()

const appState = inject('appState')
semesterData.value = {
  code: "",
  from: "",
  until: "",
}

const fetchSemesters = inject('fetchSemesters')
const createSemester = async () => {
  if(!semesterData.value.from || !semesterData.value.until)
    return
  try {
    await semesterApi.createSemester(semesterData.value)
    displayDialog.value = false
    appState.value.notifications.push({
      type: "success", title: t('$vuetify.semester_creation_title'), text: t('$vuetify.semester_creation_success_text')
    })
    fetchSemesters()
  } catch (err){
    appState.value.notifications.push({
      type: "error", title: t('$vuetify.semester_creation_title'),
      text: getErrorMessage(t, err.code)
    })
  }
}

</script>


<template>
  <v-btn color="light-green-darken-1" @click="displayDialog = true">
    {{ t('$vuetify.create_semester') }}
  </v-btn>
  <v-dialog v-model="displayDialog">
    <v-form @submit.prevent="createSemester">
      <v-card :title="t('$vuetify.create_semester')" class="mx-auto px-6 py-8" width="380px">
        <v-card-text>
          <v-text-field v-model="semesterData.code" :label="t('$vuetify.semester_code')" />
          <v-label>{{ t('$vuetify.semester_start') }}</v-label>
          <LocaleAwareDatepicker v-model="semesterData.from" />
          <v-label>{{ t('$vuetify.semester_end') }}</v-label>
          <LocaleAwareDatepicker v-model="semesterData.until" />
        </v-card-text>
        <v-card-actions class="d-flex justify-center">
          <v-btn class="mr-15" variant="tonal" color="deep-purple-darken-1" @click="displayDialog=false">
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
