<script setup>
import { useLocale } from 'vuetify'

const props = defineProps(['emoji', 'parameter'])
const { t } = useLocale()

const TOOLTIPS = {
  '👁': '$vuetify.tooltip_module_allowed_show',
  '🏆': '$vuetify.tooltip_module_completed',
  '⚡️': '$vuetify.tooltip_module_completed_early',
  '😿': '$vuetify.tooltip_module_requested_help',
  '⬆️': '$vuetify.tooltip_module_submitted',
  '✅': '$vuetify.tooltip_module_solved',
  '♻️': '$vuetify.tooltip_module_in_progress',
  '🚫': '$vuetify.tooltip_module_opened',
  '⭕️': '$vuetify.tooltip_module_unopened',

  '⏱️': '$vuetify.tooltip_lesson_timed',
  '🔑': '$vuetify.tooltip_lesson_locked',
  '👻': '$vuetify.tooltip_lesson_hidden',
}
</script>

<template>
  <span v-if="props.emoji">
    {{ props.emoji }}
    <v-tooltip activator="parent" location="top">{{ t(TOOLTIPS[emoji], parameter) }}</v-tooltip>
  </span>
</template>
