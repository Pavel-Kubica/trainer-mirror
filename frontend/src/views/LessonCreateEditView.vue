<script setup>
import { inject } from 'vue'
import { onBeforeRouteLeave } from 'vue-router'
import LessonCreateEdit from '@/components/LessonCreateEdit.vue'

const appState = inject('appState')
appState.value.leftDrawer = true
onBeforeRouteLeave((to) => {
  if (['lesson-create', 'lesson-edit'].includes(to.name)) return
  appState.value.leftDrawer = undefined
  appState.value.rightDrawer = undefined
})
</script>

<template>
  <LessonCreateEdit v-if="$route.params.lesson" :lesson-id="$route.params.lesson" />
  <LessonCreateEdit v-else :course-id="$route.params.course" :week-id="$route.params.week" />
</template>
