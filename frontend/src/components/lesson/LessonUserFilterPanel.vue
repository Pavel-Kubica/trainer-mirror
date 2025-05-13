<script setup>
import { useUserStore } from '@/plugins/store'
import { useLocale } from 'vuetify'

const { t } = useLocale()
const userStore = useUserStore()
</script>

<template>
  <span>
    <v-switch v-model="userStore.solvedFilter" density="compact" color="primary" label="✅️" hide-details />
    <v-tooltip activator="parent" location="top">{{ t('$vuetify.lesson_user_list_filter_completed') }}</v-tooltip>
  </span>
  <span>
    <v-switch v-model="userStore.requestedHelpFilter" density="compact" color="primary" label="😿⬆️" hide-details />
    <v-tooltip activator="parent" location="top">{{ t('$vuetify.lesson_user_list_filter_requests') }}</v-tooltip>
  </span>
  <span>
    <v-checkbox-btn v-model="userStore.allowedShowFilter" color="primary" label="👁" hide-details />
    <v-tooltip activator="parent" location="top">{{ t('$vuetify.lesson_user_list_filter_allowed_show') }}</v-tooltip>
  </span>
</template>
