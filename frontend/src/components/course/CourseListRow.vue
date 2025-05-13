<script setup>
import { useLocale } from 'vuetify'
import { useUserStore } from '@/plugins/store'
import * as Nav from '@/service/nav'

defineProps(['course'])
const userStore = useUserStore()
const { t } = useLocale()
const role = (course) => userStore.isTeacher(course) ? 'teacher' : 'student'
</script>

<template>
  <tr>
    <td>{{ course.name }} {{ t(`$vuetify.course_list_role_${role(course)}`) }}</td>
    <td>{{ course.subject?.code }}</td>
    <!---
    currently not used for performance reasons, looking for better solution

    <td>
      <span v-if="course.lessonCount">
        {{ t("$vuetify.course_list_lesson_completed", course.lessonCompleted, course.lessonCount) }}
        <v-tooltip activator="parent" location="top">{{ t('$vuetify.course_list_lesson_completed_tooltip', course.lessonCompleted, course.lessonCount) }}</v-tooltip>
      </span>
    </td>--->
    <td>
      <router-link :to="new Nav.CourseDetail(course).routerPath()" class="d-flex justify-end text-decoration-none">
        <v-btn variant="text" icon="mdi-arrow-right" />
      </router-link>
    </td>
  </tr>
</template>
