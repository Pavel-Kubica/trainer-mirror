<script setup>
import {useUserStore} from "@/plugins/store";
import {semesterApi, subjectApi} from "@/service/api";
import {inject, onMounted, provide, ref} from "vue";
import {useLocale} from "vuetify";
import CourseSubjectDetailListRow from "@/components/subjects/CourseSubjectDetailListRow.vue";
import * as Nav from "@/service/nav";
import TooltipIconButton from "@/components/custom/TooltipIconButton.vue";
import SubjectDeleteDialog from "@/components/subjects/SubjectDeleteDialog.vue";


const props = defineProps(['subject', 'semester'])
const userStore = useUserStore()
const error = ref(null)
const coursesSubject = ref([])
const {t} = useLocale()
const emit = defineEmits(['updateSubject'])
const displayDeleteDialog = ref(false)
const appState = inject('appState')

provide('displayDeleteDialog', displayDeleteDialog)
const loadSemesterDetail = inject('loadSemesterDetail')

const loadCoursesDetail = async () => {
  await semesterApi.listCoursesBySemesterAndSubject(props.semester, props.subject.id)
      .then((result) => {
        coursesSubject.value = result
        emit("updateSubject");
      })
      .catch((err) => {
        error.value = err.code
      })
}

const deleteSubject = async () => {
  console.log(props.subject)
  if (coursesSubject.value.length > 0) {
    appState.value.notifications.push({
      type: "error", title: t('$vuetify.subject_delete'),
      text: t('$vuetify.subject_delete_courses_present')
    })
    displayDeleteDialog.value = false
    return
  }
  await subjectApi.deleteSubject(props.subject.id)
  displayDeleteDialog.value = false
  await loadSemesterDetail()
}

onMounted(async () => {
  await loadCoursesDetail()
})
</script>

<template>
  <SubjectDeleteDialog :subject="subject" @delete-button-clicked="deleteSubject" />
  <tr style="cursor: pointer" @click="userStore.hiddenSubjects[subject.id] = !userStore.hiddenSubjects[subject.id]">
    <th v-ripple.center colspan="5">
      <div class="d-flex justify-space-between align-center" style="gap: 0">
        <span class="ps-2">{{ subject.code }} </span>
        <span class="flex-grow-1 text-center">{{ coursesSubject.length }} {{ t('$vuetify.subject_course_num') }}</span>
        <TooltipIconButton v-if="userStore.isAdmin" icon="mdi-pencil" color="black" :to="new Nav.SubjectEdit(subject).routerPath()"
                           @click="(e) => e.stopPropagation()" />
        <v-btn color="black" variant="text" :to="new Nav.CourseCreate(props.semester, props.subject.id).routerPath()"
               icon @click="(e) => e.stopPropagation()">
          <v-icon icon="mdi-plus" />
          <v-tooltip activator="parent" location="top">
            {{ t('$vuetify.subject_new_course_button') }}
          </v-tooltip>
        </v-btn>
        <TooltipIconButton v-if="userStore.isAdmin" icon="mdi-delete" color="black"
                           @click="(e) => {e.stopPropagation(); displayDeleteDialog=true}" />
        <v-btn variant="text" :icon="userStore.hiddenSubjects[subject.id] ? 'mdi-chevron-down' : 'mdi-chevron-up'" />
      </div>
    </th>
  </tr>
  <v-table v-show="!userStore.hiddenSubjects[subject.id]">
    <CourseSubjectDetailListRow v-for="course in coursesSubject" :key="course.id" :course="course" @courseDeleted="loadCoursesDetail" />
  </v-table>
</template>

<style scoped>

</style>