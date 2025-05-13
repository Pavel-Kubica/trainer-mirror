<script setup>
import { ref, inject, onMounted, provide } from 'vue'
import { useLocale } from 'vuetify'
import { onBeforeRouteLeave } from 'vue-router'
import router from '@/router'
import { courseApi, lessonApi, lessonModuleApi } from '@/service/api'
import { useUserStore } from '@/plugins/store'
import * as Nav from '@/service/nav'
import {
  LESSON_EDIT_ITEMS, LESSON_EDIT_ITEM_INFO, LESSON_EDIT_ITEM_TEXT, LESSON_EDIT_ITEM_MOD, LESSON_EDIT_ITEM_RULE
} from '@/plugins/constants'

import TextEditor from '@/components/custom/TextEditor.vue'
import LoadingScreen from '@/components/custom/LoadingScreen.vue'
import LessonCreateEditMenu from '@/components/lesson/edit/LessonCreateEditMenu.vue'
import LessonCreateEditPartLesson from '@/components/lesson/edit/LessonCreateEditPartLesson.vue'
import LessonCreateEditPartModules from '@/components/lesson/edit/LessonCreateEditPartModules.vue'
import UnsavedChangesDialog from '@/components/lesson/UnsavedChangesDialog.vue'
import LessonCreateEditPartRules from "@/components/lesson/edit/LessonCreateEditPartRules.vue";

const appState = inject('appState')
const userStore = useUserStore()
const props = defineProps(['courseId', 'weekId', 'lessonId'])
const { t } = useLocale()

const week = ref(null)
const lesson = ref(null)
const error = ref(null)

const lessonData = ref(null)
provide('lessonData', lessonData)

const lessonEditItem = ref(LESSON_EDIT_ITEMS[
    userStore.lessonEditItemIds[props.lessonId ?? -1] ?? LESSON_EDIT_ITEM_INFO
])
provide('lessonEditItem', lessonEditItem)

const routerNext = ref(() => {})
const unsavedChangesDialog = ref(false)
provide('unsavedChangesDialog', unsavedChangesDialog)

const removeModule = async (module) => {
  lessonData.value = null
  lessonModuleApi.removeLessonModule(lesson.value.id, module.id)
      .then(reload)
      .catch((err) => { error.value = err.code })
}

// Just create lesson
const createLesson = () => { return lessonApi.createLesson(lessonData.value) }
const editLesson = () => {
  // Edit module order + edit lesson
  return Promise.all(lesson.value.modules.map((mod, index) => lessonModuleApi.putLessonModule(
      lesson.value.id, mod.id, { order: index + 1 }
  )).concat([lessonApi.editLesson(lesson.value.id, lessonData.value)]))
}

const reload = async () => {
  lessonApi.lessonDetail(lesson.value.id)
      .then((result) => {
        lesson.value = lessonData.value = result
        week.value = result.week
      })
      .catch((err) => { error.value = err.code })
}

const submitAllowed = () => {
  return week.value && lessonData.value && lessonData.value.name && (lessonData.value.order || lesson.value)
}
const submit = () => {
  if (!submitAllowed())
    return false
  week.value = null // display progress
  const promise = lesson.value ? editLesson() : createLesson()
  promise
      .then(async (result) => {
        const key = lesson.value ? 'edit' : 'create'
        appState.value.notifications.push({
          type: "success", title: t(`$vuetify.lesson_${key}_notification_title`),
          text: t(`$vuetify.lesson_${key}_notification_text`),
        })
        if (lesson.value) await reload()
        else await router.push(new Nav.LessonEdit(result).routerPath())
      })
      .catch((err) => {
        error.value = err.code
      })
}

onMounted(async () => {
  console.log("lesson - ", lesson)
  if (props.courseId && props.weekId) {
    courseApi.courseWeekDetail(props.courseId, props.weekId)
        .then((result) => {
          appState.value.navigation = [new Nav.CourseList(), new Nav.CourseDetail(result.course), new Nav.LessonCreate(result)]
          week.value = result
          lesson.value = null
          lessonData.value = {
            weekId: result.id, name: "", hidden: true, order: result.lessons.length + 1, type: 'TUTORIAL',
            lockCode: null, timeStart: null, timeLimit: null, description: ''
          }
        })
        .catch((err) => { error.value = err.code })
  }
  else {
    lessonApi.lessonDetailTeacher(props.lessonId)
        .then((result) => {
          appState.value.navigation = [new Nav.CourseList(), new Nav.CourseDetail(result.week.course), new Nav.LessonEdit(result)]
          lesson.value = result
          week.value = result.week
          lessonData.value = Object.assign({}, result)
        })
        .catch((err) => { error.value = err.code })
  }

  onBeforeRouteLeave((to, from, next) => {
    if (JSON.stringify(lesson.value) === JSON.stringify(lessonData.value) || to.name === 'lesson-edit') { // ok
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
  <LessonCreateEditMenu :week="week" :lesson="lesson" />
  <v-main>
    <LoadingScreen :items="week" :error="error">
      <template #items>
        <v-card :title="t(lessonEditItem.name)">
          <v-card-item>
            <v-form @submit.prevent="submit">
              <LessonCreateEditPartLesson v-if="lessonEditItem.id === LESSON_EDIT_ITEM_INFO" />
              <TextEditor v-else-if="lessonEditItem.id === LESSON_EDIT_ITEM_TEXT" v-model="lessonData.description"
                          :editable="true" class="max-height-tab mb-4 assignment-editor" />
              <LessonCreateEditPartModules v-else-if="lesson && lessonEditItem.id === LESSON_EDIT_ITEM_MOD"
                                           :error="error" :reload="reload" :remove-module="removeModule" />
              <LessonCreateEditPartRules v-if="lesson!==null && lessonEditItem.id === LESSON_EDIT_ITEM_RULE"
                                         :error="error" :reload="reload" :lesson-id="lessonData.id"
                                         :edit="true" />
              <v-btn class="mb-2 mx-2" type="submit" color="blue" size="large" variant="tonal"
                     :block="true" :disabled="!submitAllowed()">
                {{ t(`$vuetify.action_${lesson ? 'edit' : 'create'}`) }}
              </v-btn>
            </v-form>
          </v-card-item>
        </v-card>
      </template>
    </LoadingScreen>
  </v-main>
</template>
