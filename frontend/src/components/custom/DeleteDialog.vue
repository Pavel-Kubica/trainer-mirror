<script setup>
import {inject} from 'vue';
import {useLocale} from "vuetify";
const { t } = useLocale()

const deleteDialog = inject('deleteDialog')

defineProps(['title', 'textStart', 'itemName', 'textBeforeLineBreak', 'textSecondLine', 'onConfirm', 'onCancel', 'textConfirmButton'])


</script>

<template>
  <v-dialog v-model="deleteDialog" width="auto" @keydown.enter="onConfirm">
    <v-card :title="t(title)">
      <!-- Can't use v-html and substitution, we render user provided names -->
      <v-card-item class="text-center" :style="{'white-space': 'pre-line'}">
        {{ t(textStart) }}<strong>{{ itemName }}</strong>{{ t(textBeforeLineBreak) }}<br v-if="textSecondLine">{{ t(textSecondLine) }}
      </v-card-item>
      <v-card-actions>
        <div style="width: 100%" class="d-flex flex-row justify-space-between pa-2">
          <v-btn color="primary" @click="onCancel">
            {{ t('$vuetify.dialog_close') }}
          </v-btn>
          <v-btn color="red" @click="onConfirm">
            {{ t(textConfirmButton) }}
          </v-btn>
        </div>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>