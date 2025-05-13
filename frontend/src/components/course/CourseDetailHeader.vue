<script setup>
import { useLocale } from 'vuetify'
import * as Nav from '@/service/nav'
import {inject} from "vue";

defineProps(['course'])
const { t } = useLocale()
const createEditDialog = inject('createEditDialog')
const editWeek = inject('editWeekEntity')
</script>

<template>
  <v-hover v-if="course.secret">
    <template #default="{ isHovering, props }">
      <v-btn v-bind="props" style="color: rgb(var(--v-theme-anchor))">
        {{ isHovering ? course.secret : t('$vuetify.course_detail_header_course_secret') }}
      </v-btn>
    </template>
  </v-hover>
  &ensp;
  <v-btn class="mx-3" @click="() => { editWeek = null; createEditDialog = true }">
    <span class="text-primary">{{ t('$vuetify.week_new') }}</span>
  </v-btn>

  <router-link class="text-decoration-none" :to="new Nav.CourseUserList(course).routerPath()">
    <v-btn class="text-primary">
      {{ t('$vuetify.course_detail_header_users') }}
    </v-btn>
  </router-link>
</template>
