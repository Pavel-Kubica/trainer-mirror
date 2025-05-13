<script setup>
import { getIconByLessonType, getNameByLessonType, hasTouchScreen } from '@/plugins/constants'
import * as Nav from '@/service/nav'
import {inject, onMounted, ref} from 'vue'
import { useLocale } from 'vuetify'
import { useUserStore } from '@/plugins/store'
import {lessonUserApi, scoringRuleApi, studentModuleApi} from '@/service/api'
import router from '@/router'
import TooltipEmojiSpan from '@/components/custom/TooltipEmojiSpan.vue'
import TooltipIconButton from '@/components/custom/TooltipIconButton.vue'

const userStore = useUserStore()
const props = defineProps(['lesson'])
const scoringRules = ref(null)
const studentPoints = ref(0)
const rulesPoints = ref(0)
const error = ref(null)
const modulesScoringRule = ref([])
const studentModulesMap = ref({})
const { t } = useLocale()

const isTeacher = inject('isTeacher')
const courseUser = inject('courseUser')
const deleteDialog = inject('deleteDialog')
const deleteLesson = inject('deleteLessonEntity')
const copyDialog = inject('copyDialog')
const copyLesson = inject('copyLessonEntity')

const resetProgress = async () => {
  lessonUserApi.lessonUserDelete(props.lesson.id, courseUser.value.id)
      .then(() => router.go(0))
      .catch((error) => { console.log(error) })
}

onMounted(async() => {
  console.log("lesson - ", props.lesson)
  scoringRuleApi.lessonScoringRulesList(props.lesson.id)
      .then(async (result) => {
        console.log("scoringRules - ", result)
        scoringRules.value = result
        rulesPoints.value = result.reduce((acc, sr) => acc + sr.points, 0);
        console.log("rulesPoints.value - ", rulesPoints.value)

        let points = 0;
        for (const sr of result) {
          console.log("sr - ", sr)
          modulesScoringRule.value = sr.modules
          studentModulesMap.value = {};
          console.log("modulesScoringRule.value - ", modulesScoringRule.value);
          const modulePromises = modulesScoringRule.value.map(async (module) => {
            console.log("modulesPromises");
            studentModulesMap.value[module.id] = await studentModuleApi.getStudentModuleByModuleUserLesson(module.id, props.lesson.id, userStore.user.id);
          });

          await Promise.all(modulePromises);
          console.log("studentModulesMap- ", studentModulesMap.value);
          console.log("studentModulesMap.len - ", Object.keys(studentModulesMap.value).length);

          const completedModulesCount = Object.values(studentModulesMap.value)
              .filter(item => item.completedOn !== null).length;

          console.log("completedModules - ", completedModulesCount);

         /* if (completedModulesCount === Object.keys(studentModulesMap.value).length) {
            points += sr.points
          }*/
          if (completedModulesCount >= sr.toComplete)
            points += sr.points
        }
        studentPoints.value = points
      })
      .catch((err) => { error.value = err.code })
})

</script>

<template>
  <tr>
    <td>
      <span>
        <v-icon :icon="getIconByLessonType(lesson.type)" />
        <v-tooltip activator="parent" location="top">{{ t(getNameByLessonType(lesson.type)) }}</v-tooltip>
      </span>
    </td>
    <td>
      <router-link :to="new Nav.LessonDetail(lesson).routerPath()" style="text-decoration: none; color: inherit;">
        {{ lesson.name }}
      </router-link>
      <TooltipEmojiSpan v-if="lesson.timeLimit" emoji="⏱️" :parameter="new Date(lesson.timeLimit).toLocaleString()" />
      <TooltipEmojiSpan v-if="lesson.lockCode" emoji="🔑" />
      <TooltipEmojiSpan v-if="lesson.hidden" emoji="👻" />
    </td>
    <td style="text-align: left;">
      <div style="display: flex; justify-content: flex-start; align-items: center; gap: 16px;">
        <div style="display: inline-block; vertical-align: middle; width: 25vw;">
          <v-progress-linear
            v-if="!isTeacher"
            class="rounded"
            color="rgb(var(--v-theme-progress))"
            :model-value="lesson.progress"
            height="5"
          />
        </div>
        <div v-if="rulesPoints"
             :style="{
               display: 'flex',
               justifyContent: 'center',
               alignItems: 'center',
               backgroundColor: studentPoints === rulesPoints ? '#e9f5ea' : '#fde8e6',
               width: '120px',
               height: '30px',
               borderRadius: '10px',
               color: 'black',

             }">
          {{ studentPoints }}/{{ rulesPoints }}
        </div>
      </div>
    </td>
    <td><strong v-if="!isTeacher">{{ lesson.progress }} %</strong></td>
    <td class="d-flex flex-row justify-end">
      <template v-if="isTeacher">
        <TooltipIconButton v-if="hasTouchScreen()" icon="mdi-menu" color="rgb(var(--v-theme-anchor))" class="handle-lesson-row-btn"
                           :tooltip="t('$vuetify.course_detail_week_lesson_order_change')" />
        <TooltipIconButton icon="mdi-pencil" color="rgb(var(--v-theme-anchor))"
                           :to="new Nav.LessonEdit(lesson).routerPath()" />
        <TooltipIconButton icon="mdi-eye" color="rgb(var(--v-theme-anchor))"
                           :tooltip="t('$vuetify.course_detail_lesson_row_student_list')"
                           :to="new Nav.LessonUserList(lesson, t).routerPath()" />
        <TooltipIconButton icon="mdi-content-copy" color="rgb(var(--v-theme-anchor))"
                           @click="copyLesson = lesson; copyDialog = true" />
        <TooltipIconButton icon="mdi-delete" color="red"
                           @click="deleteLesson = lesson; deleteDialog = true" />
      </template>
      <template v-if="courseUser">
        <TooltipIconButton v-if="userStore.user.username === courseUser.username" icon="mdi-trash-can" color="red"
                           :tooltip="t('$vuetify.code_module.reset_progress')"
                           @click="resetProgress" />
        <TooltipIconButton icon="mdi-eye" color="rgb(var(--v-theme-anchor))"
                           :to="new Nav.LessonUserDetail(lesson, courseUser).routerPath()"
                           :tooltip="t('$vuetify.course_user_detail_link')" />
      </template>
      <v-btn v-else variant="text" icon="mdi-arrow-right" color="rgb(var(--v-theme-anchor))"
             :to="new Nav.LessonDetail(lesson).routerPath()" />
    </td>
  </tr>
</template>
