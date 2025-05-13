<script setup>
import { inject } from 'vue'
import router from '@/router'
import HideableAlert from '@/components/custom/HideableAlert.vue'
const appState = inject('appState')

const clickNotification = (notification, ix) => {
  hideNotification(notification, ix)
  if (notification.route)
    router.push(notification.route)
}
const hideNotification = (notification, index = -1) => {
  const ix = index === -1 ? appState.value.notifications.indexOf(notification) : index
  appState.value.notifications.splice(ix, 1)
}
</script>

<template>
  <div class="notification-list" style="position: fixed; top: 64px; right: 0; cursor: pointer; z-index: 9999">
    <HideableAlert v-for="(notification, ix) in appState.notifications" :key="notification.title"
                   :notification="notification" :on-click="() => clickNotification(notification, ix)"
                   :on-hide="() => hideNotification(notification)" :timeout="3500" />
  </div>
</template>
