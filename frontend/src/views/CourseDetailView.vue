<script setup>
import { inject } from 'vue'
import { onBeforeRouteLeave } from 'vue-router'

import CourseDetail from '@/components/CourseDetail.vue'
import CourseDetailLeftDrawer from '@/components/course/CourseDetailLeftDrawer.vue'
import CourseUserListLeftDrawer from '@/components/course/CourseUserListLeftDrawer.vue'

const appState = inject('appState')
appState.value.leftDrawer = true
onBeforeRouteLeave((to) => {
  if (['course-detail', 'course-user'].includes(to.name)) return
  appState.value.leftDrawer = undefined
})
</script>

<template>
  <v-navigation-drawer v-model="appState.leftDrawer">
    <CourseUserListLeftDrawer v-if="$route.params.user" :course-id="$route.params.course"
                              :user-id="$route.params.user" />
    <CourseDetailLeftDrawer v-else :course-id="$route.params.course" />
  </v-navigation-drawer>
  <v-main>
    <CourseDetail :course-id="$route.params.course" :user="$route.params.user" />
  </v-main>
</template>
