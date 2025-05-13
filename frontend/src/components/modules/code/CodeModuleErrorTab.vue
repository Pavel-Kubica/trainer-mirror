<script setup>
import { inject } from 'vue'
import { useLocale } from 'vuetify'
import { getErrorMessage, TAB_MODULE_DETAIL } from '@/plugins/constants'

defineProps(['error'])
const { t } = useLocale()
const tabList = inject('tabList')
const selectedTab = inject('selectedTab')
tabList.value[TAB_MODULE_DETAIL] = t('$vuetify.tab_error')
selectedTab.value = TAB_MODULE_DETAIL
</script>

<template>
  <v-window-item :key="TAB_MODULE_DETAIL" :transition="false" :reverse-transition="false" :value="TAB_MODULE_DETAIL">
    <v-card class="mb-2 pa-2">
      <h3>{{ getErrorMessage(t, error) }}</h3>
    </v-card>
  </v-window-item>
</template>
