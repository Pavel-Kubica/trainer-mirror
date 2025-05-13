<script setup>
import { onMounted, inject, ref } from 'vue'
import { useLocale } from 'vuetify'
import * as Nav from '@/service/nav'
import { courseApi, lessonApi, weekApi } from '@/service/api'
import router from '@/router'
import LoadingScreen from '@/components/custom/LoadingScreen.vue'

const appState = inject('appState')
const props = defineProps(['lesson', 'week', 'course'])
const copyDialog = inject('copyDialog')
const { t } = useLocale()

const courses = ref(null)
const error = ref(null)
const lastCourse = ref(null)

const translate = (key) => t(`$vuetify.course_detail_${props.lesson ? 'lesson' : 'week'}_copy_${key}`)
const copyToCourse = (course) => {
  error.value = null
  courses.value = null
  lastCourse.value = course
  const promise = props.lesson ? lessonApi.cloneLesson(props.lesson.id, course.id) : weekApi.cloneWeek(props.week.id, course.id)
  promise
      .then(async (clonedLesson) => {
        appState.value.notifications.push({
          type: "success", title: translate('success_title'), text: translate(`success_text`),
          route: props.lesson ? new Nav.LessonDetail(clonedLesson).routerPath() : undefined
        })
        copyDialog.value = false
        await resetDialog()
        await router.push(new Nav.CourseDetail(course).routerPath())
      })
      .catch((err) => { error.value = err.code })
}

const resetDialog = async () => {
  courses.value = null
  error.value = null
  lastCourse.value = null

  await courseApi.listCourses()
      .then((result) => { courses.value = result })
      .catch((err) => { error.value = err.code })
}

onMounted(async () => { await resetDialog() })
</script>

<template>
  <v-dialog v-model="copyDialog" width="auto">
    <v-card :title="translate('title')">
      <LoadingScreen :items="courses" :error="error">
        <template #table>
          <tr
            v-for="course in courses.filter((crs) => crs.role === 'TEACHER' && crs.id !== props.course?.id)"
            :key="course.id"
          >
            <td>{{ course.name }}</td>
            <td>{{ course.semester?.code }}</td>
            <td class="d-flex justify-end">
              <v-btn variant="text" icon @click="copyToCourse(course)">
                <v-icon icon="mdi-plus" />
                <v-tooltip activator="parent" location="top">
                  {{ translate('tooltip') }}
                </v-tooltip>
              </v-btn>
            </td>
          </tr>
        </template>
      </LoadingScreen>
      <v-card-actions>
        <div style="width: 100%" class="d-flex flex-column align-center pa-2">
          <v-btn v-if="lastCourse && error" class="mx-2" :block="true" variant="tonal" color="warning" @click="copyToCourse(lastCourse)">
            {{ t('$vuetify.dialog_try_again') }}
          </v-btn>
          <v-btn class="mt-2 me-2" :block="true" variant="tonal" color="info" @click="copyDialog = false">
            {{ t('$vuetify.dialog_close') }}
          </v-btn>
        </div>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>
