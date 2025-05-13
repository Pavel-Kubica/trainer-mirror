<script setup>
import {onMounted, inject, provide, ref, watch, computed} from 'vue';
import {useLocale} from 'vuetify';
import {anonEmoji, moduleLocked, TAB_ASSIGNMENT, TAB_MODULE_RATING} from '@/plugins/constants';
import {useUserStore} from '@/plugins/store';
import * as Nav from '@/service/nav';
import {lessonApi, lessonUserApi, studentModuleApi} from '@/service/api';

import LoadingScreen from '@/components/custom/LoadingScreen.vue';
import ModuleDetail from '@/components/lesson/ModuleDetail.vue';
import ModuleTitle from '@/components/lesson/ModuleTitleCard.vue';
import ModuleRating from "@/components/lesson/ModuleRating.vue";
import ModuleRatingDialog from "@/components/lesson/ModuleRatingDialog.vue";
import codeApi from "@/service/codeApi";
import ScoringRuleUserDetail from "@/components/lesson/scoringRules/ScoringRuleUserDetail.vue";
import LessonUserListNavigation from "@/components/lesson/LessonUserListNavigation.vue";

const props = defineProps(['lessonId', 'moduleId', 'user', 'scoringRuleId']);
const appState = inject('appState');
const userStore = useUserStore();
const modulesReloadHook = inject('modulesReloadHook');
const userSidebar = inject('listUsers', undefined) // only relevant in solution view
const lessonUsers = inject('lessonUsers')
const {t} = useLocale();

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

const loadData = async () => {
  lessonUser.value = null;
  lesson.value = null;
  module.value = null;
  scoringRule.value = null;
  error.value = null;
  tabList.value = {};

  if (props.user === "-1") return;
  if (props.user) { // Solution view
    await lessonUserApi.lessonUserDetail(props.lessonId, props.user)
        .then(async (result) => {
          module.value = result.lesson.modules.find((mod) => mod.id === parseInt(props.moduleId));
          if (module.value) {
            module.value.codeCommentSourceRequest = module.value.studentRequest
            studentModule.value = module.value.students.find((st) => st.id === parseInt(userStore.user.id));
          }
          scoringRule.value = result.lesson.scoringRules.find((sr) => sr.id === parseInt(props.scoringRuleId));
          lessonUser.value = result.user;
          lesson.value = result.lesson;
          isTeacher.value = false;
          tabList.value[TAB_ASSIGNMENT] = t('$vuetify.tab_assignment');
          if (module.value !== undefined) {
            await studentModuleApi.getStudentModuleByModuleUserLesson(props.moduleId, props.lessonId, userStore.user.id)
                .then(async (result) => {
                  studentModule.value = result;
                })
                .catch((err) => {
                  error.value = err.code;
                });
          }
          appState.value.navigation = [
            new Nav.CourseList(), new Nav.CourseDetail(result.lesson.week.course),
            new Nav.LessonDetail(result.lesson, result.lesson.week)]
              .concat(module.value ? new Nav.LessonSolutionsModule(result.lesson, module.value, result.user) : new Nav.LessonUserDetail(lesson, result.user))
              .concat(scoringRule.value ? [new Nav.ScoringRuleUserDetail(result.lesson, scoringRule.value, result.user)] : []);
        })
        .catch((err) => {
          error.value = err.code;
          console.error(err)
        });
  } else { // Week/lesson detail
    await lessonApi.lessonDetail(props.lessonId)
        .then(async (result) => {
          module.value = result.modules.find((mod) => mod.id === parseInt(props.moduleId));
          if (module.value)
            module.value.codeCommentSourceRequest = module.value.studentRequest
          appState.value.navigation = [
            new Nav.CourseList(), new Nav.CourseDetail(result.week.course), new Nav.LessonDetail(result, result.week),
          ];
          if (module.value) {
            studentModule.value = module.value.students.find((st) => st.student.id === parseInt(userStore.user.id));
            if (module.value.type==='CODE')
            {
              codeApi.codeDetail(module.value.id).then(
                  (codeModule) => {
                    if (codeModule.codeType==='SHOWCASE')
                      tabList.value[TAB_ASSIGNMENT] = t('$vuetify.tab_showcase');
                  }
              ).catch((err) => { error.value = err.code })
            }
          }
          lesson.value = result;
          scoringRule.value = lesson.value.scoringRules.find((sr) => sr.id === parseInt(props.scoringRuleId));
          isTeacher.value = (result.week.course.role === 'TEACHER');

          tabList.value[TAB_ASSIGNMENT] = t('$vuetify.tab_assignment');
          if (isTeacher.value === true && module.value && (module.value.type === 'TEXT' || module.value.type === 'ASSIGNMENT')) {
            tabList.value[TAB_MODULE_RATING] = t('$vuetify.tab_rating');
          }
        })
        .catch((err) => {
          error.value = err.code;
        });
  }
};

