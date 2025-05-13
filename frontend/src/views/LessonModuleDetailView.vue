<script setup>
import {inject, ref, provide} from 'vue'
import {onBeforeRouteLeave} from 'vue-router'

import LessonModuleDetail from '@/components/LessonModuleDetail.vue'
import LessonDetailLeftDrawer from '@/components/lesson/LessonDetailLeftDrawer.vue'

const lessonUsers = ref(null)
const reloadHook = ref(0)
const file = ref([])
provide('reloadHook', reloadHook)
provide('lessonUsers', lessonUsers)
provide('fileDropped', file)

const appState = inject('appState')
appState.value.leftDrawer = true
onBeforeRouteLeave((to) => {
  if (['lesson-detail', 'lesson-user', 'lesson-module-detail', 'module-user-detail','scoring_rule_user_detail','scoring_rule_detail'].includes(to.name)) return
  appState.value.leftDrawer = undefined
  appState.value.rightDrawer = undefined
})

function onDrop(e) {
  e.preventDefault()
  file.value = e.dataTransfer.files
}

const dragOver = (e) => {
  e.preventDefault()
}

</script>

<template>
  <div style="min-height: 100%" @drop="onDrop" @dragover="dragOver">
    <v-navigation-drawer v-model="appState.leftDrawer">
      <LessonDetailLeftDrawer :lesson-id="$route.params.lesson" :module-id="$route.params.module" :scoring-rule-id="$route.params.scoringRule"
                              :user="$route.params.user" />
    </v-navigation-drawer>
    <LessonModuleDetail :lesson-id="$route.params.lesson" :module-id="$route.params.module" :scoring-rule-id="$route.params.scoringRule" :user="$route.params.user" />
  </div>
</template>
