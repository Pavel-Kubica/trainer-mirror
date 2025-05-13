<script setup>
import {computed, inject, ref} from 'vue'
import {semesterApi} from "@/service/api";
import {useLocale} from "vuetify";
import {getErrorMessage} from "@/plugins/constants";
const semesterData = ref(null)
const props = defineProps(['semester'])
const { t } = useLocale()
const appState = inject('appState')

function formatDate(dateStr) {
  const date = new Date(dateStr);
  const year = date.getFullYear();
  const month = (date.getMonth() + 1).toString().padStart(2, '0');
  const day = date.getDate().toString().padStart(2, '0');
  return `${year}-${month}-${day}`;
}

semesterData.value = {
  code: props.semester.code,
  from: formatDate(new Date(props.semester.from)),
  until: formatDate(new Date(props.semester.until)),
}

const fetchSemesters = inject('fetchSemesters')
const displayEditDialog = inject('displayEditDialog')
const editSemester = async () => {
  if(!semesterData.value.from || !semesterData.value.until)
    return
  try {
    await semesterApi.editSemester(props.semester.id, semesterData.value)
    displayEditDialog.value = false
    appState.value.notifications.push({
      type: "success", title: t('$vuetify.semester_edit_title'), text: t('$vuetify.semester_edit_success_text')
    })
    fetchSemesters()
  } catch (err)   {
    appState.value.notifications.push({
      type: "error", title: t('$vuetify.semester_edit_title'),
      text: getErrorMessage(t, err.code)
    })
  }
}

const dateRules = computed(() => {
  return [
    (v) => (v ? true : `${t('$vuetify.semester_date_required')}`),
  ];
});


</script>


<template>
  <v-dialog v-model="displayEditDialog">
    <v-form @submit.prevent="editSemester">
      <v-card :title="t('$vuetify.semester_edit')" class="mx-auto px-6 py-8" width="380px">
        <v-card-text>
          <v-text-field v-model="semesterData.code" :label="t('$vuetify.semester_code')" />
          <v-text-field v-model="semesterData.from" :rules="dateRules" :label="t('$vuetify.semester_start')" type="date" />
          <v-text-field v-model="semesterData.until" :rules="dateRules" :label="t('$vuetify.semester_end')" type="date" />
        </v-card-text>
        <v-card-actions class="d-flex justify-center">
          <v-btn class="mr-15" variant="tonal" color="deep-purple-darken-1" @click="displayEditDialog=false">
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
