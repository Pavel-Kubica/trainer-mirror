<script setup>

import LoginForm from "@/components/LoginForm.vue";
import {inject, onMounted} from "vue";
import * as Nav from "@/service/nav";
const appState = inject('appState')
appState.value.leftDrawer = undefined

onMounted(() => {
  appState.value.navigation = [
    new Nav.Login()
  ]
})
</script>

<template>
  <v-main>
    <LoginForm />
  </v-main>
</template>