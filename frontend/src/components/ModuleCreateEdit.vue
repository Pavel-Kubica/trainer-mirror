<script setup>
import {inject, onMounted, provide, ref} from 'vue'
import {useLocale} from 'vuetify'
import {useUserStore} from '@/plugins/store'
import {courseUserApi, lessonApi, lessonModuleApi, moduleApi, topicApi, subjectApi} from '@/service/api'
import router from '@/router'
import * as Nav from '@/service/nav'

import CodeEditModule from '@/components/modules/CodeEditModule.vue'
import QuizEditModule from '@/components/modules/QuizEditModule.vue'
import TextEditor from '@/components/custom/TextEditor.vue'
import {MODULE_EDIT_ITEMS, MODULE_EDIT_ITEM_INFO, MODULE_EDIT_ITEM_TEXT, getErrorMessage} from '@/plugins/constants'
import LoadingScreen from '@/components/custom/LoadingScreen.vue'
import ModuleCreateEditMenu from '@/components/lesson/edit/ModuleCreateEditMenu.vue'
import ModuleCreateEditPartModule from '@/components/lesson/edit/ModuleCreateEditPartModule.vue'
import {onBeforeRouteLeave} from "vue-router";
import UnsavedChangesDialog from "@/components/lesson/UnsavedChangesDialog.vue";
import DiscussionMenu from "@/components/modules/discussion/DiscussionMenu.vue";
import SelftestEditModule from "@/components/modules/SelftestEditModule.vue";
import quizApi from "@/service/quizApi";
import TemplateCreateEdit from "@/components/templates/TemplateCreateEdit.vue";

const appState = inject('appState')
const userStore = useUserStore()
const props = defineProps(['lessonId', 'moduleId', 'readOnly'])
const {t} = useLocale()

const lesson = ref(null)
const teachers = ref([])
const module = ref(null)
const error = ref(null)
const topics = ref([])
const alreadySelectedTopics = ref([])
const subjects = ref([])
const alreadySelectedSubjects = ref([])

const moduleData = ref(null)
const createEditCallback = ref((module) => {
  console.log(module)
})
const moduleTopics = ref([])
const moduleSubjects = ref([])
provide('moduleData', moduleData)
provide('createEditCallback', createEditCallback)
provide('moduleTopics', moduleTopics)
provide('moduleSubjects', moduleSubjects)

const moduleEditTabList = ref({
  [MODULE_EDIT_ITEM_INFO]: MODULE_EDIT_ITEMS[MODULE_EDIT_ITEM_INFO],
  [MODULE_EDIT_ITEM_TEXT]: MODULE_EDIT_ITEMS[MODULE_EDIT_ITEM_TEXT]
})
const moduleEditItem = ref(MODULE_EDIT_ITEMS[
userStore.moduleEditItemIds[props.moduleId ?? -1] ?? MODULE_EDIT_ITEM_INFO
    ] ?? MODULE_EDIT_ITEMS[MODULE_EDIT_ITEM_INFO])
provide('moduleEditTabList', moduleEditTabList)
provide('moduleEditItem', moduleEditItem)

const routerNext = ref(() => {
})
provide('routerNext', routerNext)
const unsavedChangesDialog = ref(false)
provide('unsavedChangesDialog', unsavedChangesDialog)

const reload = async (moduleId, depends) => {
  moduleApi.moduleDetail(moduleId ?? props.moduleId)
      .then((result) => {
        result.depends = depends ?? module.value.depends
        module.value = moduleData.value = result
        alreadySelectedTopics.value = moduleTopics.value
        alreadySelectedSubjects.value = moduleSubjects.value
        if (moduleData.value.type === 'QUIZ') {
          quizApi.quizDetailByModule(moduleId ?? props.moduleId)
        }
      })
      .catch((err) => {
        error.value = err.code
      })
}

const submitAllowed = () => {
  if (props.readOnly === true) {
    return false
  }
  return module.value || (moduleData.value && moduleData.value.name && moduleData.value.type)
}

