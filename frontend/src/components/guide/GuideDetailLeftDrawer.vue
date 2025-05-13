<script setup>
import * as Nav from '@/service/nav'
import {ref} from "vue";
import { getErrorMessage } from '@/plugins/constants'
import guides from "@/resources/guides.js"

const error = ref(null)

import { useLocale } from 'vuetify'
import {useUserStore} from "@/plugins/store";
const { t } = useLocale()


const userStore = useUserStore();
const getGuideName = (guide) => {
  return userStore.locale === 'customEn' ? guide.nameEn : guide.nameCz;
};

</script>

<template>
  <v-card class="mx-auto">
    <v-list density="comfortable">
      <v-list-item :to="new Nav.Guide().routerPath()">
        <template #prepend>
          <v-icon size="small" icon="mdi-arrow-left" />
        </template>
        <template #title>
          <strong>{{ t('$vuetify.guide_list') }}</strong>
        </template>
      </v-list-item>
      <v-list-item
        v-for="guide in guides" :key="guide.id"
        :title="getGuideName(guide)"
        :to="new Nav.GuideDetail(guide).routerPath()" />
      <v-list-item v-if="error" :title="getErrorMessage(t, error)" />
    </v-list>
  </v-card>
</template>