<script setup>
import {onMounted, inject, ref, provide} from 'vue'
import {courseApi} from '@/service/api'
import * as Nav from '@/service/nav'
import router from '@/router'

import LoadingScreen from '@/components/custom/LoadingScreen.vue'
import CourseListRow from '@/components/course/CourseListRow.vue'
import CourseSecretDialog from '@/components/course/CourseSecretDialog.vue'
import CourseListFilterMenu from '@/components/course/CourseListFilterMenu.vue'

const appState = inject('appState')
const courses = ref(null)
const error = ref(null)

const courseSecretDialog = ref(false)
provide('courseSecretDialog', courseSecretDialog)

import {useLocale} from 'vuetify'

const {t} = useLocale()

const semesters = ref([])
const semester = ref(null)
provide('semesters', semesters)
provide('semester', semester)

onMounted(async () => {
  courseApi.listCourses()
      .then((result) => {
        appState.value.navigation = [new Nav.CourseList()]
        semesters.value = [...new Map(result.map((c) => [c.semester?.id, c.semester])).values()].filter((s) => s)
        semester.value = semesters.value[0]
        semesters.value.unshift(null)
        courses.value = result
      })
      .catch((err) => {
        error.value = err.code
      })
})
</script>

<template>
  <CourseSecretDialog :title="t('$vuetify.course_join')" :text="t('$vuetify.course_join_short')"
                      :method="(secret) => courseApi.joinCourse(secret)"
                      :action="(result) => router.push(new Nav.CourseDetail(result).routerPath())" />
  <v-card class="mx-8 my-4">
    <v-card-title class="d-flex justify-space-between flex-wrap" style="gap: 16px">
      <CourseListFilterMenu />
      <a @click="courseSecretDialog = true">
        <v-btn>{{ t('$vuetify.course_join') }}</v-btn>
      </a>
    </v-card-title>
    <LoadingScreen :items="courses" :error="error">
      <template #table>
        <CourseListRow v-for="course in courses.filter((c) => !semester || c.semester?.id === semester?.id)" :key="course.id"
                       :course="course" />
        <tr v-if="!courses.length">
          <td>{{ t('$vuetify.course_list_empty') }}</td>
        </tr>
      </template>
    </LoadingScreen>
  </v-card>
</template>
