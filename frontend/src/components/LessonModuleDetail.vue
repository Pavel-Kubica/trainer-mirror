<script setup>
import {onMounted, inject, provide, ref, watch} from 'vue';
import {useDisplay, useLocale} from 'vuetify';
import {moduleLocked, TAB_ASSIGNMENT, TAB_MODULE_RATING} from '@/plugins/constants';
import {useUserStore} from '@/plugins/store';
import * as Nav from '@/service/nav';
import {lessonApi, lessonUserApi, studentModuleApi} from '@/service/api';

import ModuleRequestDialog from '@/components/lesson/ModuleRequestDialog.vue';
import LoadingScreen from '@/components/custom/LoadingScreen.vue';
import ModuleDetail from '@/components/lesson/ModuleDetail.vue';
import ModuleTitle from '@/components/lesson/ModuleTitleCard.vue';
import LessonUserListNavigation from '@/components/lesson/LessonUserListNavigation.vue';
import TooltipIconButton from '@/components/custom/TooltipIconButton.vue';
import ModuleRating from "@/components/lesson/ModuleRating.vue";
import ModuleRatingDialog from "@/components/lesson/ModuleRatingDialog.vue";
import codeApi from "@/service/codeApi";
import ScoringRuleUserDetail from "@/components/lesson/scoringRules/ScoringRuleUserDetail.vue";

const props = defineProps(['lessonId', 'moduleId', 'user', 'scoringRuleId']);
const appState = inject('appState');
const userStore = useUserStore();
const reloadHook = inject('reloadHook');
const lessonUsers = inject('lessonUsers');
const {t} = useLocale();
const {mobile} = useDisplay();

const lesson = ref(null);
const module = ref(null);
const scoringRule = ref(null);
const lessonUser = ref(null);
const error = ref(null);
const isTeacher = ref(false);
const displayRatingDialog = ref(false);
const studentModule = ref(null);

provide('displayRatingDialog', displayRatingDialog);
provide('studentModule', studentModule);
provide('lesson', lesson);
provide('module', module);
provide('scoringRule', scoringRule)
provide('lessonUser', lessonUser);
provide('appState', appState);

const tabList = ref({});
const selectedTab = ref(null);
provide('tabList', tabList);
provide('selectedTab', selectedTab);

const requestDialog = ref(false);
provide('requestDialog', requestDialog);

const requestAnswerCallbacks = ref([])
provide('requestAnswerCallbacks', requestAnswerCallbacks)

const reload = async (setNavigation) => {
  reloadHook.value = new Date().getTime();
  lessonUser.value = null;
  lesson.value = null;
  module.value = null;
  scoringRule.value = null;
  error.value = null;
  tabList.value = {};

  if (props.user === "-1") return;
  if (props.user) {
    await lessonUserApi.lessonUserDetail(props.lessonId, props.user)
        .then(async (result) => {
          module.value = result.lesson.modules.find((mod) => mod.id === parseInt(props.moduleId));
          if (module.value) {
            studentModule.value = module.value.students.find((st) => st.id === parseInt(userStore.user.id));
          }
          scoringRule.value = result.lesson.scoringRules.find((sr) => sr.id === parseInt(props.scoringRuleId));
          if (scoringRule.value)
          lessonUser.value = result.user;
          lesson.value = result.lesson;
          isTeacher.value = false;
          if (module.value !== undefined) {
            await studentModuleApi.getStudentModuleByModuleUserLesson(props.moduleId, props.lessonId, userStore.user.id)
                .then(async (result) => {
                  studentModule.value = result;
                })
                .catch((err) => {
                  error.value = err.code;
                });
          }
          if (setNavigation) {
            appState.value.navigation = [
              new Nav.CourseList(), new Nav.CourseDetail(result.lesson.week.course),
              new Nav.CourseUserList(result.lesson.week.course),
              new Nav.CourseUserDetail(result.lesson.week.course, result.user),
              new Nav.LessonUserDetail(result.lesson, result.user, true),
              new Nav.ScoringRuleList(result.lesson.id, result.user.id),
            ].concat(module.value ? [new Nav.ModuleUserDetail(result.lesson, module.value, result.user)] : [])
                .concat(scoringRule.value ? [new Nav.ScoringRuleUserDetail(result.lesson, scoringRule.value, result.user)] : []);
          }
        })
        .catch((err) => {
          error.value = err.code;
        });
  } else {
    await lessonApi.lessonDetail(props.lessonId)
        .then(async (result) => {
          module.value = result.modules.find((mod) => mod.id === parseInt(props.moduleId));
          if (module.value) {
            studentModule.value = module.value.students.find((st) => st.student.id === parseInt(userStore.user.id));
          }
          lesson.value = result;
          scoringRule.value = lesson.value.scoringRules.find((sr) => sr.id === parseInt(props.scoringRuleId));
          isTeacher.value = (result.week.course.role === 'TEACHER');
          if (module.value.type==='CODE')
          {
            codeApi.codeDetail(module.value.id).then(
                (codeModule) => {
                  if (codeModule.codeType==='SHOWCASE')
                    tabList.value[TAB_ASSIGNMENT] = t('$vuetify.tab_showcase');
                }
            ).catch((err) => { error.value = err.code })
          }
          tabList.value[TAB_ASSIGNMENT] = t('$vuetify.tab_assignment');
          if (isTeacher.value === true && module.value && (module.value.type === 'TEXT' || module.value.type === 'ASSIGNMENT')) {
            tabList.value[TAB_MODULE_RATING] = t('$vuetify.tab_rating');
          }
          if (module.value !== undefined) {
            await studentModuleApi.getStudentModuleByModuleAndUser(props.moduleId, props.lessonId)
                .then(async (result) => {
                  studentModule.value = result;
                })
                .catch((err) => {
                  error.value = err.code;
                });
          }
          if (setNavigation) {
            appState.value.navigation = [
              new Nav.CourseList(), new Nav.CourseDetail(result.week.course), new Nav.LessonDetail(result),
              new Nav.ScoringRuleList(result.id, userStore.user.id),
            ].concat(module.value ? [new Nav.ModuleDetail(result, module.value)] : [])
                .concat(scoringRule.value ? [new Nav.ScoringRuleDetail(result, scoringRule.value)] : []);
          }
        })
        .catch((err) => {
          error.value = err.code;
        });
  }
};

