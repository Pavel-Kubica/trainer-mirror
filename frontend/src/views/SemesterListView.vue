<script setup>
import SemesterList from "@/components/SemesterList.vue";
import * as Nav from "@/service/nav";
import {inject} from 'vue'

const appState = inject('appState')
appState.value.leftDrawer = undefined

appState.value.navigation = [
  new Nav.CourseList(), new Nav.SemesterList()
]

</script>
<template>
  <v-main>
    <SemesterList />
  </v-main>
</template>