<script setup>
import {inject, ref, provide} from 'vue'
import { useLocale } from 'vuetify'
import { useUserStore } from '@/plugins/store'
import { anonEmoji } from '@/plugins/constants'
import TooltipIconButton from "@/components/custom/TooltipIconButton.vue";
import CourseUserListRowContent from "@/components/course/CourseUserListUserDetail.vue";

const props = defineProps(['courseId', 'user'])
const userStore = useUserStore()
const { t } = useLocale()

provide('courseUser', props.user);

const collapsed = ref(true)

const deleteCourseUser = inject('deleteCourseUser')
const deleteCourseUserDialog = inject('deleteCourseUserDialog')
</script>

<template>
  <tr v-ripple.center :class="collapsed ? '' : 'unfolded'" style="cursor: pointer" @click="collapsed = !collapsed">
    <td>
      <v-btn density="compact" variant="text"
             :icon="collapsed ? 'mdi-chevron-down' : 'mdi-chevron-up'" />
    </td>
    <td>
      {{ userStore.anonymous ? anonEmoji(user.username) : user.name }}
    </td>
    <td>{{ userStore.anonymous ? anonEmoji(user.username) : user.username }}</td>
    <td>{{ t(user.role === 'TEACHER' ? '$vuetify.role_teacher' : '$vuetify.role_student') }}</td>
    <td style="width: 50vw">
      <div class="d-flex justify-space-between align-center">
        <v-progress-linear class="rounded" color="rgb(var(--v-theme-progress))"
                           :model-value="user.progress" height="5" style="width: 90%" />
        <strong class="ms-2">{{ user.progress }}&nbsp;%</strong>
      </div>
    </td>
    <td v-if="userStore.isGuarantor" class="d-flex justify-end">
      <TooltipIconButton icon="mdi-delete" color="red"
                         :tooltip="t('$vuetify.course_user_remove')"
                         @click.stop.prevent="deleteCourseUser = user; deleteCourseUserDialog = true" />
    </td>
  </tr>
  <tr v-if="!collapsed" style="width: 100%">
    <td colspan="7">
      <div style="width: 95%; margin: 0 2.5%">
        <CourseUserListRowContent :course-id="courseId" :user="user" />
      </div>
    </td>
  </tr>
</template>

<style scoped>
  tr.unfolded > td {
    border-bottom: none !important;
  }
</style>