<script setup>
import CourseList from '@/components/CourseList.vue'
import CourseLessonList from '@/components/course/CourseLessonList.vue'

import {inject} from "vue";
const appState = inject('appState')
appState.value.leftDrawer = undefined
</script>

<template>
  <v-main>
    <CourseLessonList />
    <CourseList />
  </v-main>
</template>
