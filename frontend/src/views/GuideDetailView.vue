<script setup>
import GuideDetailLeftDrawer from "@/components/guide/GuideDetailLeftDrawer.vue";
import GuideDetail from "@/components/guide/GuideDetail.vue";

import {inject} from "vue";
const appState = inject('appState')
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
    <GuideDetailLeftDrawer :guides="accessibleGuides" />
  </v-navigation-drawer>
  <v-main>
    <GuideDetail :guides="accessibleGuides" />
  </v-main>
</template>

