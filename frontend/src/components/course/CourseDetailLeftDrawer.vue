<script setup>
import { onMounted, ref } from 'vue'
import { getErrorMessage } from '@/plugins/constants'
import * as Nav from '@/service/nav'
import { courseApi } from '@/service/api'

defineProps(['courseId'])
const courses = ref([])
const error = ref(null)

import { useLocale } from 'vuetify'
const { t } = useLocale()

onMounted(async () => {
  courseApi.listCourses()
      .then((result) => { courses.value = result })
      .catch((err) => { error.value = err.code })
})
</script>

<template>
  <v-card class="mx-auto">
    <v-list density="comfortable">
      <v-list-item :to="new Nav.CourseList().routerPath()">
        <template #prepend>
          <v-icon size="small" icon="mdi-arrow-left" />
        </template>
        <template #title>
          <strong>{{ t('$vuetify.course_detail_left_drawer_course_list') }}</strong>
        </template>
      </v-list-item>
      <v-list-item
        v-for="course in courses" :key="course.id" :active="`${course.id}` === courseId"
        :title="`${course.shortName}${course.semester ? ` (${course.semester.code})` : ''}`"
        :to="new Nav.CourseDetail(course).routerPath()" />
      <v-list-item v-if="error" :title="getErrorMessage(t, error)" />
    </v-list>
  </v-card>
</template>
