<script setup>
import { ref, onMounted, inject, provide } from 'vue'
import { TAB_ASSIGNMENT, TAB_MODULE_DETAIL } from '@/plugins/constants'
import { useDisplay, useLocale } from 'vuetify'
import { studentModuleApi } from '@/service/api'
import AssignmentImagePreviewCard from "@/components/modules/assignment/AssignmentImagePreviewCard.vue";
import AssignmentModuleBottomOverlay from "@/components/modules/assignment/AssignmentModuleBottomOverlay.vue";
import ModuleRequestDrawer from "@/components/lesson/ModuleRequestDrawer.vue";

const props = defineProps(['teacher'])
const lesson = inject('lesson')
const moduleData = inject('module')
const { mobile } = useDisplay()
const { t } = useLocale()

const file = ref(null)
const fileDownloadedUrl = ref(null)
const error = ref(null)
const runtime = ref({running: false})
provide('file', file)
provide('fileDownloadedUrl', fileDownloadedUrl)
provide('error', error)
provide('runtime', runtime)

const loadModuleFile = async () => {
  if (!moduleData.value.hasStudentFile) return

  const callback = (blob) => {
    if (fileDownloadedUrl.value)
      URL.revokeObjectURL(fileDownloadedUrl.value)
    fileDownloadedUrl.value = URL.createObjectURL(blob)
    const image = document.querySelector('#preview')
    image.src = URL.createObjectURL(blob)
  }

  if (props.teacher)
    studentModuleApi.getStudentModuleFileTeacher(lesson.value.id, moduleData.value.id, props.teacher)
        .then(callback).catch((err) => { error.value = err.code })
  else
    studentModuleApi.getStudentModuleFile(lesson.value.id, moduleData.value.id)
        .then(callback).catch((err) => { error.value = err.code })
}

onMounted(async () => {
  if (props.teacher) {
    const tabList = inject('tabList')
    tabList.value[TAB_MODULE_DETAIL] = t('$vuetify.tab_preview')
  }
  else {
    const selectedTab = inject('selectedTab')
    selectedTab.value = TAB_ASSIGNMENT
  }
  moduleData.value.teacher = props.teacher
  moduleData.value.lesson = lesson.value
  await loadModuleFile()
})
</script>

<template>
  <v-card v-if="!teacher && moduleData.lesson" flat class="mb-2 pa-2">
    <AssignmentModuleBottomOverlay />
  </v-card>
  <v-window-item v-else :key="TAB_MODULE_DETAIL" :transition="false"
                 :reverse-transition="false" :value="TAB_MODULE_DETAIL">
    <AssignmentImagePreviewCard :max-height="true" />
  </v-window-item>

  <v-navigation-drawer v-if="!mobile" location="right" class="pa-2" width="400">
    <div class="d-flex flex-column fill-height justify-space-between">
      <span v-if="teacher" />
      <AssignmentImagePreviewCard v-else-if="fileDownloadedUrl" :title="t('$vuetify.tab_preview')" />
      <ModuleRequestDrawer v-if="fileDownloadedUrl" />
    </div>
  </v-navigation-drawer>
  <v-card v-else class="mt-4 mb-2">
    <span v-if="teacher" />
    <AssignmentImagePreviewCard v-else-if="fileDownloadedUrl" :title="t('$vuetify.tab_preview')" />
    <ModuleRequestDrawer v-if="fileDownloadedUrl" />
  </v-card>
</template>
