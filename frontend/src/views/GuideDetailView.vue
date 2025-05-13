<script setup>
import GuideDetailLeftDrawer from "@/components/guide/GuideDetailLeftDrawer.vue";
import GuideDetail from "@/components/guide/GuideDetail.vue";
</script>

<template>
  <v-navigation-drawer>
    <GuideDetailLeftDrawer />
  </v-navigation-drawer>
  <v-main>
    <GuideDetail />
  </v-main>
</template>

