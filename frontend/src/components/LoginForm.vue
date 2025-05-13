<script setup>
import { ref, onMounted } from 'vue'
import { useLocale } from 'vuetify'
import { useUserStore } from '@/plugins/store'
import router from '@/router'
import { authApi } from '@/service/api'
import { CourseList } from '@/service/nav'
import {HIDE_PRESENTATION} from "@/plugins/constants";

const { t } = useLocale()
const settings = ref(null)
const loading = ref(true)

const login = async () => {
  if (!settings.value)
    return
  window.location.href = 'https://auth.fit.cvut.cz/oauth/authorize?response_type=code' +
      `&client_id=${settings.value.clientId}` +
      `&redirect_uri=${settings.value.redirectUri}` +
      '&scope=cvut:umapi:read'
}

onMounted(async () => {
  const urlParams = new URLSearchParams(window.location.search)
  const codeParam = urlParams.get('code')

  if (!codeParam) {
    settings.value = await authApi.getOAuthSettings()
    loading.value = false
    return
  }

  authApi.login({ code: codeParam })
      .then((userLogin) => {
        if (!userLogin.username || !userLogin.name) {
          window.location.href = '/' // error
          return
        }

        let store = useUserStore()
        store.setUser(userLogin)
        router.push(new CourseList().routerPath())
      })
      .catch(() => window.location.href = '/')
})
</script>

<script>
export default {
  data() {
    return {
      showDevModeWarning: process.env.VUE_APP_IS_DEV ?? false
    }
  },
  methods: {
    closeShowDevModeWarning() {
      this.showDevModeWarning = false
    }
  }
}
</script>

<template>
  <div v-if="!loading && showDevModeWarning" class="fullscreen-sheet">
    <h1 class="ps-4 text-center">
      {{ t('$vuetify.dev_version_notofication') }}
    </h1>
    <v-btn class="mt-8 mb-1" color="white" size="large" variant="tonal"
           @click="closeShowDevModeWarning">
      {{ t('$vuetify.dialog_close') }}
    </v-btn>
  </div>

  <div>
    <v-card class="mx-auto my-12 pa-12 pb-8" elevation="8" max-width="448" rounded="lg">
      <div class="d-flex justify-center align-center">
        <v-icon size="large" icon="mdi-timer-outline" />
        <h1 class="ps-4 text-center">
          Trainer
        </h1>
      </div>

      <div class="d-flex flex-column">
        <div v-if="loading" class="d-flex mt-8 mb-6 justify-center">
          <v-progress-circular indeterminate />
        </div>
        <v-btn v-else :block="true" class="mt-8 mb-1" color="blue" size="large" variant="tonal"
               @click="login">
          {{ t('$vuetify.login_ctu') }}
        </v-btn>

        <v-btn v-if="!HIDE_PRESENTATION" :to="{name: 'presentation'}" class="mt-3 mb-6" color="grey" size="large"
               variant="tonal">
          {{ t('$vuetify.presentation_link') }}
        </v-btn>
      </div>
    </v-card>
  </div>
</template>

<style>
.fullscreen-sheet {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(0, 0, 0, 0.8);
  color: white;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}
</style>