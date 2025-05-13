<script setup>
import { ref, inject, onMounted, watch, provide } from 'vue'
import { useLocale } from 'vuetify'
import { courseApi, courseUserApi } from '@/service/api'
import * as Nav from '@/service/nav'

import LoadingScreen from '@/components/custom/LoadingScreen.vue'
import CourseDetailHeader from '@/components/course/CourseDetailHeader.vue'
import CourseDetailDeleteDialog from '@/components/course/CourseDetailDeleteDialog.vue'
import CourseDetailCopyDialog from '@/components/course/CourseDetailCopyDialog.vue'
import CourseDetailEditWeekDialog from '@/components/course/CourseDetailEditWeekDialog.vue'
import CourseDetailWeekRow from '@/components/course/CourseDetailWeekRow.vue'
import {useUserStore} from "@/plugins/store";

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
const copyLesson = ref(null)
const copyWeek = ref(null)
provide('copyDialog', copyDialog)
provide('copyLessonEntity', copyLesson)
provide('copyWeekEntity', copyWeek)

const createEditDialog = ref(false)
const editWeek = ref(null)
provide('createEditDialog', createEditDialog)
provide('editWeekEntity', editWeek)

const loadCourseDetail = async () => {
  if (props.user) {
    courseUserApi.courseUserDetail(props.courseId, props.user)
        .then((result) => {
          appState.value.navigation = [
            new Nav.CourseList(), new Nav.CourseDetail(result.course), new Nav.CourseUserList(result.course),
            new Nav.CourseUserDetail(result.course, result.user)
          ]
          courseUser.value = result.user
          isTeacher.value = false
          course.value = result.course
          console.log("course - ", course.value)
        })
        .catch((err) => { error.value = err.code })
  }
  else courseApi.courseDetail(props.courseId)
      .then((result) => {
        appState.value.navigation = [new Nav.CourseList(), new Nav.CourseDetail(result)]
        course.value = result
        console.log("course - ", course.value)
        isTeacher.value = (result.role === 'TEACHER' || userStore.user.isAdmin || userStore.user.isGuarantor.includes(result.subject.id))
      })
      .catch((err) => { error.value = err.code })
}

onMounted(async () => { await loadCourseDetail() })
watch(props, async () => { await loadCourseDetail() })
</script>

<template>
  <CourseDetailDeleteDialog :delete-lesson="deleteLesson" :delete-week="deleteWeek" :callback="loadCourseDetail" />
  <CourseDetailCopyDialog :course="course" :lesson="copyLesson" :week="copyWeek" />
  <CourseDetailEditWeekDialog :course-id="courseId" :course="course" :week="editWeek" :callback="loadCourseDetail" />
  <v-card :title="t('$vuetify.course_detail_title')">
    <template v-if="course" #append>
      <CourseDetailHeader v-if="isTeacher" :course="course" />
    </template>
    <LoadingScreen :items="course" :error="error">
      <template #prepend>
        <v-progress-linear v-if="!isTeacher" color="rgb(var(--v-theme-progress))" :model-value="average(course.weeks.flatMap((w) => w.lessons))" />
      </template>
      <template #content>
        <v-table :class="isTeacher ? 'mt-0' : 'mt-4'">
          <CourseDetailWeekRow v-for="week in course.weeks" :key="week.id" :week="week" :is-teacher="isTeacher" />
          <span v-if="!course.weeks.length">
            {{ t('$vuetify.course_detail_no_weeks') }}
          </span>
        </v-table>
      </template>
    </LoadingScreen>
  </v-card>
</template>
