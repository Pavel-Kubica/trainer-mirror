<script setup>
import {inject} from "vue";
import * as Nav from "@/service/nav";
import BackToCourseListLeftDrawer from "@/components/guide/BackToCourseListLeftDrawer.vue";
import GuideList from "@/components/guide/GuideList.vue";

const appState = inject('appState')

appState.value.navigation = [new Nav.CourseList(), new Nav.Guide()]
appState.value.leftDrawer = true

const userStore = useUserStore()

import guides from "@/resources/guides.js"
import {useUserStore} from "@/plugins/store";

// Not really authorization, check this through API and don't serve the files if it's necessary to hide it for real
const accessibleGuides = guides.filter((g) => {
  switch (g.access) {
    case "ADMIN":
      return userStore.isAdmin
    case "GUARANTOR":
      return userStore.isGuarantor || userStore.isAdmin
    case "TEACHER":
      return userStore.isAnyTeacher || userStore.isGuarantor || userStore.isAdmin
    default:
      return true;
  }
})

</script>

<template>
  <v-navigation-drawer v-model="appState.leftDrawer">
    <BackToCourseListLeftDrawer />
  </v-navigation-drawer>
  <v-main>
    <GuideList :guides="accessibleGuides" />
  </v-main>
</template>


