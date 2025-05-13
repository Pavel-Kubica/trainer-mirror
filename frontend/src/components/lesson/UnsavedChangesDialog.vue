<script setup>
import { inject } from 'vue'
import { useLocale } from 'vuetify'

const props = defineProps(['routerCallback', 'saveCallback'])
const unsavedChangesDialog = inject('unsavedChangesDialog')
const { t } = useLocale()

const translate = (key) => t(`$vuetify.unsaved_changes_dialog_${key}`)

const onConfirm = async () => {
  await props.saveCallback();
  props.routerCallback();
  unsavedChangesDialog.value = false
}

</script>

<template>
  <v-dialog v-model="unsavedChangesDialog" width="auto" @keydown.enter="onConfirm">
    <v-card :title="translate('title')">
      <v-card-item>{{ translate('text') }}</v-card-item>
      <v-card-actions>
        <div style="width: 100%" class="d-flex flex-row justify-space-between pa-2">
          <v-btn color="red" @click="() => { routerCallback(); unsavedChangesDialog = false }">
            {{ t('$vuetify.dialog_trash') }}
          </v-btn>
          <v-btn color="primary" @click="onConfirm">
            {{ t('$vuetify.dialog_save') }}
          </v-btn>
        </div>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>
