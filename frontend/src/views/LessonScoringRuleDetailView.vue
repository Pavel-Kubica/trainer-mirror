<script setup>
import {inject, ref, provide} from 'vue'

import LessonDetailLeftDrawer from '@/components/lesson/LessonDetailLeftDrawer.vue'
import LessonCreateEditPartRules from "@/components/lesson/edit/LessonCreateEditPartRules.vue";

const lessonUsers = ref(null)
const modulesReloadHook = ref(0)
const file = ref([])
provide('modulesReloadHook', modulesReloadHook)
provide('lessonUsers', lessonUsers)
provide('fileDropped', file)

const appState = inject('appState')
appState.value.leftDrawer = true

</script>

<template>
  <v-navigation-drawer v-model="appState.leftDrawer">
    <LessonDetailLeftDrawer :lesson-id="$route.params.lesson" :user-id="$route.params.user" />
  </v-navigation-drawer>
  <v-main>
    <LessonCreateEditPartRules :lesson-id="$route.params.lesson" :user="$route.params.user" :edit="false" />
  </v-main>
</template>