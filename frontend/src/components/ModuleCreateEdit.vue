<script setup>
import {computed, inject, onMounted, provide, ref} from 'vue'
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
import TemplateCreateEdit from "@/components/templates/TemplateCreateEdit.vue";

const appState = inject('appState')
const userStore = useUserStore()
const props = defineProps(['lessonId', 'moduleId', 'readOnly'])
const {t} = useLocale()
const creating = computed(() => !props.moduleId)

const lesson = ref(null)
const teachers = ref([])
const module = ref(null)
const error = ref(null)
const topics = ref([])
const alreadySelectedTopics = ref([])
const subjects = ref([])
const alreadySelectedSubjects = ref([])

const moduleData = ref(null)
const createEditCallback = ref(() => { })
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

const routerNext = ref(() => {})
const unsavedChangesDialog = ref(false)
provide('routerNext', routerNext)
provide('unsavedChangesDialog', unsavedChangesDialog)

const reload = async (moduleId, depends) => {
  moduleApi.moduleDetail(moduleId ?? props.moduleId)
      .then((result) => {
        result.depends = depends ?? module.value.depends
        module.value = moduleData.value = result
        alreadySelectedTopics.value = moduleTopics.value
        alreadySelectedSubjects.value = moduleSubjects.value
      })
      .catch((err) => {
        error.value = err.code
      })
}

const submitAllowed = computed(() => {
  if (props.readOnly === true) {
    return false
  }
  return moduleData.value && moduleData.value.name && moduleData.value.type && moduleSubjects.value.length;
})

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
  if (!submitAllowed.value)
    return false
  const moduleDataVal = moduleData.value
  moduleData.value = null // display progress
  if (moduleDataVal.type === 'TEMPLATE') {
    createEditCallback.value(moduleDataVal)
    unsavedChangesDialog.value = false
    appState.value.notifications.push({
      type: "success", title: t(`$vuetify.template_create_notification_title`),
      text: t(`$vuetify.template_create_notification_text`),
    })
    await router.push(new Nav.LessonEdit(lesson.value).routerPath())
    await reload()
  } else {
    const promise = creating.value ? moduleApi.createModule(moduleDataVal) : moduleApi.editModule(props.moduleId, moduleDataVal)
    promise.then((resultModule) => {
      const promises = []
      if (creating.value || lesson.value.modules.some((mod) => mod.id === +props.moduleId)) { // only try to put lessonmodule if it already exists, or we are creating a new module
        promises.push(lessonModuleApi.putLessonModule(props.lessonId, resultModule.id, {
          order: creating.value ? lesson.value.modules.length : null,
          depends: moduleDataVal.depends
        }))
      }
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
              if (creating.value)
                await moduleApi.deleteModule(resultModule.id) // rollback
              error.value = err.code
              moduleData.value = moduleDataVal
              return
            }

            const key = creating.value ? 'create' : 'edit'
            appState.value.notifications.push({
              type: "success", title: t(`$vuetify.module_${key}_notification_title`),
              text: t(`$vuetify.module_${key}_notification_text`),
            })
            if (creating.value) await router.push(new Nav.LessonModuleEdit(lesson.value, resultModule).routerPath())
            await reload(resultModule.id, moduleDataVal.depends)
          })
          .catch((err) => error.value = err.code)
    }).catch((err) => error.value = err.response.status === 412 ? 'ERR_CONCURRENT' : err.code)
  }
}

onMounted(async () => {
  lessonApi.lessonDetailTeacher(props.lessonId)
      .then(async (resultLesson) => {
        const depends = resultLesson.modules.find((mod) => mod.id === parseInt(props.moduleId))?.depends ?? -1
        lesson.value = resultLesson
        if (creating.value)
          moduleSubjects.value.push(resultLesson.week.course.subject.id)
        lesson.value.modules = [{
          id: -1,
          name: t('$vuetify.module_edit_no_depend')
        }].concat(resultLesson.modules)
        if (props.readOnly === true) {
          teachers.value = []
        }
        if (props.readOnly === false) {
          teachers.value = props.moduleId ? await moduleApi.moduleTeachers(props.moduleId) : await courseUserApi.courseTeachers(resultLesson.week.course.id)
        }
        const [top, sub] = await Promise.all([
          topicApi.listTopics(),
          subjectApi.listSubjects()
        ]);
        topics.value = top;
        subjects.value = sub;

        if (props.moduleId) {
          moduleApi.moduleDetail(props.moduleId)
              .then((moduleResult) => {
                appState.value.navigation = [new Nav.CourseList(), new Nav.CourseDetail(resultLesson.week.course),
                  new Nav.LessonDetail(resultLesson, resultLesson.week), new Nav.LessonModuleEdit(resultLesson, moduleResult)]
                moduleResult.depends = depends
                module.value = moduleResult
                moduleData.value = Object.assign({}, moduleResult)
              })
              .catch((err) => {
                error.value = err.code
              })

          const [selectedTopics, selectedSubjects] = await Promise.all([
            moduleApi.getModuleTopics(props.moduleId),
            moduleApi.getModuleSubjects(props.moduleId)
          ])
          alreadySelectedTopics.value = moduleTopics.value = selectedTopics;
          alreadySelectedSubjects.value = moduleSubjects.value = selectedSubjects;
        } else {
          appState.value.navigation = [new Nav.CourseList(), new Nav.CourseDetail(resultLesson.week.course),
            new Nav.LessonDetail(resultLesson, resultLesson.week), new Nav.ModuleCreate(resultLesson)]
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
  document.addEventListener("keydown", handleCtrlS)

  onBeforeRouteLeave((to, from, next) => {
    routerNext.value = () => { if (to.name !== 'module-edit') document.removeEventListener("keydown", handleCtrlS); next(); }

    const newModuleDataComparable = creating.value ? undefined : Object.fromEntries(Object.entries(moduleData.value).filter(([key]) => key !== 'file'))
    if (creating.value || props.readOnly || (JSON.stringify(module.value) === JSON.stringify(newModuleDataComparable)))
    {
      routerNext.value()
      return
    }
    // Unsaved changes
    appState.value.leftDrawer = true
    unsavedChangesDialog.value = true
  })
})
const handleCtrlS = (event) => {
  if (event.ctrlKey && event.key.toLowerCase() === 's') {
    if (event.repeat)
      return;
    event.stopPropagation();
    event.preventDefault();
    submit();
  }
}
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
              <span>
                <v-btn class="mb-2 mx-2" type="submit" color="blue" size="large" variant="tonal"
                       :block="true" :disabled="!submitAllowed">
                  {{ t(`$vuetify.action_${module ? 'edit' : 'create'}`) }}
                </v-btn>
                <v-tooltip v-if="!submitAllowed && !readOnly" activator="parent" location="top">
                  {{ t('$vuetify.module_edit_not_allowed') }}
                </v-tooltip>
              </span>
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
