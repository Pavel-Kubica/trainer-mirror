<script setup>
import {onMounted, ref} from 'vue'
import {getErrorMessage} from '@/plugins/constants'
import * as Nav from '@/service/nav'
import {courseApi} from '@/service/api'

const props = defineProps(['courseId', 'semesterId', 'subjectId'])
const courses = ref([])
const error = ref(null)

import {useLocale} from 'vuetify'

const {t} = useLocale()

onMounted(async () => {
  courseApi.semSubjectCourse(props.semesterId, props.subjectId)
      .then((result) => {
        courses.value = result
      })
      .catch((err) => {
        error.value = err.code
      })
})
</script>

<template>
  <v-card class="mx-auto">
    <v-list density="comfortable">
      <v-list-item :to="new Nav.SubjectsList(props.semesterId).routerPath()">
        <template #prepend>
          <v-icon size="small" icon="mdi-arrow-left" />
        </template>
        <template #title>
          <strong>{{ t('$vuetify.course_detail_left_drawer_course_list') }}</strong>
        </template>
      </v-list-item>
      <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0">
      <v-list-item
        :to="new Nav.CourseCreate(props.semesterId, props.subjectId).routerPath()">
        <template #title>
          <div style="display: flex; align-items: center; gap: 8px;">
            <span class="material-symbols-outlined">group</span>
            <strong>{{ t('$vuetify.course_create_new') }}</strong>
          </div>
        </template>
      </v-list-item>
      <v-list-item
        v-for="course in courses" :key="course.id" :active="`${course.id}` === courseId"
        :title="`${course.shortName} (${course.semester?.code})`"
        :to="new Nav.CourseEdit(course).routerPath()" />
      <v-list-item v-if="error" :title="getErrorMessage(t, error)" />
    </v-list>
  </v-card>
</template>
