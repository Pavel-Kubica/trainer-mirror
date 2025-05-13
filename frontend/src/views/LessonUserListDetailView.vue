<script setup>
import { inject, ref, provide } from 'vue'
import { onBeforeRouteLeave } from 'vue-router'

import LessonModuleDetail from '@/components/LessonModuleDetail.vue'
import LessonUserListLeftDrawer from '@/components/lesson/LessonUserListLeftDrawer.vue'

const reloadHook = ref(0)
const lessonUsers = ref(null)
provide('reloadHook', reloadHook)
provide('lessonUsers', lessonUsers)

const appState = inject('appState')
appState.value.leftDrawer = true
onBeforeRouteLeave((to) => {
  if (['lesson-module-user'].includes(to.name)) return
  appState.value.leftDrawer = undefined
  appState.value.rightDrawer = undefined
})
</script>

<template>
  <v-navigation-drawer v-model="appState.leftDrawer" width="300">
    <LessonUserListLeftDrawer :lesson-id="$route.params.lesson" :module-id="$route.params.module" :user="$route.params.user" />
  </v-navigation-drawer>
  <LessonModuleDetail :lesson-id="$route.params.lesson" :module-id="$route.params.module" :scoring-rule-id="$route.params.scoringRule"
                      :user="$route.params.user" />
</template>
