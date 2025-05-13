<script setup>
import {ref, watch, inject, onBeforeMount} from 'vue'
import {getIconByLessonType, moduleBorder, ruleBorder} from '@/plugins/constants'
import { useUserStore } from '@/plugins/store'
import * as Nav from '@/service/nav'
import {lessonApi, lessonUserApi} from '@/service/api'
import ModuleTooltipTitle from '@/components/lesson/ModuleTooltipTitle.vue'
import TooltipIconButton from '@/components/custom/TooltipIconButton.vue'
import { useLocale } from 'vuetify'


const userStore = useUserStore()
const { t } = useLocale()
const props = defineProps(['lessonId', 'moduleId','scoringRuleId', 'user'])
const selectedItem = ref(null)
const week = ref(null)
const lessonUser = ref(null)
const error = ref(null)
//const tabActive = ref(0)

const collapsed = ref([])

const reload = () => {
  const promise = props.user ? lessonUserApi.lessonWeekDetailTeacher(props.lessonId, props.user)
      : lessonApi.lessonWeekDetail(props.lessonId)
  promise
      .then((result) => {
        result.lessons.forEach((l) => {
          const start = new Date(l.timeStart), end = new Date(l.timeLimit), now = new Date()
          collapsed.value[l.id] = props.lessonId !== `${l.id}` && !(start <= now && now <= end)
        })
        week.value = result;
        lessonUser.value = props.user ? {id: props.user} : null
      })
      .catch((err) => { error.value = err.code })
}


const reloadHook = inject('reloadHook')
watch(reloadHook, () => reload())
onBeforeMount(
    () => {
      reload()
    }
)
</script>

<template>
  <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0&icon_names=sports_score">
  <v-list v-if="week" v-model="selectedItem" density="comfortable">
    <v-list-item :to="(lessonUser ? new Nav.CourseUserDetail(week.course, lessonUser)
      : new Nav.CourseDetail(week.course)).routerPath()">
      <template #prepend>
        <v-icon icon="mdi-arrow-left" />
      </template>
      <strong>{{ week.course.subject?.code }} ({{ week.name }})</strong>
      <template v-if="lessonUser" #append>
        <TooltipIconButton icon="mdi-eye" tooltip="$vuetify.lesson_user_list_left_drawer_module" class="ms-4" density="compact"
                           icon-size="18" :to="new Nav.ModuleDetail({id: lessonId}, {id: moduleId ?? -1}).routerPath()" />
      </template>
    </v-list-item>

    <v-list>
      <template v-for="lesson in week.lessons" :key="lesson.id">
        <v-list-item v-show="!lesson.hidden || !userStore.anonymous"
                     :to="(lessonUser ? new Nav.LessonUserDetail(lesson, lessonUser)
                       : new Nav.LessonDetail(lesson)).routerPath()"
                     :active="!moduleId && !scoringRuleId && lessonId === `${lesson.id}`">
          {{ lesson.name }}
          <span v-if="userStore.anonymous && lesson.modules.some((m) => m.hidden)">
            ({{ lesson.modules.filter((m) => m.hidden).length }} 👻)
            <v-tooltip activator="parent" location="top">{{ t('$vuetify.hidden_module_count') }}</v-tooltip>
          </span>
          <template #prepend>
            <v-icon size="18" :icon="getIconByLessonType(lesson.type)" />
          </template>
          <template #append>
            <template v-if="userStore.isTeacher(week.course) && !user">
              <TooltipIconButton v-if="!userStore.anonymous" icon="mdi-pencil" class="ms-4" density="compact" icon-size="18"
                                 color="rgb(var(--v-theme-anchor))" :to="new Nav.LessonEdit(lesson).routerPath()" />
              <TooltipIconButton v-else icon="mdi-eye" density="compact" icon-size="18" color="rgb(var(--v-theme-anchor))"
                                 tooltip="$vuetify.course_detail_lesson_row_student_list"
                                 :to="new Nav.LessonUserList(lesson, t).routerPath()" />
            </template>
            <v-btn v-if="lesson.modules.length" density="compact" variant="text"
                   :icon="!collapsed[lesson.id] ? 'mdi-chevron-up' : 'mdi-chevron-down'"
                   @click.prevent="collapsed[lesson.id] = !collapsed[lesson.id]" />
          </template>
        </v-list-item>
        
        <v-expand-transition>
          <div v-if="!collapsed[lesson.id] && lesson.modules.length">
            <div v-if="lesson.description">
              <template v-for="module in (lesson.description ? [{id: -1}] : [])" :key="module.id">
                <v-list-item v-show="!module.hidden || !userStore.anonymous"
                             :to="(lessonUser ? new Nav.ModuleUserDetail(lesson, module, lessonUser)
                               : new Nav.ModuleDetail(lesson, module)).routerPath()"
                             :active="lessonId === `${lesson.id}` && moduleId === `${module.id}`"
                             :style="{'padding-inline-start': '32px !important', 'border-left': moduleBorder(lesson, module)}">
                  <ModuleTooltipTitle :lesson="lesson" :module="module" />
                </v-list-item>
              </template>
            </div>

            <template v-for="scoringRule in lesson.scoringRules" :key="scoringRule.id">
              <v-list-item
                :to="(lessonUser ? new Nav.ScoringRuleUserDetail(lesson, scoringRule, lessonUser)
                  : new Nav.ScoringRuleDetail(lesson, scoringRule)).routerPath()"
                :active="lessonId === `${lesson.id}` && scoringRuleId === `${scoringRule.id}` "
                :style="{'padding-inline-start': '32px !important', 'border-left': ruleBorder(lesson, scoringRule)}">
                <div class="d-flex align-center" style="gap: 3px;">
                  <div class="material-symbols-outlined" style="font-size: 24px; color:#7f7f7f;">
                    sports_score
                  </div>
                  {{ scoringRule.name }}
                </div>
              </v-list-item>
            </template>


            <template v-for="module in lesson.modules" :key="module.id">
              <v-list-item v-show="!module.hidden || !userStore.anonymous"
                           :to="(lessonUser ? new Nav.ModuleUserDetail(lesson, module, lessonUser)
                             : new Nav.ModuleDetail(lesson, module)).routerPath()"
                           :active="lessonId === `${lesson.id}` && moduleId === `${module.id}`"
                           :style="{'padding-inline-start': '32px !important', 'border-left': moduleBorder(lesson, module)}">
                <ModuleTooltipTitle :lesson="lesson" :module="module" />
              </v-list-item>
            </template>
          </div>
        </v-expand-transition>
      </template>
    </v-list>
  </v-list>
</template>
