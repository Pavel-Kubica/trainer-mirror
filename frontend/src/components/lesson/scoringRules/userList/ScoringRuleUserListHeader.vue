<script setup>
import {inject, onMounted} from 'vue'
import { useLocale } from 'vuetify'

const lesson = inject('lesson')
const { t } = useLocale()

onMounted(() => {
  console.log("lesson Header - ", lesson)
})
</script>

<template>
  <thead>
    <tr>
      <th>{{ t('$vuetify.lesson_user_list_header_user') }}</th>
      <th>{{ lesson.name }}</th>
      <th v-for="sr in lesson.scoringRules" :key="sr.id">
        <span>{{ sr.shortName }}</span>
      </th>
    </tr>
  </thead>
</template>
