<script setup>
import {inject, ref, watch} from 'vue'
import { useLocale } from 'vuetify'
import { moduleApi } from '@/service/api'
import { downloadFileBlob, moduleFileDownloadName } from '@/plugins/constants'
import ModuleBottomOverlay from '@/components/lesson/ModuleBottomOverlay.vue'
import { TarWriter } from '@gera2ld/tarjs'

const appState = inject('appState')
const { t } = useLocale()

const props = defineProps(['fileId'])
const fileDropped = inject('fileDropped')
const moduleData = inject('module')
const codeData = inject('codeData')
const run = inject('run')

const codeFile = ref(null)
const codeFileInserted = async (files) => {
  if (!files
      || files.length === 0
      || codeData.value.codeType === 'SHOWCASE')
    return
  for (const index in files) {
    const file = files[index]
    const ix = codeData.value.files.findIndex((cf) => file.name.endsWith(cf.name))
    if (ix === -1 && (props.fileId === undefined || index > 0)) {
      appState.value.notifications.push({
        type: "error", title: t(`$vuetify.code_module.code_inserted_failure_title`),
        text: t(`$vuetify.code_module.code_inserted_failure_text`),
      })
      return // error
    }
    const fileContents = await file.arrayBuffer()
    codeData.value.files[ix === -1 ? props.fileId : ix].content = new TextDecoder().decode(fileContents)
  }
  appState.value.notifications.push({
    type: "success", title: t(`$vuetify.code_module.code_inserted_title`),
    text: t(`$vuetify.code_module.code_inserted_text`),
  })
  await run()
}

const changedFile = () => {
  let checkIndexes = props.fileId ? [props.fileId] : Object.keys(codeData.value.files)
  for (let index of checkIndexes)
    if (codeData.value.originalFiles[index]?.content !== codeData.value.files[index]?.content)
      return true
  return false
}

const downloadTar = async (files, tarName, reference = false) => {
  const writer = new TarWriter()
  for (const file of files)
    writer.addFile(file.name, reference ? file.reference : file.content)
  const blob = await writer.write()
  const fileName = moduleFileDownloadName(moduleData.value, tarName)
  await downloadFileBlob(blob, fileName)
}

const downloadCode = async (original = false) => {
  if (props.fileId === undefined) {
    await downloadTar(original ? codeData.value.originalFiles : codeData.value.files,
        original ? '-default.tar' : '-student.tar')
    return
  }
  const currentFile = (original ? codeData.value.originalFiles[props.fileId ?? 0] : codeData.value.files[props.fileId ?? 0])
  if (!currentFile) return
  const fileName = moduleFileDownloadName(moduleData.value, `-${currentFile.name}`)
  await downloadFileBlob(new Blob([currentFile.content], { type: "text/c++" }), fileName)
}

const downloadRef = async () => {
  if (props.fileId === undefined) {
    await downloadTar(codeData.value.files, '-ref.tar', true)
    return
  }
  const currentFile = codeData.value.files[props.fileId]
  const fileName = moduleFileDownloadName(moduleData.value, `-ref-${currentFile.name}`)
  await downloadFileBlob(new Blob([currentFile.reference], { type: "text/c++" }), fileName)
}

const downloadFile = async () => {
  const fileBlob = await moduleApi.getModuleFile(moduleData.value.id)
  const fileName = moduleFileDownloadName(moduleData.value, '.tar')
  await downloadFileBlob(fileBlob, fileName)
}

watch(fileDropped, () => {
  codeFile.value = Array.from(fileDropped.value)
  codeFileInserted(codeFile.value)
})
</script>

<template>
  <v-card v-if="!moduleData.teacher" flat class="d-flex flex-column">
    <ModuleBottomOverlay v-model="codeFile" accept=".c, .cpp, .cxx, .h, .hpp" multiple
                         :label="t('$vuetify.code_module.file_upload')"
                         :reset-callback="() => codeData.codeModuleIdCheck = -1" @update:modelValue="codeFileInserted">
      <template #prepend-actions>
        <v-menu location="left">
          <template #activator="{ props }">
            <v-btn icon variant="text" v-bind="props">
              <v-icon icon="mdi-download" />
              <v-tooltip activator="parent" location="top">
                {{ t('$vuetify.code_module.download') }}
              </v-tooltip>
            </v-btn>
          </template>
          <v-list>
            <v-list-item v-if="changedFile()" @click="() => downloadCode(false)">
              {{ t('$vuetify.code_module.submitted_code') }}
            </v-list-item>
            <v-list-item v-if="codeData.referencePublic" @click="() => downloadRef()">
              {{ t('$vuetify.code_module.reference_solution') }}
            </v-list-item>
            <v-list-item @click="() => downloadCode(true)">
              {{ t('$vuetify.code_module.default_code') }}
            </v-list-item>
            <v-list-item v-if="moduleData.file" @click="() => downloadFile()">
              {{ t('$vuetify.code_module.test_data') }}
            </v-list-item>
          </v-list>
        </v-menu>
      </template>
    </ModuleBottomOverlay>
  </v-card>
</template>
