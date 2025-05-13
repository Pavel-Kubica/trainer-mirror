<script setup>
import { useLocale } from 'vuetify'
import * as Nav from '@/service/nav'


const { t } = useLocale()



</script>

<template>
  <v-card class="mx-auto">
    <v-list density="comfortable">
      <v-list-item :to="new Nav.CourseList().routerPath()">
        <template #prepend>
          <v-icon size="small" icon="mdi-arrow-left" />
        </template>
        <template #title>
          <strong>{{ t('$vuetify.course_detail_left_drawer_course_list') }}</strong>
        </template>
      </v-list-item>
    </v-list>
  </v-card>
</template>
