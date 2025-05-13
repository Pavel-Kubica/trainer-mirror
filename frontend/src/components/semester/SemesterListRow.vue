<script setup>
import {inject, onMounted, provide, ref} from 'vue';
import {semesterApi} from "@/service/api";
import {useUserStore} from "@/plugins/store";
import SemesterDeleteDialog from "@/components/semester/SemesterDeleteDialog.vue";
import SemesterEditDialog from "@/components/semester/SemesterEditDialog.vue";
import {useLocale} from "vuetify";
import {getErrorMessage} from "@/plugins/constants";

const { t } = useLocale()
const emit = defineEmits(['semesterDeleted'])
const props = defineProps(['semester'])
const dateFrom = new Date(props.semester.from)
const dateUntil = new Date(props.semester.until)

function formatDate(date){
  const day = date.getDate().toString().padStart(2, '0');
  const month = (date.getMonth() + 1).toString().padStart(2, '0');
  const year = date.getFullYear();
  return `${day}.${month}.${year}`;
}

function extractThreeDistinctSubjectsFromCourses(coursesData) {
  const subjectList = [];

  for (const course of coursesData) {
    const subjectCode = course.subject.code;

    if (!subjectList.includes(subjectCode)) {
      subjectList.push(subjectCode);
    }

    if (subjectList.length > 3) {
      subjectList[3] = '...'
      break;
    }
  }

  return subjectList;
}

const threeSubjectNames = ref([])
const amountOfCourses = ref(0)
const error = ref(null)

onMounted(async () => {
  semesterApi.getSemesterCourses(props.semester.id)
      .then((courses) => {
        amountOfCourses.value = courses.length
        threeSubjectNames.value = extractThreeDistinctSubjectsFromCourses(courses);
      })
      .catch((err) => {error.value = err.code})
})

const store = useUserStore()
const displayDeleteDialog = ref(false)
const displayEditDialog = ref(false)
provide('displayDeleteDialog', displayDeleteDialog)
provide('displayEditDialog', displayEditDialog)
const appState = inject('appState')
const deleteSemester = async () => {
  try {
    if(amountOfCourses.value > 0){
      appState.value.notifications.push({
        type: "error", title: t('$vuetify.semester_deletion_title'),
        text: t('$vuetify.semester_cant_delete_courses_present')
      })
      displayDeleteDialog.value = false
      return
    }
    await semesterApi.deleteSemester(props.semester.id);
    displayDeleteDialog.value = false
    appState.value.notifications.push({
      type: "success", title: t('$vuetify.semester_deletion_title'), text: t('$vuetify.semester_deletion_success_text')
    })
    emit('semesterDeleted')
  } catch (err) {
    appState.value.notifications.push({
      type: "error", title: t('$vuetify.semester_deletion_title'),
      text: getErrorMessage(t, err.code)
    })
  }
};


</script>

<template>
  <v-toolbar :color="store.darkMode ? 'grey-darken-4' : 'white'">
    <v-icon size="x-large" style="padding-right: 10px; padding-left: 10px">
      mdi-blur-linear
    </v-icon>

    <v-toolbar-title>
      <span style="font-weight: bold;">{{ semester.code }}</span><br>
      <span style="font-size: 0.8em;">{{ formatDate(dateFrom) }} - {{ formatDate(dateUntil) }}</span>
    </v-toolbar-title>

    <v-toolbar-title style="padding-right: 130px;">
      <p style="font-size: 0.8em;">
        {{ `${t('$vuetify.subjects')}: ${threeSubjectNames.join(', ')}` }}
      </p>
      <p style="font-size: 0.6em; font-style: italic; display: inline;">
        {{ `${t('$vuetify.courses_total')}: ` }}
      </p>
      <p style="font-size: 0.6em; display: inline;">
        {{ `${amountOfCourses}` }}
      </p>
    </v-toolbar-title>

    <v-toolbar-items>
      <v-btn icon="mdi-pencil" variant="text" color="blue" @click="displayEditDialog=true" />
      <v-btn icon="mdi-delete" variant="text" color="red" @click="displayDeleteDialog=true" />
    </v-toolbar-items>
  </v-toolbar>

  <SemesterDeleteDialog :semester="semester" @deleteButtonClicked="deleteSemester" />
  <SemesterEditDialog :semester="semester" />

  <h1>{{ error }}</h1>
</template>