const processTags = (tagsBefore, tagsAfter, promises, moduleId, funcDelete, funcAdd) => {
  let topicsToDelete = new Set([])
  let topicsToAdd = []

  for (const topic in tagsBefore.value)
    topicsToDelete.add(tagsBefore.value[topic])

  for (const topic in tagsAfter.value)
    if (!topicsToDelete.delete(tagsAfter.value[topic]))
      topicsToAdd.push(tagsAfter.value[topic])

  for (const topicId of topicsToDelete)
    promises.push(funcDelete(moduleId, topicId))

  for (const topicId of topicsToAdd)
    promises.push(funcAdd(moduleId, topicId))
}

const submit = async () => {
  if (!submitAllowed())
    return false
  const editing = module.value !== null
  const moduleDataVal = moduleData.value
  moduleData.value = null // display progress
  if (moduleDataVal.type === 'TEMPLATE') {
    createEditCallback.value(moduleDataVal)
    unsavedChangesDialog.value = false
    appState.value.notifications.push({
      type: "success", title: t(`$vuetify.template_create_notification_title`),
      text: t(`$vuetify.template_create_notification_text`),
    })
    console.log(lesson.value)
    await router.push(new Nav.LessonEdit(lesson.value).routerPath())
    await reload()
  } else {
    const promise = editing ? moduleApi.editModule(props.moduleId, moduleDataVal) : moduleApi.createModule(moduleDataVal)
    promise.then((resultModule) => {
      const promises = [lessonModuleApi.putLessonModule(props.lessonId, resultModule.id, {
        order: editing ? null : lesson.value.modules.length,
        depends: moduleDataVal.depends
      })]
      if (moduleDataVal.file)
        promises.push(moduleApi.putModuleFile(resultModule.id, moduleDataVal.file))

      processTags(alreadySelectedTopics, moduleTopics, promises, resultModule.id, moduleApi.deleteModuleTopic, moduleApi.addModuleTopic)
      processTags(alreadySelectedSubjects, moduleSubjects, promises, resultModule.id, moduleApi.deleteModuleSubject, moduleApi.addModuleSubject)

      Promise.all(promises)
          .then(async () => {
            const fn = createEditCallback.value
            try {
              await fn(resultModule)
            } catch (err) {
              await moduleApi.deleteModule(resultModule.id) // rollback
              error.value = err.code
              moduleData.value = moduleDataVal
              return
            }

            const key = editing ? 'edit' : 'create'
            appState.value.notifications.push({
              type: "success", title: t(`$vuetify.module_${key}_notification_title`),
              text: t(`$vuetify.module_${key}_notification_text`),
            })
            if (!editing) await router.push(new Nav.ModuleEdit(resultModule).routerPath())
            await reload(resultModule.id, moduleDataVal.depends)
            if (resultModule.type === 'QUIZ') {
              await reload()
            }
          })
          .catch((err) => error.value = err.code)
    }).catch((err) => error.value = err.response.status === 412 ? 'ERR_CONCURRENT' : err.code)
  }
}

