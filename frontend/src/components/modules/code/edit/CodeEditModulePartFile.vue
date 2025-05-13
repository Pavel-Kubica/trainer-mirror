<script setup>
import { inject, ref } from 'vue'
import { useLocale } from 'vuetify'
import { CODE_MODULE_CODE_LIMIT_VALUES } from "@/plugins/constants";
import CodeEditor from "@/components/custom/CodeEditor.vue";

defineProps(['fileId', 'addFile', 'readOnly'])
const codeData = inject('codeData')
const testToDelete = inject('testToDelete')
const fileToDelete = inject('fileToDelete')
const deleteDialog = inject('deleteDialog')

const selectedTab = ref("content")
const TAB_OPTIONS = ['content', 'reference']

const { t } = useLocale()
const translate = (key) => t(`$vuetify.code_module.edit_file_${key}`)
</script>

<template>
  <v-text-field v-model="codeData.files[fileId].name" :label="translate('name')" />
  <v-select v-model="codeData.files[fileId].codeLimit" :label="translate('code_limit')"
            :items="CODE_MODULE_CODE_LIMIT_VALUES(t)" item-title="title" item-value="size" />
  <v-switch v-model="codeData.files[fileId].headerFile" :label="translate('header_file')"
            class="ps-2" color="primary" hide-details />
  <v-tabs v-model="selectedTab">
    <v-tab v-for="option in TAB_OPTIONS" :key="option" :value="option">
      {{ translate(option) }}
    </v-tab>
  </v-tabs>
  <v-window v-model="selectedTab">
    <v-window-item v-for="option in TAB_OPTIONS" :key="option" :value="option"
                   :transition="false" :reverse-transition="false">
      <CodeEditor class="mb-4" :file-id="fileId" :file-key="option" :disabled="readOnly" />
    </v-window-item>
  </v-window>

  <v-btn v-if="codeData.files[fileId].id" class="ms-2 mt-2 mb-4" :disabled="readOnly" color="red" :block="true" size="large" variant="tonal"
         @click="() => { testToDelete = null; fileToDelete = codeData.files[fileId]; deleteDialog = true }">
    {{ t('$vuetify.code_module.delete_file') }}
  </v-btn>
  <div v-else>
    <v-btn class="ms-2 mt-2 mb-4" color="green" :block="true" size="large" variant="tonal"
           :disabled="!codeData.files[fileId].name" @click="() => addFile(codeData.files[fileId])">
      {{ t('$vuetify.code_module.create_file') }}
    </v-btn>
    <v-tooltip v-if="!codeData.files[fileId].name" activator="parent" location="top">
      {{ t('$vuetify.code_module.create_file_requirements') }}
    </v-tooltip>
  </div>
</template>
