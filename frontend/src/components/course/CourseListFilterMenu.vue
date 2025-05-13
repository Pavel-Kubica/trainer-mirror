<script setup>
import { useLocale } from 'vuetify'
import { inject } from 'vue'

const { t } = useLocale()
const semesters = inject('semesters')
const semester = inject('semester')
</script>

<template>
  <v-menu>
    <template #activator="{ props }">
      <v-btn append-icon="mdi-chevron-down" color="rgb(var(--v-theme-anchor))" variant="outlined" v-bind="props">
        {{ t('$vuetify.course_list_semesters', semester ? semester.code : t('$vuetify.course_list_all_semesters')) }}
      </v-btn>
    </template>
    <v-list>
      <v-list-item v-for="sem in semesters" :key="sem?.id"
                   :active="semester?.id === sem?.id" @click="semester = sem">
        <v-list-item-title>{{ sem?.code ?? t('$vuetify.course_list_all_semesters') }}</v-list-item-title>
      </v-list-item>
    </v-list>
  </v-menu>
</template>
