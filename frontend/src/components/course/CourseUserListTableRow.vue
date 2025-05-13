<script setup>
import { inject } from 'vue'
import { useLocale } from 'vuetify'
import { useUserStore } from '@/plugins/store'
import { anonEmoji } from '@/plugins/constants'
import * as Nav from '@/service/nav'
import TooltipIconButton from "@/components/custom/TooltipIconButton.vue";

defineProps(['course', 'item'])
const userStore = useUserStore()
const { t } = useLocale()

const deleteCourseUser = inject('deleteCourseUser')
const deleteCourseUserDialog = inject('deleteCourseUserDialog')
</script>

<template>
  <tr>
    <td>{{ userStore.anonymous ? anonEmoji(item.username) : item.name }}</td>
    <td>{{ userStore.anonymous ? anonEmoji(item.username) : item.username }}</td>
    <td>{{ t(item.role === 'TEACHER' ? '$vuetify.role_teacher' : '$vuetify.role_student') }}</td>
    <td>
      <div class="d-flex justify-space-between align-center">
        <v-progress-linear class="rounded" color="rgb(var(--v-theme-progress))"
                           :model-value="item.progress" height="5" style="width: 25vw" />
        <strong class="ms-2">{{ item.progress }} %</strong>
      </div>
    </td>
    <td class="d-flex justify-end">
      <TooltipIconButton icon="mdi-delete" color="red"
                         :tooltip="t('$vuetify.course_user_remove')"
                         @click="deleteCourseUser = item; deleteCourseUserDialog = true" />
      <TooltipIconButton icon="mdi-eye" color="rgb(var(--v-theme-anchor))"
                         :tooltip="t('$vuetify.course_user_detail_link')"
                         :to="new Nav.CourseUserDetail(course, item).routerPath()" />
    </td>
  </tr>
</template>
