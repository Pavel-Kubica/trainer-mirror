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
    await Promise.all([userApi.getUserById(store.user.id), subjectApi.listMeGuarantor(), userApi.getIsTeacher()]).then((result) => {
      const [user, isGuarantor, isTeacher] = result;
      store.realUser.isAdmin = user.isAdmin;
      store.realUser.isGuarantor = isGuarantor;
      store.realUser.isTeacher = isTeacher;
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