<script setup>
import {onMounted, provide, ref, watch} from 'vue'
import {semesterApi} from '@/service/api'
import LoadingScreen from "@/components/custom/LoadingScreen.vue";
import {useLocale} from "vuetify";
import SubjectListDetailRow from "@/components/subjects/SubjectListDetailRow.vue";
import CourseDetailDeleteDialog from "@/components/course/CourseDetailDeleteDialog.vue";
import {useUserStore} from "@/plugins/store";
import SubjectDetailEditFAB from "@/components/subjects/SubjectDetailEditFAB.vue";
import SubjectCreateDialog from "@/components/subjects/SubjectCreateDialog.vue";


const {t} = useLocale()
const props = defineProps(['semester'])

const deleteCourse = ref(null)
const deleteDialog = ref(false)
const subjects = ref([])
const reloadPage = ref(false)
const error = ref(null)
const userStore = useUserStore()
const createEditDialog = ref(false)

provide('subjects', subjects)
provide('deleteCourseEntity', deleteCourse)
provide('deleteDialog', deleteDialog)
provide('createEditDialog', createEditDialog)

const loadSemesterDetail = async () => {
  await semesterApi.listSubjects()
      .then((result) => {
        reloadPage.value = false
        subjects.value = result
        reloadPage.value = true
      })
      .catch((err) => {
        error.value = err.code
      })
}

provide('loadSemesterDetail', loadSemesterDetail)

onMounted(async () => {
  subjects.value = []
  await loadSemesterDetail()
})
watch(props, async () => {
  subjects.value = []
  await loadSemesterDetail()
})
</script>

<template>
  <SubjectCreateDialog :callback="loadSemesterDetail" />
  <CourseDetailDeleteDialog :delete-course="deleteCourse" :callback="loadSemesterDetail" />
  <div style="display: flex; justify-content: center;">
    <v-card width="97%" :title="t('$vuetify.subject_detail_list')" style="text-align: left;">
      <LoadingScreen v-if="reloadPage" :items="subjects" :error="error">
        <template #content>
          <v-table>
            <SubjectListDetailRow v-for="subject in subjects.filter( subject => ( userStore.user.isGuarantor.some(id => id === subject.id)) || userStore.user.isAdmin )" :key="subject.id" :subject="subject" :semester="semester" @updateSubject="loadSemesterDetail" />
          </v-table>
          <span v-if="subjects.length === 0">{{ t('$vuetify.subject_none') }}</span>
        </template>
      </LoadingScreen>
    </v-card>
    <SubjectDetailEditFAB v-if="userStore.user.isAdmin" />
  </div>
</template>