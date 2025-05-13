<script setup>
import {inject} from 'vue'
import {courseApi, lessonApi, lessonModuleApi, userApi, weekApi} from '@/service/api'
import { useUserStore } from '@/plugins/store'
import TooltipIconButton from '@/components/custom/TooltipIconButton.vue'
import TooltipEmojiSpan from '@/components/custom/TooltipEmojiSpan.vue'
import {useLocale} from "vuetify";
import codeApiClient from "@/service/codeApi";
import {
  DEFAULT_CODE_CONTENT_C, MODULE_EDIT_CODE_TEST,
  DEFAULT_CODE_FILENAME_C
} from "@/plugins/constants";
import * as Nav from "@/service/nav";

const props = defineProps(['customSearch', 'dismiss', 'reloadDialog', 'reloadSource', 'topicChipColor', 'subjectChipColor'])
const userStore = useUserStore()
const { t } = useLocale()

const codeData = inject('codeData')
const modules = inject('modules')
const lesson = inject('lesson')
const error = inject('error')
const appState = inject('appState')
const moduleEditTabList = inject('moduleEditTabList')
const moduleDeleteDialog = inject('deleteDialog')
const moduleToDelete = inject('moduleToDelete')

const copyModule = async (module) => {
  modules.value = null
  lessonModuleApi.copyLessonModule(lesson.value.id, {
    copiedId: module.id,
    order: (lesson.value.modules.slice(-1)[0]?.order ?? 0) + 1
  })
  .then(() => {
        props.reloadSource()
        props.reloadDialog()
  })
  .catch((err) => { error.value = err.code })
}

const testModule = async(module) => {
  modules.value = null
  let sandboxCourse = await userApi.getSandbox(userStore.user.id)
  if (!sandboxCourse) // If no sandbox, create one
    sandboxCourse = await courseApi.createSandbox(userStore.user.id)
  let week = sandboxCourse.weeks[0]
  if (sandboxCourse && !sandboxCourse.weeks.length) { // If we created a new sandboxcourse it will have a week, but otherwise might not
    week = await weekApi.createWeek({courseId: sandboxCourse.id, name: "Sandbox week", from: new Date(), until: new Date().setUTCFullYear(2030)})
  }
  if (sandboxCourse && (!sandboxCourse.weeks.length || !sandboxCourse.weeks[0].lessons.length)) {
    // We didn't refetch it yet, if it didn't have a week before or didn't have a lesson, create one
    await lessonApi.createLesson({
      weekId: week.id, name: "Sandbox lesson",
      hidden: false, order: 0, type: 'TUTORIAL', description: "Sandbox lesson description"
    })
    sandboxCourse = await userApi.getSandbox(userStore.user.id) // Must now refetch it
  }

  lessonModuleApi.putLessonModule(sandboxCourse.weeks[0].lessons[0].id, module.id, {
    order: (sandboxCourse.weeks[0].lessons[0].order ?? 0) + 1
  }).then(() => {
    appState.value.notifications.push({
      title:  t('$vuetify.lesson_edit_modules_sandbox_title'),
      text:   t('$vuetify.lesson_edit_modules_sandbox_success'),
      type: "success"
    })
  }).catch((err) => { error.value = err.code })
  props.dismiss(true)
}

const addModule = async (module) => {
  modules.value = null
  lessonModuleApi.putLessonModule(lesson.value.id, module.id, {
    order: (lesson.value.modules.slice(-1)[0]?.order ?? 0) + 1
  })
  .then(() => {
      props.reloadSource()
      props.reloadDialog()
  })
  .catch((err) => { error.value = err.code })
}