onMounted(async () => {
  lessonApi.lessonDetailTeacher(props.lessonId)
      .then(async (result) => {
        const depends = result.modules.find((mod) => mod.id === parseInt(props.moduleId))?.depends ?? -1
        lesson.value = result
        lesson.value.modules = [{
          id: -1,
          name: t('$vuetify.module_edit_no_depend')
        }].concat(result.modules.filter((mod) => mod.id !== parseInt(props.moduleId)))
        if (props.readOnly === true) {
          teachers.value = []
        }
        if (props.readOnly === false) {
          teachers.value = props.moduleId ? await moduleApi.moduleTeachers(props.moduleId) : await courseUserApi.courseTeachers(result.week.course.id)
        }
        topics.value = await topicApi.listTopics()
        subjects.value = await subjectApi.listSubjects()

        if (props.moduleId) {
          moduleApi.moduleDetail(props.moduleId)
              .then((moduleResult) => {
                appState.value.navigation = [new Nav.CourseList(), new Nav.CourseDetail(result.week.course),
                  new Nav.LessonEdit(result), new Nav.ModuleEdit(moduleResult)]
                moduleResult.depends = depends
                module.value = moduleResult
                moduleData.value = Object.assign({}, moduleResult)
              })
              .catch((err) => {
                error.value = err.code
              })

          alreadySelectedTopics.value = await moduleApi.getModuleTopics(props.moduleId)
          moduleTopics.value = alreadySelectedTopics.value
          alreadySelectedSubjects.value = await moduleApi.getModuleSubjects(props.moduleId)
          moduleSubjects.value = alreadySelectedSubjects.value
        } else {
          appState.value.navigation = [new Nav.CourseList(), new Nav.CourseDetail(result.week.course),
            new Nav.LessonEdit(result), new Nav.ModuleCreate()]
          module.value = null
          moduleData.value = {
            name: '', author: userStore.user, editors: [], minPercent: 100, difficulty: null, assignment: '',
            lockable: false, timeLimit: false, manualEval: false, hidden: true, depends: -1,
            type: null, lastModificationTime: 0, file: null
          }
        }
        moduleData.value.file = null
      })
      .catch((err) => {
        error.value = err.code
      })

  onBeforeRouteLeave((to, from, next) => {
    if ((JSON.stringify(module.value) === JSON.stringify(moduleData.value)) || props.readOnly) { // ok
      next()
      return
    }
    appState.value.leftDrawer = true
    routerNext.value = next
    unsavedChangesDialog.value = true


  })
})

</script>

<template>
  <UnsavedChangesDialog :router-callback="routerNext" :save-callback="submit" />
  <ModuleCreateEditMenu :lesson="lesson" :module="module" />
  <v-main>
    <LoadingScreen :items="moduleData" :error="error">
      <template #items>
        <v-card>
          <v-card-title class="d-flex mt-3">
            <span>{{ t(moduleEditItem.name) }}</span>
            <DiscussionMenu v-if="module && moduleEditItem.name === '$vuetify.module_edit_tab_information'" :module-id="moduleId" />
          </v-card-title>
          <v-card-item>
            <v-form :disabled="props.readOnly" @submit.prevent="submit">
              <ModuleCreateEditPartModule v-if="moduleEditItem.id === MODULE_EDIT_ITEM_INFO"
                                          :lesson="lesson" :teachers="teachers" :topics="topics" :subjects="subjects"
                                          :read-only="props.readOnly" />
              <TextEditor v-else-if="moduleEditItem.id === MODULE_EDIT_ITEM_TEXT" v-model="moduleData.assignment"
                          :editable="!props.readOnly" class="max-height-tab mb-4 assignment-editor" />

              <QuizEditModule v-if="moduleData.type === 'QUIZ'" :module-id="moduleId" :read-only="props.readOnly"
                              :reload="reload" :topics="topics" :subjects="subjects" />
              <SelftestEditModule v-if="moduleData.type === 'SELFTEST'" :module-id="moduleId"
                                  :read-only="props.readOnly" />
              <CodeEditModule v-if="moduleData.type === 'CODE'" :module-id="moduleId" :read-only="props.readOnly" />
              <TemplateCreateEdit v-if="moduleData.type === 'TEMPLATE'" />
              <!-- TextModule / AssignmentModule have no content -->
              <v-btn class="mb-2 mx-2" type="submit" color="blue" size="large" variant="tonal"
                     :block="true" :disabled="!submitAllowed()">
                {{ t(`$vuetify.action_${module ? 'edit' : 'create'}`) }}
              </v-btn>
            </v-form>
          </v-card-item>
        </v-card>
      </template>
      <template #error>
        {{ getErrorMessage(t, error) }}
        <v-btn class="mx-2" variant="tonal" color="warning" @click="error = null">
          {{ t('$vuetify.dialog_try_again') }}
        </v-btn>
      </template>
    </LoadingScreen>
  </v-main>
</template>
