<script setup>
import { inject } from 'vue'
import { useLocale } from 'vuetify'
import { studentModuleApi } from '@/service/api'
import { getErrorMessage } from '@/plugins/constants'
import { useUserStore } from '@/plugins/store'
import router from '@/router'

const appState = inject('appState')
const reloadHook = inject('reloadHook')
const { t } = useLocale()
const userStore = useUserStore()

const props = defineProps(['resetCallback'])
const moduleData = inject('module')

const allowShow = async (e) => {
  e.stopPropagation()
  const nowAllowed = !moduleData.value.allowedShow
  moduleData.value.allowedShow = !moduleData.value.allowedShow
  studentModuleApi.putStudentModule(moduleData.value.lesson.id, moduleData.value.id, {
    allowedShow: moduleData.value.allowedShow
  }).then(() => {
    reloadHook.value = new Date().getTime() // update side menu
    appState.value.notifications.push({
      type: "success",
      title: t(`$vuetify.code_module.${nowAllowed ? '' : 'dis'}allowed_show.title`),
      text: t(`$vuetify.code_module.${nowAllowed ? '' : 'dis'}allowed_show.text`),
    })
  }).catch((err) => {
    appState.value.notifications.push({
      type: "error",
      title: t(`$vuetify.code_module.${nowAllowed ? '' : 'dis'}allowed_show.title_fail`),
      text: t(`$vuetify.code_module.${nowAllowed ? '' : 'dis'}allowed_show.text_fail`, getErrorMessage(t, err.code)),
    })
    moduleData.value.allowedShow = !moduleData.value.allowedShow // revert change
  })
}

const resetProgress = async (e) => {
  e.stopPropagation()
  if (props.resetCallback) props.resetCallback()
  studentModuleApi.deleteStudentModule(moduleData.value.lesson.id, moduleData.value.id)
      .then(() => router.go(0))
      .catch((error) => { console.log(error) })
}
</script>

<template>
  <v-file-input class="py-2" variant="solo-filled" :prepend-icon="null" :hide-details="true">
    <template #append-inner>
      <slot name="prepend-actions" />
      <v-btn icon variant="text" @click="allowShow">
        <v-icon :icon="moduleData.allowedShow ? 'mdi-eye-off' : 'mdi-eye'" />
        <v-tooltip activator="parent" location="top">
          {{ t(`$vuetify.code_module.${moduleData.allowedShow ? 'disallow_show_tooltip' : 'allow_show_tooltip'}`) }}
        </v-tooltip>
      </v-btn>
      <v-btn v-if="!moduleData.teacher && userStore.isTeacher(moduleData.lesson.week.course)" icon variant="text" @click="resetProgress">
        <v-icon icon="mdi-trash-can" />
        <v-tooltip activator="parent" location="top">
          {{ t('$vuetify.code_module.reset_progress') }}
        </v-tooltip>
      </v-btn>
      <slot name="append-actions" />
    </template>
  </v-file-input>
</template>
