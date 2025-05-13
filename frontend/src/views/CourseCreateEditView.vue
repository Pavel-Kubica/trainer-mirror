<script setup>
import {inject} from 'vue'
import CourseCreateEditLeftDrawer from "@/components/course/CourseCreateEditLeftDrawer.vue";
import CourseCreateEdit from "@/components/course/CourseCreateEdit.vue";


const appState = inject('appState')
appState.value.leftDrawer = true
</script>

<template>
  <v-navigation-drawer v-model="appState.leftDrawer">
    <CourseCreateEditLeftDrawer :course-id="$route.params.course" :semester-id="$route.params.semester" :subject-id="$route.params.subject" />
  </v-navigation-drawer>
  <v-main>
    <CourseCreateEdit v-if="$route.params.course" :course-id="$route.params.course" />
    <CourseCreateEdit v-else :semester-id="$route.params.semester" :subject-id="$route.params.subject" />
  </v-main>
</template>
