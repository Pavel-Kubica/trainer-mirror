<script setup>
import {ref, watch, inject, onBeforeMount, computed} from 'vue'
import {getIconByLessonType, hasTouchScreen, isSolvable, moduleBorder, ruleBorder} from '@/plugins/constants'
import Vuedraggable from 'vuedraggable'
import { useUserStore } from '@/plugins/store'
import * as Nav from '@/service/nav'
import {lessonApi, lessonUserApi} from '@/service/api'
import TooltipIconButton from '@/components/custom/TooltipIconButton.vue'
import { useLocale } from 'vuetify'
import LeftDrawerModuleRow from "@/components/lesson/LeftDrawerModuleRow.vue";


const userStore = useUserStore()
const { t } = useLocale()
const props = defineProps(['lessonId', 'moduleId','scoringRuleId', 'userId'])

const addModuleDialog = inject('addModuleDialog', undefined);
const addModuleTargetLessonId = inject('addModuleTargetLessonId', undefined);

const selectedItem = ref(null)
const week = ref(null)
const lessonUser = ref(null)
const error = ref(null)
//const tabActive = ref(0)
const reloadWeekDetail = () => {
  const promise = props.userId ? lessonUserApi.lessonWeekDetailTeacher(props.lessonId, props.userId)
      : lessonApi.lessonWeekDetail(props.lessonId)
  promise
      .then((result) => {
        week.value = result;
        userStore.unfoldedLessons[props.lessonId] = true;
        lessonUser.value = props.userId ? {id: props.userId} : null
      })
      .catch((err) => { error.value = err.code; console.log(err) })
}

const reorderLessonModules = (lesson) => {
  lessonApi.editLessonModuleOrder(lesson.id, lesson.modules.map((module) => module.id))
}

const editingMode = computed(() => userStore.isTeacher(week?.value?.course) && !props.userId && !userStore.anonymous)

const modulesReloadHook = inject('modulesReloadHook')
watch(() => props.lessonId, () => userStore.unfoldedLessons[props.lessonId] = true)
watch(modulesReloadHook, reloadWeekDetail)
onBeforeMount(reloadWeekDetail)
</script>

<template>
  <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0&icon_names=sports_score">
  <!-- :key re-renders the component upon navigating through the sidebar. If we don't do this, the layout crumbles in solution view. Changing the key should not lead to additional API calls. -->
  <v-list v-if="week" :key="userId ? $route.fullPath : 'LessonDetailLeftDrawerKey'" v-model="selectedItem" density="comfortable">
    <v-list-item v-if="!lessonUser" :to="new Nav.CourseDetail(week.course).routerPath()">
      <template #prepend>
        <v-icon icon="mdi-arrow-left" />
      </template>
      <strong>{{ week.course.subject?.code }} ({{ week.course.shortName }})</strong>
      <template v-if="lessonUser" #append>
        <TooltipIconButton icon="mdi-eye" tooltip="$vuetify.lesson_user_list_left_drawer_module" class="ms-4" density="compact"
                           icon-size="18" :to="new Nav.ModuleDetail({id: lessonId}, {id: moduleId ?? -1}).routerPath()" />
      </template>
    </v-list-item>

    <template v-for="lesson in week.lessons" :key="lesson.id">
      <v-list-item v-show="!lesson.hidden || !userStore.anonymous"
                   :to="lessonUser ? new Nav.LessonUserDetail(lesson, lessonUser).routerPath()
                     : new Nav.LessonDetail(lesson).routerPath()"
                   :active="!moduleId && !scoringRuleId && lessonId === `${lesson.id}`"
                   @click="userStore.unfoldedLessons[lesson.id] = true">
        {{ lesson.name }}
        <span v-if="userStore.anonymous && lesson.modules.some((m) => m.hidden)">
          ({{ lesson.modules.filter((m) => m.hidden).length }} 👻)
          <v-tooltip activator="parent" location="top">{{ t('$vuetify.hidden_module_count') }}</v-tooltip>
        </span>
        <template #prepend>
          <v-icon size="18" :icon="getIconByLessonType(lesson.type)" />
        </template>
        <template #append>
          <template v-if="editingMode">
            <TooltipIconButton icon="mdi-pencil" class="ms-4" density="compact"
                               :to="new Nav.LessonEdit(lesson).routerPath()" />
          </template>
          <template v-if="userStore.isTeacher(week.course) && lessonUser">
            <TooltipIconButton icon="mdi-table" class="ms-4" density="compact" :tooltip="t('$vuetify.progress_overview')"
                               :to="new Nav.LessonProgressOverview(lesson, t).routerPath()" />
          </template>
          <v-btn density="compact" variant="text"
                 :icon="userStore.unfoldedLessons[lesson.id] ? 'mdi-chevron-up' : 'mdi-chevron-down'"
                 @click.stop.prevent="userStore.unfoldedLessons[lesson.id] = !userStore.unfoldedLessons[lesson.id]" />
        </template>
      </v-list-item>

      <v-expand-transition>
        <div v-if="userStore.unfoldedLessons[lesson.id]">
          <template v-if="!userId">
            <v-list-item v-for="scoringRule in lesson.scoringRules" :key="scoringRule.id"
                         :to="(lessonUser ? new Nav.ScoringRuleUserDetail(lesson, scoringRule, lessonUser)
                           : new Nav.ScoringRuleDetail(lesson, scoringRule)).routerPath()"
                         :active="lessonId === `${lesson.id}` && scoringRuleId === `${scoringRule.id}` "
                         :style="{'padding-inline-start': '16px !important', 'border-left': ruleBorder(lesson, scoringRule)}">
              <div class="d-flex align-center" style="gap: 3px;">
                <div class="material-symbols-outlined" style="font-size: 24px; color:#7f7f7f;">
                  sports_score
                </div>
                {{ scoringRule.name }}
              </div>
            </v-list-item>
          </template>

          <Vuedraggable :list="lesson.modules" item-key="id" :disabled="!editingMode"
                        :handle="hasTouchScreen() ? '.module-row-handle-text' : '.module-row-handle'"
                        @change="(e) => e.moved ? reorderLessonModules(lesson) : {}">
            <template #item="{ element: module }">
              <v-list-item v-if="!userId || isSolvable(module)" v-show="!module.hidden || !userStore.anonymous"
                           :to="(lessonUser ? new Nav.LessonSolutionsUser(lesson, lessonUser, module)
                             : new Nav.ModuleDetail(lesson, module)).routerPath()"
                           :active="lessonId === `${lesson.id}` && moduleId === `${module.id}`"
                           :style="{'padding-inline-start': '16px !important', 'border-left': moduleBorder(lesson, module)}">
                <LeftDrawerModuleRow :lesson="lesson" :module="module" class="module-row-handle" :is-teacher="userStore.isTeacher(week.course)" :user-id="userId" />
              </v-list-item>
            </template>
          </Vuedraggable>

          <template v-if="editingMode">
            <v-list-item-action class="d-flex justify-center ma-2">
              <v-btn color="green" size="small" width="192" variant="tonal" :to="new Nav.ModuleCreate(lesson).routerPath()">
                {{ t('$vuetify.lesson_edit_modules_create') }}
              </v-btn>
            </v-list-item-action>
            <v-list-item-action class="d-flex justify-center ma-2">
              <v-btn color="purple" size="small" width="192" variant="tonal" @click="addModuleDialog = true; addModuleTargetLessonId = lesson.id">
                {{ t('$vuetify.lesson_edit_modules_add') }}
              </v-btn>
            </v-list-item-action>
          </template>
        </div>
      </v-expand-transition>
    </template>
  </v-list>
</template>
