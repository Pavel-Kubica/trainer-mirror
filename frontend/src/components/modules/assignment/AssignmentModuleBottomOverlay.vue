<script setup>
import { inject } from 'vue'
import { useLocale } from 'vuetify'
import {downloadFileUrl, unsatisfiedModuleRequest} from '@/plugins/constants'
import Compressor from 'compressorjs'
import { studentModuleApi } from '@/service/api'
import ModuleBottomOverlay from '@/components/lesson/ModuleBottomOverlay.vue'

const MAX_UPLOAD_SIZE = 1024*1024

const { t } = useLocale()
const moduleData = inject('module')
const fileDownloadedUrl = inject('fileDownloadedUrl')
const file = inject('file')
const error = inject('error')

const fileInserted = async (file) => {
  if (!file)
    return

  if (fileDownloadedUrl.value)
    URL.revokeObjectURL(fileDownloadedUrl.value)
  fileDownloadedUrl.value = null

  new Compressor(file, {
    quality: 0.8,
    maxWidth: 1920, maxHeight: 1080, resize: "none",
    convertTypes: 'image/png', convertSize: MAX_UPLOAD_SIZE, // convert when exceeds limit
    success(result) {
      if (result.size > MAX_UPLOAD_SIZE) {
        error.value = t('$vuetify.assignment_module.error_file_too_large')
        return
      }
      studentModuleApi.putStudentModuleFile(moduleData.value.lesson.id, moduleData.value.id, result)
          .then(() => { fileDownloadedUrl.value = URL.createObjectURL(result) })
          .catch((err) => { error.value = err.code })
    },
    error(err) { error.value = err },
  })
}

const downloadFile = async (e) => {
  e.stopPropagation()
  await downloadFileUrl(fileDownloadedUrl.value,
      `${moduleData.value.teacher ? `${moduleData.value.teacher}-` : ''}${moduleData.value.name}.jpeg`)
}

const deleteFile = (e) => {
  e.stopPropagation()
  fileDownloadedUrl.value = null
  file.value = null
}
</script>

<template>
  <ModuleBottomOverlay v-model="file"
                       accept="image/jpg, image/jpeg, image/png"
                       :label="t('$vuetify.assignment_module.input_label')" :disabled="unsatisfiedModuleRequest(moduleData) && moduleData.studentRequest.requestType === 'EVALUATE'"
                       @update:modelValue="fileInserted">
    <template #prepend-actions>
      <v-btn v-if="fileDownloadedUrl" icon variant="text" @click="downloadFile">
        <v-icon icon="mdi-download" />
        <v-tooltip activator="parent" location="top">
          {{ t('$vuetify.code_module.download') }}
        </v-tooltip>
      </v-btn>
    </template>
    <template #append-actions>
      <v-btn v-if="fileDownloadedUrl" icon variant="text" @click="deleteFile">
        <v-icon icon="mdi-delete" />
        <v-tooltip activator="parent" location="top">
          {{ t('$vuetify.action_delete') }}
        </v-tooltip>
      </v-btn>
    </template>
  </ModuleBottomOverlay>
</template>
