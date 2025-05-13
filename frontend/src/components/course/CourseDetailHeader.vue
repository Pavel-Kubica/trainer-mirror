<script setup>
import { useLocale } from 'vuetify'
import * as Nav from '@/service/nav'
import {inject} from "vue";

const joinCodeDialog = inject('joinCodeDialog')

defineProps(['course'])
const { t } = useLocale()
</script>

<template>
  <v-btn class="text-primary" @click="joinCodeDialog = true">
    {{ t('$vuetify.course_detail_header_course_secret') }}
  </v-btn>

  <router-link class="text-decoration-none" :to="new Nav.CourseUserList(course).routerPath()">
    <v-btn class="text-primary ml-5">
      {{ t('$vuetify.course_detail_header_users') }}
    </v-btn>
  </router-link>
</template>
