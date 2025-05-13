<script setup>
import { inject } from 'vue'
import LessonCreateEdit from '@/components/LessonCreateEdit.vue'

const appState = inject('appState')
appState.value.leftDrawer = true
</script>

<template>
  <LessonCreateEdit v-if="$route.params.lesson" :lesson-id="$route.params.lesson" />
  <LessonCreateEdit v-else :course-id="$route.params.course" :week-id="$route.params.week" />
</template>
