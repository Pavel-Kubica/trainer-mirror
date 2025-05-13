<script setup>
import {inject} from "vue";
import * as Nav from "@/service/nav";
import BackToCourseListLeftDrawer from "@/components/guide/BackToCourseListLeftDrawer.vue";
import GuideList from "@/components/guide/GuideList.vue";

const appState = inject('appState')

appState.value.navigation = [new Nav.CourseList(), new Nav.Guide()]

</script>

<template>
  <v-navigation-drawer>
    <BackToCourseListLeftDrawer />
  </v-navigation-drawer>
  <v-main>
    <GuideList />
  </v-main>
</template>


