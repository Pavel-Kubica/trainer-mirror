<script setup>
import { getErrorMessage } from '@/plugins/constants'
import { useLocale } from 'vuetify'
const { t } = useLocale()

defineProps(['items', 'error'])
</script>

<template>
  <slot v-if="items && !error" name="items">
    <v-card-item>
      <slot name="prepend" />
      <!-- default content is table -->
      <slot name="content">
        <v-table>
          <tbody>
            <slot name="table" />
          </tbody>
        </v-table>
      </slot>
    </v-card-item>
  </slot>
  <v-card-item v-else-if="error">
    <slot name="error">
      {{ getErrorMessage(t, error) }}
    </slot>
  </v-card-item>
  <v-card-item v-else class="pa-8 flex justify-center">
    <slot name="loading">
      <v-progress-circular indeterminate="true" />
    </slot>
  </v-card-item>
</template>
