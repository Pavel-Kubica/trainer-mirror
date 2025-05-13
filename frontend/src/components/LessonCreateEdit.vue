<script setup>
import {ref, inject, onMounted, provide} from 'vue'
import { useLocale } from 'vuetify'
import { onBeforeRouteLeave } from 'vue-router'
import router from '@/router'
import { courseApi, lessonApi } from '@/service/api'
import { useUserStore } from '@/plugins/store'
import * as Nav from '@/service/nav'
import {
  LESSON_EDIT_ITEMS, LESSON_EDIT_ITEM_INFO, LESSON_EDIT_ITEM_TEXT, LESSON_EDIT_ITEM_RULE
} from '@/plugins/constants'

import TextEditor from '@/components/custom/TextEditor.vue'
import LoadingScreen from '@/components/custom/LoadingScreen.vue'
import LessonCreateEditMenu from '@/components/lesson/edit/LessonCreateEditMenu.vue'
import LessonCreateEditPartLesson from '@/components/lesson/edit/LessonCreateEditPartLesson.vue'
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

const weekStart = ref(null)
const weekEnd = ref(null)

// Just create lesson
const createLesson = () => { return lessonApi.createLesson(lessonData.value) }
const editLesson = () => {
  // Edit module order + edit lesson
  return lessonApi.editLesson(lesson.value.id, lessonData.value)
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
        else await router.push(new Nav.LessonDetail(result).routerPath())
      })
      .catch((err) => {
        error.value = err.code
      })
}

const setDefaultStartEnd = () => {
  lessonData.value.timeStart = weekStart.value;
  lessonData.value.timeLimit = weekEnd.value;
  lessonData.value.referenceSolutionAccessibleFrom = weekEnd.value;
}
provide('setDefaultStartEnd', setDefaultStartEnd);

onMounted(async () => {
  console.log("loading")
  if (props.courseId && props.weekId) {
    courseApi.courseWeekDetail(props.courseId, props.weekId)
        .then((result) => {
          week.value = result
          lesson.value = null
          weekStart.value = result.from;
          weekEnd.value = new Date(result.until);
          weekEnd.value.setHours(23,59,59);
          appState.value.navigation = [new Nav.CourseList(), new Nav.CourseDetail(result.course)]
          if (week.value.lessons.length)
            appState.value.navigation.push(new Nav.LessonDetail(week.value.lessons[0], week.value))
          appState.value.navigation.push(new Nav.LessonCreate(result))
          lessonData.value = {
            weekId: result.id, name: "", hidden: true, order: result.lessons.length + 1, type: 'TUTORIAL',
            lockCode: null, description: ''
          }
          setDefaultStartEnd()
          console.log("loaded")
        })
        .catch((err) => { error.value = err.code })
  }
  else {
    lessonApi.lessonDetailTeacher(props.lessonId)
        .then((result) => {
          lesson.value = result
          week.value = result.week
          weekStart.value = result.week.from;
          weekEnd.value = new Date(result.week.until);
          weekEnd.value.setHours(23,59,59);
          lessonData.value = Object.assign({}, result)
          appState.value.navigation = [new Nav.CourseList(), new Nav.CourseDetail(result.week.course), new Nav.LessonDetail(result, week.value) , new Nav.LessonEdit(result)]
          console.log("loaded")
        })
        .catch((err) => { error.value = err.code })
  }
  document.addEventListener("keydown", handleCtrlS)
})

onBeforeRouteLeave((to, from, next) => {
  routerNext.value = () => { document.removeEventListener("keydown", handleCtrlS); next(); }

  if (JSON.stringify(lesson.value) === JSON.stringify(lessonData.value) || to.name === 'lesson-edit' || to.name === 'lesson-detail') { // ok
    routerNext.value()
    return
  }
  appState.value.leftDrawer = true
  unsavedChangesDialog.value = true
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
