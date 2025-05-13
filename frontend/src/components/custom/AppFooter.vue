<script setup>
import { useLocale } from 'vuetify'
import {HIDE_PRESENTATION} from "@/plugins/constants";

const { t } = useLocale()
</script>

<template>
  <v-footer app>
    <div class="d-flex justify-space-between" style="width: 100%">
      <span>
        <v-label>{{ t('$vuetify.trainer_footer_title') }}</v-label>
        <router-link v-if="!HIDE_PRESENTATION" :to="{ name: 'presentation' }" class="ml-2 text-decoration-none text-grey">
          <v-icon>mdi-presentation</v-icon>
        </router-link>
      </span>
      <a href="https://gitlab.fit.cvut.cz/trainer-fit/feedback/issues/" target="_blank"
         class="text-decoration-none text-red">{{ t('$vuetify.trainer_footer_report') }}</a>
      <div class="d-flex justify-end">
        <v-label>{{ t('$vuetify.trainer_version') }}: ${BUILD_VERSION} (${BUILD_SHA})</v-label>
      </div>
    </div>
  </v-footer>
</template>
