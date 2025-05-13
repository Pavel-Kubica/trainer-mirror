<script setup>
import NotificationList from '@/components/NotificationList.vue'
import { onMounted, inject } from 'vue'
import * as Nav from '@/service/nav'

const appState = inject('appState')
appState.value.leftDrawer = undefined

onMounted(() => {
  appState.value.navigation = [
    new Nav.CourseList(), new Nav.NotificationList()
  ]
})
</script>

<template>
  <v-main>
    <NotificationList :show-all="true" />
  </v-main>
</template>
