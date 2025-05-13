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

const deleteDialog = inject('deleteDialog')
const createEditDialog = inject('createEditDialog')
const copyDialog = inject('copyDialog')
const deleteLesson = inject('deleteLessonEntity')
const deleteWeek = inject('deleteWeekEntity')
const editWeek = inject('editWeekEntity')
const copyTargetWeekId = inject('copyTargetWeekId')

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
      <div style="display: grid; width: 100%; height: 48px; grid-template-columns: 0.2fr 1.8fr 60% 2fr; align-items: center">
        <v-btn style="grid-column: 1" variant="text" density="compact" :icon="userStore.hiddenWeeks[week.id] ? 'mdi-chevron-down' : 'mdi-chevron-up'" />
        <span style="grid-column: 2; display: flex; justify-content: space-between">
          <span style="text-align: start" class="pl-4">{{ week.name }}</span>
          <span v-if="userStore.anonymous && week.lessons.some((l) => l.hidden)" class="font-weight-regular">
            ({{ week.lessons.filter((l) => l.hidden).length }} 👻)
            <v-tooltip activator="parent" location="top">{{ t('$vuetify.hidden_lesson_count') }}</v-tooltip>
          </span>
        </span>
        <span style="grid-column: 3" class="text-center">
          {{ new Date(week.from).toLocaleDateString(current === 'customCs' ? 'cs' : 'en') }}
          –
          {{ new Date(week.until).toLocaleDateString(current === 'customCs' ? 'cs' : 'en') }}
        </span>
        <span v-if="isTeacher && !userStore.anonymous" style="grid-column: 4; justify-self: end; display: flex; justify-content: end;">
          <TooltipIconButton icon="mdi-delete"
                             @click="(e) => { e.stopPropagation(); deleteLesson = null; deleteWeek = week; deleteDialog = true }" />
          <TooltipIconButton icon="mdi-pencil"
                             @click="(e) => { e.stopPropagation(); editWeek = week; createEditDialog = true }" />
        </span>
      </div>
    </th>
  </tr>
  <Vuedraggable v-show="!userStore.hiddenWeeks[week.id]" tag="tbody" item-key="id" :disabled="!isTeacher || courseUser" group="lessons"
                :list="week.lessons" :handle="hasTouchScreen() ? '.handle-lesson-row-btn' : '.handle-lesson-row'" @change="moveLesson">
    <template #item="{ element: lesson }">
      <CourseDetailLessonRow v-if="!userStore.anonymous || !lesson.hidden" class="handle-lesson-row" :lesson="lesson" />
    </template>
  </Vuedraggable>
  <v-card-actions v-if="isTeacher && !userStore.anonymous" v-show="!userStore.hiddenWeeks[week.id]">
    <v-btn variant="tonal" density="comfortable" class="ml-3 text-primary"
           :to="new Nav.LessonCreate(week).routerPath()">
      {{ t('$vuetify.course_detail_header_new_lesson') }}
    </v-btn>
    <v-btn variant="tonal" density="comfortable" class="ml-5 text-primary"
           @click="copyTargetWeekId = week.id; copyDialog = true">
      {{ t('$vuetify.course_detail_lesson_copy_title') }}
    </v-btn>
  </v-card-actions>
</template>