const reload = async (setNavigation) => {
  modulesReloadHook.value = new Date().getTime();
  await loadData(setNavigation);
}

const requestToggle = () => {
  displayRatingDialog.value = true;
};

onMounted(async () => {
  await loadData(true);
  if (!module.value && Object.keys(tabList.value).length > 0) {
    selectedTab.value = Object.keys(tabList.value)[0];
  }
});

const heading = computed(() => {
  if (!props.user)
    return lesson.value.name;
  if (userSidebar?.value)
    return t('$vuetify.solutions_for_module') + ` ${module.value.name}`;
  return t('$vuetify.solutions_by_user') + " " + (userStore.anonymous ? anonEmoji(lessonUser.value.username) : `${lessonUser.value.username} (${lessonUser.value.name})`)
})

watch(props, async () => {
  await loadData(true);
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
  <ModuleRatingDialog v-if="module!==undefined" :module-id="moduleId" :lesson-id="lessonId" :callback="reload" />
  <v-main class="d-flex flex-column justify-space-between ma-2">
    <LoadingScreen :items="lesson" :error="error">
      <template #items>
        <div>
          <div class="d-flex justify-space-between">
            <v-card-title class="font-weight-bold pl-2">
              {{ heading }}
            </v-card-title>
            <v-btn v-if="isTeacher && !user" class="text-primary" style="align-self: center"
                   :to="new Nav.LessonProgressOverview(lesson, t).routerPath()">
              <template #prepend>
                <v-icon icon="mdi-table" />
              </template>
              {{ t('$vuetify.progress_overview') }}
            </v-btn>
            <div v-else-if="user" style="align-self: end">
              <v-btn class="text-primary mr-4" style="align-self: center"
                     :to="module ? new Nav.ModuleDetail(lesson, module).routerPath() : new Nav.LessonDetail(lesson, lesson.week).routerPath()">
                {{ t('$vuetify.lesson_solutions_to_lesson') }}
              </v-btn>
              <v-btn class="text-primary" style="align-self: center"
                     :to="new Nav.CourseUserList(lesson.week.course).routerPath()">
                {{ t('$vuetify.lesson_solutions_course_users') }}
              </v-btn>
            </div>
          </div>
          <div v-if="!scoringRuleId">
            <div class="d-flex justify-space-between align-center">
              <v-tabs v-if="Object.keys(tabList).length > 1" v-model="selectedTab" density="compact">
                <v-tab v-for="id in Object.keys(tabList)" :key="id" :value="id">
                  {{ tabList[id] }}
                </v-tab>
              </v-tabs>
            </div>
            <v-window v-model="selectedTab">
              <v-window-item v-if="!moduleLocked(lesson, module)" :key="TAB_ASSIGNMENT"
                             :value="TAB_ASSIGNMENT"
                             :transition="false" :reverse-transition="false">
                <ModuleTitle :request-toggle="requestToggle" :is-teacher="isTeacher" :solution-view="lessonUser" />
              </v-window-item>
              <div v-if="userStore.isTeacher(lesson.week.course)">
                <v-window-item
                  v-if="userStore.isTeacher(lesson.week.course) && !user && !moduleLocked(lesson, module)"
                  :key="TAB_MODULE_RATING" :value="TAB_MODULE_RATING"
                  :transition="false" :reverse-transition="false">
                  <ModuleRating />
                </v-window-item>
              </div>
              <v-window-item v-if="user && (!module || module.type === 'TEXT')" class="pa-4">
                <em>{{ t('$vuetify.tab_empty_text') }}</em>
              </v-window-item>
              <v-window-item v-if="userStore.anonymous && module && !module.allowedShow && user" class="pa-4">
                <em>{{ t('$vuetify.tab_not_allowed_share') }}</em>
              </v-window-item>
              <ModuleDetail v-else-if="module" :user="user" :reload="reload" />
              <LessonUserListNavigation v-if="userSidebar && module && lessonUsers" :user="user" />
            </v-window>
          </div>
          <div v-if="scoringRuleId">
            <ScoringRuleUserDetail :scoring-rule-id="scoringRuleId" :lesson-id="lessonId" />
          </div>
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
