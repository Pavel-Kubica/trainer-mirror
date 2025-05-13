<script setup>
import { onMounted, ref } from 'vue'
import { useLocale } from 'vuetify'
import { useUserStore } from '@/plugins/store'
import { notificationApi } from '@/service/api'
import NotificationList from '@/components/NotificationList.vue'

const { t } = useLocale()
const userStore = useUserStore()
const menu = ref(false)

onMounted(() => {
  notificationApi.getNotifications()
      .then((result) => userStore.setNotifications(result.length))
})
</script>

<template>
  <v-menu v-model="menu" location="bottom">
    <template #activator="{ props }">
      <v-btn icon v-bind="props">
        <v-badge v-if="userStore.notifications" color="warning" :content="userStore.notifications > 9 ? '9+' : userStore.notifications">
          <v-icon icon="mdi-bell" />
        </v-badge>
        <v-icon v-else icon="mdi-bell" />
        <v-tooltip activator="parent" location="bottom">
          {{ t('$vuetify.notification_menu_title') }}
        </v-tooltip>
      </v-btn>
    </template>
    <NotificationList />
  </v-menu>
</template>
