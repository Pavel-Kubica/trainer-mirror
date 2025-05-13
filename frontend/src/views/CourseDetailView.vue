<script setup>
import { inject } from 'vue'

import CourseDetail from '@/components/CourseDetail.vue'
import CourseDetailLeftDrawer from '@/components/course/CourseDetailLeftDrawer.vue'

const appState = inject('appState')
appState.value.leftDrawer = true
</script>

<template>
  <v-navigation-drawer v-model="appState.leftDrawer">
    <CourseDetailLeftDrawer :course-id="$route.params.course" />
  </v-navigation-drawer>
  <v-main>
    <CourseDetail :course-id="$route.params.course" :user="$route.params.user" />
  </v-main>
</template>
