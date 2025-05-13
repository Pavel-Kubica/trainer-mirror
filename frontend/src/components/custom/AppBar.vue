<script setup>
import {useUserStore} from '@/plugins/store'
import {inject, onMounted, watch} from 'vue'
import NotificationMenu from '@/components/menu/NotificationMenu.vue'
import UserMenu from '@/components/menu/UserMenu.vue'
import * as Nav from '@/service/nav'
import {isHalloween, isChristmas, anonEmoji, HIDE_GUIDES} from '@/plugins/constants'
import {useLocale} from 'vuetify'
import {semesterApi} from "@/service/api";

const appState = inject('appState')
const userStore = useUserStore()
const { t, current } = useLocale()
const store = useUserStore()

const anonymizeName = (item) => (item.anonymous && userStore.anonymous ? anonEmoji(item.anonymous) : t(item.name))
const callback = (state) => {
  document.title = 'Trainer' + (state.navigation.length > 1 ? ` | ${anonymizeName(state.navigation.slice(-1)[0])}` : '')
}

const toggleLocale = () => {
  userStore.toggleLocale()
  current.value = userStore.locale
}

onMounted(async () => {
  if (store.isLoggedIn)
    semesterApi.listSemesters()
        .then((result) => {
          if (userStore.isGuarantor)
            userStore.semester = result[0].id
        })
})

watch(appState, callback, {deep: true})
</script>

<template>
  <v-app-bar
    :color="isHalloween ? 'orange' : (isChristmas ? 'green-darken-4' : (store.isLoggedIn && userStore.isAdmin ? '#01013F' : (store.isLoggedIn && userStore.isAnyTeacher ? 'purple' : 'indigo')))"
    prominent>
    <v-btn v-if="appState.leftDrawer !== undefined" icon="mdi-menu"
           @click="appState.leftDrawer = !appState.leftDrawer" />
    <v-btn icon="mdi-timer-outline" :to="{name: 'course-list'}" :active="false" />

    <v-toolbar-title>
      <router-link v-for="(item, index) in appState.navigation" :key="item.name"
                   class="text-decoration-none"
                   :to="{name: item.path, params: item.params}" :style="`color: ${isHalloween ? 'black' : 'white'}`">
        {{ anonymizeName(item) + (index !== (appState.navigation.length - 1) ? ' > ' : '') }}
      </router-link>
    </v-toolbar-title>

    <template v-if="store.isLoggedIn">
      <v-menu v-if="userStore.isAdmin || userStore.isGuarantor" open-on-hover>
        <template #activator="{ props }">
          <v-btn color="white" v-bind="props" dark>
            <span v-if="userStore.isAdmin">{{ t('$vuetify.user_menu_admin_button') }}</span>
            <span v-else-if="userStore.isGuarantor">{{ t('$vuetify.user_menu_guarantor_button') }}</span>
          </v-btn>
        </template>
        <v-list>
          <v-list-item v-if="userStore.isAdmin" :to="{name: 'semester-list'}">
            <v-list-item-title>
              {{ t('$vuetify.semester_title') }}
            </v-list-item-title>
          </v-list-item>

          <v-list-item v-if="userStore.isAdmin || userStore.isGuarantor" :to="new Nav.SubjectsList(userStore.semester).routerPath()">
            <v-list-item-title>
              {{ t('$vuetify.subject_list') }}
            </v-list-item-title>
          </v-list-item>

          <v-list-item v-if="userStore.isAdmin || userStore.isGuarantor" :to="{name: 'topic-list'}">
            <v-list-item-title>
              {{ t("$vuetify.topic_list_title") }}
            </v-list-item-title>
          </v-list-item>
        </v-list>
      </v-menu>
      <NotificationMenu />

      <v-btn v-if="!HIDE_GUIDES" :to="{name: 'guide'}" icon>
        <v-icon icon="mdi-help" />
        <v-tooltip activator="parent" location="bottom">
          {{ t('$vuetify.guides') }}
        </v-tooltip>
      </v-btn>

      <UserMenu :callback="callback" :is-halloween="isHalloween" />
    </template>
    <template v-else>
      <v-btn class="mx-2" @click="toggleLocale">
        {{ t('$vuetify.user_menu_switch_language') }}
      </v-btn>
      <v-btn :to="{name: 'login'}" class="mr-4">
        {{ t('$vuetify.login') }}
      </v-btn>
    </template>
  </v-app-bar>
</template>
