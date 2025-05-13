<script setup>
import LogList from "@/components/LogList.vue";
import * as Nav from "@/service/nav";
import {inject} from 'vue'

const appState = inject('appState')
appState.value.leftDrawer = undefined
appState.value.navigation = [
  new Nav.CourseList(), new Nav.LogList()
]

</script>
<template>
  <v-main>
    <LogList />
  </v-main>
</template>