const requestToggle = () => {
  displayRatingDialog.value = true;
};

const canEdit = (module) => {
  if (!module) return;
  return module.author === userStore.user.username || module.editors.includes(userStore.user.username);
};

onMounted(async () => {
  await reload(true);
  if (!module.value && Object.keys(tabList.value).length > 0) {
    selectedTab.value = Object.keys(tabList.value)[0];
  }
});

watch(props, async () => {
  await reload(true);
  if (!module.value && Object.keys(tabList.value).length > 0) {
    selectedTab.value = Object.keys(tabList.value)[0];
  }
});


watch(module, () => {
  if (!module.value && Object.keys(tabList.value).length > 0) {
    selectedTab.value = Object.keys(tabList.value)[0];
  }
});
</script>

<template>
  <ModuleRequestDialog />
  <ModuleRatingDialog v-if="module!==undefined" :module-id="moduleId" :lesson-id="lessonId" :callback="reload" />
  <v-main class="d-flex flex-column justify-space-between ma-2">
    <LoadingScreen :items="lesson" :error="error">
      <template #items>
        <div v-if="!scoringRuleId">
          <div class="d-flex justify-space-between align-center">
            <v-tabs v-if="Object.keys(tabList).length > 1" v-model="selectedTab" density="compact">
              <v-tab v-for="id in Object.keys(tabList)" :key="id" :value="id">
                {{ tabList[id] }}
              </v-tab>
            </v-tabs>
            <div v-if="!lessonUser && (userStore.isTeacher(lesson.week.course) || userStore.user.isAdmin
                   || (lesson.week.course.subject && userStore.user.isGuarantor.includes(lesson.week.course.subject.id)))"
                 :style="Object.keys(tabList).length > 1 ? ''
                   : `position: absolute; z-index: 1; top: 92px; right: ${(mobile || module?.type === 'TEXT') ? 16 : 416}px`">
              <TooltipIconButton v-if="canEdit(module) && !moduleLocked(lesson, module)" icon="mdi-pencil"
                                 tooltip="$vuetify.code_module.edit_module" class="me-2" size="36"
                                 :to="new Nav.ModuleEdit(module).routerPath()" />
              <TooltipIconButton v-if="module && !moduleLocked(lesson, module)" icon="mdi-eye"
                                 tooltip="$vuetify.code_module.module_solutions" class="me-2"
                                 size="36"
                                 :to="new Nav.LessonModuleUser(lesson, module, {id: -1}).routerPath()" />
            </div>
          </div>
          <v-window v-model="selectedTab" class="flex-grow-1">
            <v-window-item v-if="!lessonUser && !moduleLocked(lesson, module)" :key="TAB_ASSIGNMENT"
                           :value="TAB_ASSIGNMENT"
                           :transition="false" :reverse-transition="false">
              <ModuleTitle :request-toggle="requestToggle" :is-teacher="isTeacher" />
            </v-window-item>
            <div v-if="userStore.isTeacher(lesson.week.course)">
              <v-window-item
                v-if="userStore.isTeacher(lesson.week.course) && !lessonUser && !moduleLocked(lesson, module)"
                :key="TAB_MODULE_RATING" :value="TAB_MODULE_RATING"
                :transition="false" :reverse-transition="false">
                <ModuleRating />
              </v-window-item>
            </div>
            <v-window-item v-if="lessonUser && (!module || module.type === 'TEXT')" class="pa-4">
              <em>{{ t('$vuetify.tab_empty_text') }}</em>
            </v-window-item>
            <v-window-item v-if="userStore.anonymous && module && !module.allowedShow && lessonUser" class="pa-4">
              <em>{{ t('$vuetify.tab_not_allowed_share') }}</em>
            </v-window-item>
            <ModuleDetail v-else-if="module" :user="user" :reload="reload" />
            <LessonUserListNavigation v-if="lessonUsers" :user="user" />
          </v-window>
        </div>
        <div v-if="scoringRuleId">
          <ScoringRuleUserDetail :scoring-rule-id="scoringRuleId" :lesson-id="lessonId" />
        </div>
      </template>
    </LoadingScreen>
  </v-main>
</template>

<style scoped>
.v-tab {
  text-transform: none !important;
  font-size: 1em;
}
</style>
