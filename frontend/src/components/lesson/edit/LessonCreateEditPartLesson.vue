<script setup>
import { inject } from 'vue'
import { useLocale } from 'vuetify'

import { LESSON_TYPES_ITEMS } from '@/plugins/constants'
import LocaleAwareDatepicker from "@/components/custom/LocaleAwareDatepicker.vue";

const lessonData = inject('lessonData')
const { t } = useLocale()
const translate = (key) => t(`$vuetify.lesson_edit_${key}`)
</script>

<template>
  <v-text-field v-model="lessonData.name" :label="translate('name')" />
  <v-text-field v-model="lessonData.order" :label="translate('order')" type="number" />
  <v-select v-model="lessonData.type" :label="translate('type')"
            :items="LESSON_TYPES_ITEMS(t)" item-title="name" item-value="type" />
  <v-text-field v-model="lessonData.lockCode" :label="translate('code')" />
  <v-label>{{ translate('time_start') }}</v-label>
  <LocaleAwareDatepicker v-model="lessonData.timeStart" :with-time="true" />
  <v-label class="mt-2">
    {{ translate('time_end') }}
  </v-label>
  <LocaleAwareDatepicker v-model="lessonData.timeLimit" :with-time="true" />
  <v-switch v-model="lessonData.hidden" class="mt-2 ms-4" color="primary" :label="translate('hidden')" />
</template>
