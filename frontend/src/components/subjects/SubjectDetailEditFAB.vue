<script setup>
import { inject } from 'vue'
import { useLocale } from 'vuetify'

const { t } = useLocale()
const createEditDialog = inject('createEditDialog')
defineEmits(['loadSemesterDetail'])
</script>

<template>
  <div class="ma-4" style="position: fixed; right: 16px; bottom: 40px">
    <v-btn icon color="primary" elevation="8" @click="() => { createEditDialog = true }">
      <v-icon icon="mdi-plus" />
      <v-tooltip activator="parent" location="top">
        {{ t('$vuetify.subject_create_new') }}
      </v-tooltip>
    </v-btn>
  </div>
</template>
