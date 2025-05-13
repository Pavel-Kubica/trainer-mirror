<script setup>
import {useTheme, useLocale} from 'vuetify'
import {watch, ref, provide, onMounted} from 'vue'

import {CourseList} from '@/service/nav'
import {useUserStore} from './plugins/store'
import {isHalloween, isChristmas} from '@/plugins/constants'

import CustomAppBar from '@/components/custom/AppBar.vue'
import CustomFooter from '@/components/custom/AppFooter.vue'
import NotificationSnackbar from '@/components/custom/NotificationSnackbar.vue'
import {subjectApi, userApi} from "@/service/api";

const appState = ref({
  navigation: [new CourseList()],
  notifications: [],
})

console.log("Init app")
const store = useUserStore()

if (isHalloween && !store.originalMode) {
  store.originalMode = store.darkMode ? 'dark' : 'light'
}

if (!isHalloween && store.originalMode) {
  store.darkMode = store.originalMode === 'dark'
  store.originalMode = ''
}

const light = isChristmas ? 'christmasLight' : 'customLight'
const dark = isChristmas ? 'christmasDark' : 'customDark'

const theme = useTheme()
theme.global.name.value = store.darkMode ? (isHalloween ? 'halloween' : dark) : light

const {current} = useLocale()
current.value = store.locale
watch(
    store.$state,
    () => {
      theme.global.name.value = store.darkMode ? (isHalloween ? 'halloween' : dark) : light
    },
    {deep: true}
)

provide('appState', appState)

onMounted(async () => {
  if (store.user !== null) {
    console.log("store.user - ", store.user)
    await userApi.getUserById(store.user.id).then((result) => {
      store.realUser.isAdmin = result.isAdmin
    })
    await subjectApi.listMeGuarantor().then((result) => {
      store.realUser.isGuarantor = result
    })
    await userApi.getIsTeacher().then((result) => {
      console.log("Is teacher: " + result)
      store.realUser.isTeacher = result
    })
  }
  if (store.isLoggedIn && store.realUser.username === store.user.username) {
    store.user = store.realUser
  }
})
</script>

<template>
  <v-app>
    <CustomAppBar />
    <router-view />
    <NotificationSnackbar />
    <CustomFooter />
  </v-app>
</template>

<style src="../public/css/style.css"></style>