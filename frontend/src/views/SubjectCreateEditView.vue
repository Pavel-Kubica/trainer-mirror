<script setup>
import {inject} from 'vue'
import {onBeforeRouteLeave} from 'vue-router'
import SubjectCreateEditLeftDrawer from "@/components/subjects/SubjectCreateEditLeftDrawer.vue";
import SubjectCreateEdit from "@/components/subjects/SubjectCreateEdit.vue";


const appState = inject('appState')



appState.value.leftDrawer = true
onBeforeRouteLeave((to) => {
  if (['subject-create','subject-edit'].includes(to.name)) return
  appState.value.leftDrawer = undefined
})
</script>

<template>
  <v-navigation-drawer v-model="appState.leftDrawer">
    <SubjectCreateEditLeftDrawer :semester-id="$route.params.semester" />
  </v-navigation-drawer>
  <v-main>
    <SubjectCreateEdit v-if="$route.params.subject" :subject-id="$route.params.subject" />
    <SubjectCreateEdit v-else />
  </v-main>
</template>
