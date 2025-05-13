<script setup>
import LessonProgressOverview from '@/components/LessonProgressOverview.vue'

import {inject} from "vue";
const appState = inject('appState')
appState.value.leftDrawer = undefined
</script>

<template>
  <v-main>
    <LessonProgressOverview :lesson-id="$route.params.lesson" />
  </v-main>
</template>
