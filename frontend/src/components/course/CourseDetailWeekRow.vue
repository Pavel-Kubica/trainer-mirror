<script setup>
import { inject } from 'vue'
import { useLocale } from 'vuetify'
import Vuedraggable from 'vuedraggable'
import { weekApi } from '@/service/api'
import { hasTouchScreen } from '@/plugins/constants'
import { useUserStore } from '@/plugins/store'
import * as Nav from '@/service/nav'
import CourseDetailLessonRow from '@/components/course/CourseDetailLessonRow.vue'
import TooltipIconButton from '@/components/custom/TooltipIconButton.vue'

const appState = inject('appState')
const courseUser = inject('courseUser')
const userStore = useUserStore()
const { current, t } = useLocale()
const props = defineProps(['week', 'isTeacher'])

const copyDialog = inject('copyDialog')
const deleteDialog = inject('deleteDialog')
const createEditDialog = inject('createEditDialog')
const copyLesson = inject('copyLessonEntity')
const copyWeek = inject('copyWeekEntity')
const deleteLesson = inject('deleteLessonEntity')
const deleteWeek = inject('deleteWeekEntity')
const editWeek = inject('editWeekEntity')

const translate = (x, param = undefined) => t(`$vuetify.course_detail_week_lesson_order_changed_${x}`, param)
const moveLesson = (e) => {
  if (e.removed) return // ignore change
  weekApi.editWeekLessonOrder(props.week.id, props.week.lessons.map((l) => l.id))
      .then(() => { appState.value.notifications.push({
        type: "success", title: translate('title'), text: translate('text')
      }) })
      .catch((err) => { appState.value.notifications.push({
        type: "error", title: translate('error_title'), text: translate('error_text', err.code)
      }) })
}
</script>

<template>
  <tr style="cursor: pointer" @click="userStore.hiddenWeeks[week.id] = !userStore.hiddenWeeks[week.id]">
    <th v-ripple.center colspan="5">
      <div class="d-flex justify-space-between align-center" style="gap: 16px">
        <span class="ps-2">{{ week.name }}</span>
        <span v-if="userStore.anonymous && week.lessons.some((l) => l.hidden)" class="font-weight-regular">
          ({{ week.lessons.filter((l) => l.hidden).length }} 👻)
          <v-tooltip activator="parent" location="top">{{ t('$vuetify.hidden_lesson_count') }}</v-tooltip>
        </span>
        <span class="flex-grow-1 text-center">
          {{ new Date(week.from).toLocaleDateString(current === 'customCs' ? 'cs' : 'en') }}
          – 
          {{ new Date(week.until).toLocaleDateString(current === 'customCs' ? 'cs' : 'en') }}
        </span>
        <span v-if="isTeacher" style="display: inline">
          <TooltipIconButton icon="mdi-pencil" color="black"
                             @click="(e) => { e.stopPropagation(); editWeek = week; createEditDialog = true }" />
          <v-btn color="black" variant="text" :to="new Nav.LessonCreate(week).routerPath()"
                 icon @click="(e) => e.stopPropagation()">
            <v-icon icon="mdi-plus" />
            <v-tooltip activator="parent" location="top">{{ t('$vuetify.course_detail_header_new_lesson') }}</v-tooltip>
          </v-btn>
          <TooltipIconButton icon="mdi-content-copy" color="black"
                             @click="(e) => { e.stopPropagation(); copyLesson = null; copyWeek = week; copyDialog = true }" />
          <TooltipIconButton icon="mdi-delete" color="black"
                             @click="(e) => { e.stopPropagation(); deleteLesson = null; deleteWeek = week; deleteDialog = true }" />
        </span>
        <v-btn variant="text" :icon="userStore.hiddenWeeks[week.id] ? 'mdi-chevron-down' : 'mdi-chevron-up'" />
      </div>
    </th>
  </tr>
  <Vuedraggable v-show="!userStore.hiddenWeeks[week.id]" tag="tbody" item-key="id" :disabled="!isTeacher || courseUser" group="lessons"
                :list="week.lessons" :handle="hasTouchScreen() ? '.handle-lesson-row-btn' : '.handle-lesson-row'" @change="moveLesson">
    <template #item="{ element: lesson }">
      <CourseDetailLessonRow v-if="!userStore.anonymous || !lesson.hidden" class="handle-lesson-row" :lesson="lesson" />
    </template>
  </Vuedraggable>
</template>
