<script setup>
import { onMounted, ref } from 'vue'
import { useLocale } from 'vuetify'
import { anonEmoji } from '@/plugins/constants'
import { useUserStore } from '@/plugins/store'
import { notificationApi } from '@/service/api'
import * as Nav from '@/service/nav'
import router from '@/router'
import LoadingScreen from "@/components/custom/LoadingScreen.vue";

const { t } = useLocale()
const props = defineProps(['showAll'])
const userStore = useUserStore()

const notifications = ref(null)
const error = ref(null)

const getNotificationLink = (notification) => {
  if (notification.isStudent)
    return new Nav.ModuleDetail(notification.lesson, notification.module)
  else
    return new Nav.LessonSolutionsModule(notification.lesson, notification.module, notification.user)
}

const getNotificationText = (notification) => {
  const name = userStore.anonymous ? anonEmoji(notification.user.username) : notification.user.name
  const prefix = !notification.isStudent && notification.satisfied ? '<span style="color: gray">' : ''
  const suffix = !notification.isStudent && notification.satisfied ? '</span>' : ''
  const type = notification.type.toLowerCase()
  const student = notification.isStudent ? "" : "_teacher"
  const escapedComment = notification.comment.replace("<", "&lt;").replace(">", "&gt;").replace('"', "&quot;").replace("'", "&#39;")
  return prefix + t(`$vuetify.notification_${type}${student}`, name, notification.module.name, escapedComment ?? '') + suffix
}

const markAllRead = async () => {
  await notificationApi.deleteNotifications()
      .then(() => {
        userStore.setNotifications(0)
        if (!props.showAll)
          notifications.value = []
      })
      .catch((err) => { error.value = err.code })
}

onMounted(async () => {
  await (props.showAll ? notificationApi.getAllNotifications() : notificationApi.getNotifications())
      .then((result) => {
        if (!props.showAll)
          userStore.setNotifications(result.length)
        notifications.value = result
      })
      .catch((err) => { error.value = err })
  if (props.showAll)
    await markAllRead()
})
</script>

<template>
  <v-card min-width="300">
    <LoadingScreen :items="notifications" :error="error">
      <template #content>
        <v-card-title>{{ t('$vuetify.notification_menu_title') }}</v-card-title>
        <v-switch v-if="showAll && userStore.isAnyTeacher" v-model="userStore.hideSatisfiedNotifications" hide-details class="ps-2"
                  color="purple"
                  :label="t(`$vuetify.notification_show_satisfied`)" />
        <v-list>
          <v-list-item v-for="notification in notifications.filter((not) => !userStore.hideSatisfiedNotifications || !not.satisfied)"
                       :key="notification.id" :to="getNotificationLink(notification).routerPath()" @click="markAllRead">
            <v-label>{{ new Date(notification.datetime).toLocaleString() }}</v-label>
            <v-divider />
            <span v-html="getNotificationText(notification)" />
          </v-list-item>
          <v-list-item v-if="!notifications.length">
            {{ t(`$vuetify.notification_list_${showAll ? 'all_' : ''}empty`) }}
          </v-list-item>
        </v-list>

        <template v-if="!showAll">
          <v-btn v-if="notifications.length" class="mb-2" :block="true" variant="tonal" color="green"
                 @click="markAllRead">
            {{ t('$vuetify.notification_list_mark_satisfied') }}
          </v-btn>
          <v-btn :block="true" variant="tonal" color="blue" @click="router.push(new Nav.NotificationList().routerPath())">
            {{ t('$vuetify.notification_list_show_all') }}
          </v-btn>
        </template>
      </template>
    </LoadingScreen>
  </v-card>
</template>
