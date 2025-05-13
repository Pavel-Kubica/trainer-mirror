<script setup>
import * as Nav from '@/service/nav'
import {ref} from "vue";
import { getErrorMessage } from '@/plugins/constants'

defineProps(['guides'])

const error = ref(null)

import { useLocale } from 'vuetify'
const { t } = useLocale()

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
        :title="t(guide.name)"
        :to="new Nav.GuideDetail(guide).routerPath()" />
      <v-list-item v-if="error" :title="getErrorMessage(t, error)" />
    </v-list>
  </v-card>
</template>