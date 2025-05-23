<script setup>
import {inject} from 'vue'
import {courseApi, lessonModuleApi, moduleApi, scoringRuleApi, userApi} from '@/service/api'
import { useUserStore } from '@/plugins/store'
import TooltipIconButton from '@/components/custom/TooltipIconButton.vue'
import TooltipEmojiSpan from '@/components/custom/TooltipEmojiSpan.vue'
import * as Nav from '@/service/nav'
import {useLocale} from "vuetify";
import codeApiClient from "@/service/codeApi";
import {
  DEFAULT_CODE_CONTENT, MODULE_EDIT_CODE_TEST,
  MODULE_FILE_CODE
} from "@/plugins/constants";

const props = defineProps(['customSearch', 'dismiss', 'reloadDialog', 'reloadSource', 'topicChipColor', 'subjectChipColor'])
const userStore = useUserStore()
const { t } = useLocale()

const codeData = inject('codeData')
const lessonModules = inject('lessonModules')
const lesson = inject('lesson')
const scoringRule = inject('scoringRule')
const error = inject('error')
const appState = inject('appState')
const moduleEditTabList = inject('moduleEditTabList')

const copyModule = async (lessonModule) => {
  lessonModules.value = null
  lessonModuleApi.copyLessonModule(lesson.value.id, {
    copiedId: lessonModule.module.id,
    order: (lesson.value.modules.slice(-1)[0]?.order ?? 0) + 1
  })
      .then(() => props.dismiss(true))
      .catch((err) => { error.value = err.code })
}

const deleteModule = async (lessonModule) => {
  lessonModules.value = null
  moduleApi.deleteModule(lessonModule.module.id)
      .then(props.reloadDialog)
      .catch((err) => { error.value = err.code })
}

const testModule = async(lessonModule) => {
  lessonModules.value = null
  let sandboxCourse = await userApi.getSandbox(userStore.user.id)
  if (!sandboxCourse)
    sandboxCourse = await courseApi.createSandbox(userStore.user.id)
  lessonModuleApi.putLessonModule(sandboxCourse.weeks[0].lessons[0].id, lessonModule.module.id, {
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

const addModule = async (lessonModule) => {
  lessonModules.value = null
  scoringRuleApi.putScoringRuleModule(scoringRule.value.id, lessonModule.id)
      .then(() => {
        props.reloadSource()
        props.reloadDialog()
      })
      .catch((err) => { error.value = err.code })
}

const useAsTemplate = async (lessonModule) => {
  codeApiClient.codeDetail(lessonModule.module.id).then(
      (response) => {
        codeData.value = {
          codeType: response.codeType,  interactionType: 'EDITOR', codeHidden: response.codeHidden,  fileLimit: response.fileLimit, referencePublic: false,
          envelopeType: response.envelopeType, customEnvelope: response.customEnvelope, libraryType: response.libraryType, assignment: '', tests: response.tests,
          files: [{name: MODULE_FILE_CODE, tmp: true, id: -1, codeLimit: 1024, content: DEFAULT_CODE_CONTENT, reference: '', headerFile: response.headerFile}],
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
        props.reloadSource()
        props.reloadDialog()
      }
  ).catch((err) => { error.value = err.code })
}


function isModuleInScoringRule(lessonModule) {
  return scoringRule.value.modules.some((mod) => mod.id === lessonModule.module.id);
}

</script>

<template>
  <tr v-for="lessonModule in lessonModules.filter(customSearch)" :key="lessonModule.id">
    <td>#{{ lessonModule.module.id }}</td>
    <td>
      {{ lessonModule.module.name }}
      <TooltipEmojiSpan v-if="lessonModule.module.hidden" emoji="👻" />
    </td>
    <td>{{ lessonModule.module.author }}</td>
    <td>{{ lessonModule.module.type }}</td>
    <td>
      <div class="d-flex flex-wrap" style="gap: 3px; padding: 5px">
        <v-chip v-for="topic in lessonModule.module.topics" :key="topic.id" class="mr-2" :color="topicChipColor(topic)">
          {{ topic.name }}
        </v-chip>
      </div>
    </td>
    <td>
      <div class="d-flex flex-wrap" style="gap: 3px; padding: 5px">
        <v-chip v-for="subject in lessonModule.module.subjects" :key="subject.id" class="mr-2" :color="subjectChipColor(subject)">
          {{ subject.code }}
        </v-chip>
      </div>
    </td>
    <td>
      <div class="d-flex justify-end">
        <router-link class="text-decoration-none" :to="new Nav.ModuleRead(lessonModule.module).routerPath()">
          <TooltipIconButton v-show="lesson" icon="mdi-eye"
                             tooltip="$vuetify.lesson_edit_modules_read" color="blue" />
        </router-link>
        <TooltipIconButton v-show="lesson" icon="mdi-console" color="rgb(var(--v-theme-anchor))" tooltip="$vuetify.action_test" @click="testModule(lessonModule)" />
        <TooltipIconButton v-show="lesson && (lessonModule.module.author === userStore.user.username)" icon="mdi-delete" color="red"
                           @click="deleteModule(lessonModule)" />
        <TooltipIconButton v-show="lesson" icon="mdi-content-copy" color="rgb(var(--v-theme-anchor))" @click="copyModule(lessonModule)" />
        <TooltipIconButton v-show="(lesson && (userStore.isTeacher || userStore.isAdmin || lessonModule.module.author === userStore.user.username ||
                             lessonModule.module.editors.includes(userStore.user.username)) && !isModuleInScoringRule(lessonModule))" icon="mdi-plus" tooltip="$vuetify.action_add"
                           color="rgb(var(--v-theme-anchor))" @click=" addModule(lessonModule)" />
        <TooltipIconButton v-if="!lesson && (lessonModule.module.author === userStore.user.username ||
                             lessonModule.module.editors.includes(userStore.user.username))" icon="mdi-plus" tooltip="$vuetify.template_module_test_title"
                           color="rgb(var(--v-theme-anchor))" @click="useAsTemplate(lessonModule)" />
        <v-btn v-else variant="text" style="visibility: hidden" />
      </div>
    </td>
  </tr>
</template>


