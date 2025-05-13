<script setup>
import CourseUserImport from '@/components/CourseUserImport.vue'

import {inject} from "vue";
const appState = inject('appState')
appState.value.leftDrawer = undefined
</script>

<template>
  <v-main>
    <CourseUserImport :course="$route.params.course" />
  </v-main>
</template>
