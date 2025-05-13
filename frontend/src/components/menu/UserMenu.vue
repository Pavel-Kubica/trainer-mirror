<script setup>
import {useUserStore} from '@/plugins/store'
import {inject, provide, ref} from 'vue'
import {useLocale} from 'vuetify'
import {anonEmoji} from '@/plugins/constants'
import {courseApi, userApi} from "@/service/api";
import * as Nav from "@/service/nav";
import router from "@/router";
import GitlabTokenUpdateMenu from "@/components/gitlab/GitlabTokenUpdateMenu.vue";
const {t, current} = useLocale()

const appState = inject('appState')
const userStore = useUserStore()
const displayGitlabTokenUpdateDialog = ref(false)
defineProps(['isHalloween', 'callback'])

let sandboxCourse = ref(null)

const toggleLocale = () => {
  userStore.toggleLocale()
  current.value = userStore.locale
}

const goToSandbox = async () => {
  sandboxCourse.value = await userApi.getSandbox(userStore.user.id);
  if (!sandboxCourse.value) {
    sandboxCourse.value = await courseApi.createSandbox(userStore.user.id)
  }
  console.log(sandboxCourse.value)
  await router.push(new Nav.CourseDetail(sandboxCourse.value).routerPath())
}

const logout = () => {
  userStore.logout()
  router.go(0) // reload the page
}

provide('displayGitlabTokenUpdateDialog', displayGitlabTokenUpdateDialog)

</script>

<template>
  <GitlabTokenUpdateMenu />
  <v-menu location="bottom end" :close-on-content-click="false">
    <template #activator="{ props }">
      <v-btn append-icon="mdi-chevron-down" :color="isHalloween ? 'black' : 'white'" v-bind="props">
        {{ userStore.user.username + (userStore.anonymous ? ` ${anonEmoji(userStore.user.username)}` : '') }}
      </v-btn>
    </template>
    <v-list>
      <v-list-item @click="displayGitlabTokenUpdateDialog = true">
        {{ t('$vuetify.user_menu_gitlab_token') }}
      </v-list-item>
      <v-list-item v-if="userStore.isAnyTeacher" @click="userStore.toggleAnonymous(); callback(appState)">
        <v-switch v-model="userStore.anonymous" density="compact" color="primary" class="me-2 v-input--reverse" :hide-details="true">
          <template #prepend>
            {{ t('$vuetify.user_menu_anonymous_mode') }}
          </template>
        </v-switch>
      </v-list-item>
      <v-list-item @click="userStore.toggleDarkMode()">
        <v-switch v-model="userStore.darkMode" density="compact" color="primary" class="me-2 v-input--reverse" :hide-details="true">
          <template #prepend>
            {{ t('$vuetify.user_menu_dark_mode') }}
          </template>
        </v-switch>
      </v-list-item>
      <v-list-item @click="toggleLocale">
        {{ t('$vuetify.user_menu_switch_language') }}
      </v-list-item>
      <v-list-item v-if="userStore.isAdmin || userStore.isAnyTeacher || userStore.isGuarantor || userStore.isTestUser"
                   @click="userStore.swapIdentity(); $router.go(0)">
        {{ t('$vuetify.user_menu_switch_account') }}
      </v-list-item>
      <v-list-item v-if="userStore.isAdmin || userStore.isAnyTeacher || userStore.isGuarantor" @click="goToSandbox">
        {{ t('$vuetify.user_menu_sandbox_button') }}
      </v-list-item>
      <v-list-item @click="logout">
        {{ t('$vuetify.user_menu_logout') }}
      </v-list-item>
    </v-list>
  </v-menu>
</template>
