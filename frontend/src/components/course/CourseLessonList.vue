<script setup>
import { useLocale } from 'vuetify'
import { onMounted, ref } from 'vue'
import { lessonApi } from '@/service/api'

import LoadingScreen from '@/components/custom/LoadingScreen.vue'
import CurrentLessonListRow from "@/components/lesson/CurrentLessonListRow.vue";

const { current } = useLocale()
const dayWord = () => {
  const day = new Date().toLocaleString(current.value === 'customCs' ? 'cs' : 'en', {weekday: "long"})
  return day.charAt(0).toLocaleUpperCase() + day.slice(1)
}
const hourMinute = () => new Date().toLocaleString(current.value === 'customCs' ? 'cs' : 'en', {hour: "2-digit", minute: "2-digit"})

const lessons = ref(null)
const error = ref(null)
const { t } = useLocale()

onMounted(async () => {
  lessonApi.listCurrentLessons()
      .then((result) => { lessons.value = result })
      .catch((err) => { error.value = err.code })
})
</script>

<template>
  <v-alert v-if="t('$vuetify.information') !== ' '" class="mx-8 my-4" type="info" variant="tonal">
    <span class="text-pre-wrap">{{ t('$vuetify.information') }}</span>
  </v-alert>
  <v-card class="mx-8 my-4" :title="t('$vuetify.current_lesson_list_title')">
    <template #append>
      🗓️ {{ dayWord() }} ⏱️ {{ hourMinute() }}
    </template>
    <LoadingScreen :items="lessons" :error="error">
      <template #table>
        <CurrentLessonListRow v-for="lesson in lessons" :key="lesson.id" :lesson="lesson" />
        <tr v-if="!lessons.length">
          <td>{{ t('$vuetify.current_lesson_list_empty') }}</td>
        </tr>
      </template>
    </LoadingScreen>
  </v-card>
</template>
