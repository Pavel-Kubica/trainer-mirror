<script setup>

import TopicList from "@/components/TopicList.vue";
import {inject, onMounted} from "vue";
import * as Nav from "@/service/nav";
const appState = inject('appState')
appState.value.leftDrawer = undefined

onMounted(() => {
  appState.value.navigation = [
    new Nav.CourseList(), new Nav.TopicList()
  ]
})
</script>

<template>
  <v-main>
    <TopicList />
  </v-main>
</template>