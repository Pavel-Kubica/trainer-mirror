<script setup>
import {inject, ref, provide} from 'vue'

import LessonDetailLeftDrawer from '@/components/lesson/LessonDetailLeftDrawer.vue'
import LessonCreateEditPartRules from "@/components/lesson/edit/LessonCreateEditPartRules.vue";
import {onBeforeRouteLeave} from "vue-router";

const lessonUsers = ref(null)
const reloadHook = ref(0)
const file = ref([])
provide('reloadHook', reloadHook)
provide('lessonUsers', lessonUsers)
provide('fileDropped', file)

const appState = inject('appState')
appState.value.leftDrawer = true
onBeforeRouteLeave((to) => {
  if (['lesson_scoring_rule_list'].includes(to.name)) return
  appState.value.leftDrawer = undefined
})

</script>

<template>
  <v-navigation-drawer v-model="appState.leftDrawer">
    <LessonDetailLeftDrawer :lesson-id="$route.params.lesson" :user="$route.params.user" />
  </v-navigation-drawer>
  <v-main>
    <LessonCreateEditPartRules :lesson-id="$route.params.lesson" :user="$route.params.user" :edit="false" />
  </v-main>
</template>