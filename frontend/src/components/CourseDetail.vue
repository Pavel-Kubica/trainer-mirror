<script setup>
import { ref, inject, onMounted, watch, provide } from 'vue'
import { useLocale } from 'vuetify'
import { courseApi } from '@/service/api'
import * as Nav from '@/service/nav'

import LoadingScreen from '@/components/custom/LoadingScreen.vue'
import CourseDetailHeader from '@/components/course/CourseDetailHeader.vue'
import CourseDetailDeleteDialog from '@/components/course/CourseDetailDeleteDialog.vue'
import CourseDetailEditWeekDialog from '@/components/course/CourseDetailEditWeekDialog.vue'
import CourseDetailWeekRow from '@/components/course/CourseDetailWeekRow.vue'
import {useUserStore} from "@/plugins/store";
import CourseJoinCodeDialog from "@/components/course/CourseJoinCodeDialog.vue";
import CourseDetailImportDialog from "@/components/course/CourseDetailImportDialog.vue";

const userStore = useUserStore()
const appState = inject('appState')
const props = defineProps(['courseId', 'user'])
const { t } = useLocale()

const average = (array) => array.reduce((acc, lesson) => acc + lesson.progress, 0) / array.length
const isTeacher = ref(false)
provide('isTeacher', isTeacher)

const courseUser = ref(null)
provide('courseUser', courseUser)

const course = ref(null)
const error = ref(null)

const deleteDialog = ref(false)
const deleteLesson = ref(null)
const deleteWeek = ref(null)
provide('deleteDialog', deleteDialog)
provide('deleteLessonEntity', deleteLesson)
provide('deleteWeekEntity', deleteWeek)

const copyDialog = ref(false)
const copyTargetWeekId = ref(null)
provide('copyDialog', copyDialog)
provide('copyTargetWeekId', copyTargetWeekId)

const createEditDialog = ref(false)
const editWeek = ref(null)
provide('createEditDialog', createEditDialog)
provide('editWeekEntity', editWeek)

const joinCodeDialog = ref(false);
provide('joinCodeDialog', joinCodeDialog);

const loadCourseDetail = async () => {
  course.value = null; // Show loading
  courseApi.courseDetail(props.courseId)
      .then((result) => {
        appState.value.navigation = [new Nav.CourseList(), new Nav.CourseDetail(result)]
        course.value = result
        isTeacher.value = (result.role === 'TEACHER' || userStore.user.isAdmin || userStore.user.isGuarantor.includes(result.subject.id))
      })
      .catch((err) => { error.value = err.code })
}

onMounted(async () => { await loadCourseDetail() })
watch(props, async () => { await loadCourseDetail() })
</script>

<template>
  <CourseDetailDeleteDialog :delete-lesson="deleteLesson" :delete-week="deleteWeek" :callback="loadCourseDetail" />
  <CourseDetailImportDialog :target-week-id="copyTargetWeekId" :target-course-id="$route.params.course" :callback="loadCourseDetail" />
  <CourseDetailEditWeekDialog :course-id="courseId" :course="course" :week="editWeek" :callback="loadCourseDetail" />
  <CourseJoinCodeDialog :course-id="courseId" />
  <v-card :title="course?.name">
    <template v-if="course" #append>
      <CourseDetailHeader v-if="isTeacher" :course="course" />
    </template>
    <LoadingScreen :items="course" :error="error">
      <template #prepend>
        <v-progress-linear v-if="!isTeacher" color="rgb(var(--v-theme-progress))" :model-value="average(course.weeks.flatMap((w) => w.lessons))" />
      </template>
      <template #content>
        <span v-if="isTeacher && !userStore.anonymous">
          <v-btn variant="tonal" density="comfortable"
                 @click="() => { editWeek = null; createEditDialog = true }">
            {{ t('$vuetify.week_new') }}
          </v-btn>
          <v-btn variant="tonal" density="comfortable" class="ml-5"
                 @click="copyTargetWeekId = undefined; copyDialog = true;">
            {{ t('$vuetify.week_copy') }}
          </v-btn>
        </span>
        <v-table class="mt-2 mr-2">
          <CourseDetailWeekRow v-for="week in course.weeks" :key="week.id" :week="week" :is-teacher="isTeacher" />
          <span v-if="!course.weeks.length">
            {{ t('$vuetify.course_detail_no_weeks') }}
          </span>
        </v-table>
      </template>
    </LoadingScreen>
  </v-card>
</template>
