<script setup>
import {inject} from "vue";
import CourseUserList from '@/components/CourseUserList.vue'
import CourseUserListLeftDrawer from "@/components/course/CourseUserListLeftDrawer.vue";

const appState = inject('appState')
appState.value.leftDrawer = true

</script>

<template>
  <v-navigation-drawer v-model="appState.leftDrawer">
    <CourseUserListLeftDrawer :course-id="$route.params.course" />
  </v-navigation-drawer>
  <v-main>
    <CourseUserList :course-id="$route.params.course" />
  </v-main>
</template>
