<script setup>
import { useLocale } from 'vuetify'

const { t } = useLocale()
defineProps(['icon', 'tooltip', 'iconSize', 'color'])

const TOOLTIPS = {
  'mdi-pencil': '$vuetify.action_edit',
  'mdi-delete': '$vuetify.action_delete',
  'mdi-content-copy': '$vuetify.action_copy',
}
</script>

<template>
  <v-btn variant="text" icon>
    <v-icon :color="color" :size="iconSize" :icon="icon" />
    <v-tooltip activator="parent" location="top">
      {{ t(tooltip ?? (TOOLTIPS[icon] ?? '')) }}
    </v-tooltip>
  </v-btn>
</template>