const useAsTemplate = async (module) => {
  codeApiClient.codeDetail(module.id).then(
    (response) => {
      codeData.value = {
        codeType: response.codeType,  interactionType: 'EDITOR', codeHidden: response.codeHidden,  fileLimit: response.fileLimit, referencePublic: false,
        envelopeType: response.envelopeType, customEnvelope: response.customEnvelope, libraryType: response.libraryType, assignment: '', tests: response.tests,
        files: [{name: DEFAULT_CODE_FILENAME_C, tmp: true, id: -1, codeLimit: 1024, content: DEFAULT_CODE_CONTENT_C, reference: '', headerFile: response.headerFile}],
      }
      codeData.value.tests.forEach((test) => {
        test.id = -1
        test.realId = null
      })
      codeData.value.files.push({name: t('$vuetify.code_module.new_file')})
      codeData.value.tests.push({name: t('$vuetify.code_module.new_test')})
      moduleEditTabList.value[MODULE_EDIT_CODE_TEST].tabList = codeData.value.tests.map(
          (test, ix) => ({id: ix + 100, name: test.name})
      )
      appState.value.notifications.push({
        type: "success", title: t(`$vuetify.code_module.edit_tab_information_tests_success_title`),
        text: t(`$vuetify.code_module.edit_tab_information_tests_success_body`),
      })
      props.dismiss(true)
    }
  ).catch((err) => { error.value = err.code })
}
</script>

<template>
  <tr v-for="module in modules.filter(customSearch).sort((m1, m2) => m1.name.localeCompare(m2.name))" :key="module.id">
    <td>#{{ module.id }}</td>
    <td>
      {{ module.name }}
      <TooltipEmojiSpan v-if="module.hidden" emoji="👻" />
    </td>
    <td>{{ module.author }}</td>
    <td>{{ module.type }}</td>
    <td>
      <div class="d-flex flex-wrap" style="gap: 3px; padding: 5px">
        <v-chip v-for="topic in module.topics" :key="topic.id" class="mr-2" :color="topicChipColor(topic)">
          {{ topic.name }}
        </v-chip>
      </div>
    </td>
    <td>
      <div class="d-flex flex-wrap" style="gap: 3px; padding: 5px">
        <v-chip v-for="subject in module.subjects" :key="subject.id" class="mr-2" :color="subjectChipColor(subject)">
          {{ subject.code }}
        </v-chip>
      </div>
    </td>
    <td>
      <div class="d-flex">
        <template v-if="lesson">
          <router-link class="text-decoration-none" style="display: grid" :to="new Nav.LessonModuleEdit(lesson, module).routerPath()" target="_blank">
            <TooltipIconButton icon="mdi-eye"
                               tooltip="$vuetify.lesson_edit_modules_read" color="blue" />
          </router-link>
          <TooltipIconButton icon="mdi-console" color="rgb(var(--v-theme-anchor))" tooltip="$vuetify.action_test" @click="testModule(module)" />
          <TooltipIconButton v-if="(module.author === userStore.user.username)" icon="mdi-delete" color="red"
                             @click="moduleToDelete = module; moduleDeleteDialog = true" />
          <div v-else style="display: flex; align-items: center; justify-content: center; width: 48px; height: 48px">
            <v-icon icon="mdi-delete" color="grey" size="24" style="align-self: center" />
            <v-tooltip activator="parent" location="top">
              {{ t('$vuetify.module_list_cannot_delete') }}
            </v-tooltip>
          </div>
          <TooltipIconButton icon="mdi-content-copy" color="rgb(var(--v-theme-anchor))" @click="copyModule(module)" />
          <!--        Button for adding module to lesson-->
          <TooltipIconButton v-if="((userStore.isTeacher || userStore.isAdmin || module.author === userStore.user.username ||
                               module.editors.includes(userStore.user.username)) && !lesson.modules.some((mod) => mod.id === module.id))" icon="mdi-plus" tooltip="$vuetify.action_add"
                             color="rgb(var(--v-theme-anchor))" @click="addModule(module)" />
          <div v-else style="display: flex; align-items: center; justify-content: center; width: 48px; height: 48px">
            <v-icon icon="mdi-plus" color="grey" size="24" style="align-self: center" />
            <v-tooltip activator="parent" location="top">
              {{ t('$vuetify.module_list_cannot_add') }}
            </v-tooltip>
          </div>
        </template>
        <!--        Button for using tests from module -->
        <TooltipIconButton v-else icon="mdi-plus" tooltip="$vuetify.template_module_test_title"
                           color="rgb(var(--v-theme-anchor))" @click="useAsTemplate(module)" />
      </div>
    </td>
  </tr>
</template>
