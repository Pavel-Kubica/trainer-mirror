<script setup>
import {ref, inject, onMounted, provide, watch} from 'vue'
import { useLocale } from 'vuetify'
import {lessonApi, lessonUserApi, scoringRuleApi as scoringRulesApi} from '@/service/api'
import * as Nav from '@/service/nav'

import LoadingScreen from '@/components/custom/LoadingScreen.vue'
import LessonUserFilterPanel from '@/components/lesson/LessonUserFilterPanel.vue'
import LessonUserListHeader from '@/components/lesson/LessonUserListHeader.vue'
import LessonUserListRow from '@/components/lesson/LessonUserListRow.vue'

const appState = inject('appState')
const props = defineProps(['lessonId'])
const { t } = useLocale()

const lesson = ref(null)
const lessonSr = ref(null)
const error = ref('')
const scoringRulesMap = ref([])
const scoringRules = ref([])
const allPromises = []
provide('lesson', lesson)
provide('lessonSr', lessonSr)
provide ('scoringRulesMap',scoringRulesMap)
provide('scoringRules',scoringRules)

onMounted(async() => {
  lessonApi.lessonDetail(props.lessonId)
      .then(async (result2) => {
        lessonSr.value = result2
  lessonUserApi.lessonUserList(props.lessonId)
      .then(async (result) => {
        appState.value.navigation = [new Nav.CourseList(), new Nav.CourseDetail(result.course), new Nav.LessonUserList(result, t)]
        result.modules = result.modules.filter((module) => module.type !== 'TEXT')
        const minPercent = result.modules.reduce((obj, module) => {
          obj[module.id] = module.minPercent
          return obj
        }, {})
        result.users = result.users
            .sort((u1, u2) => u1.username > u2.username)
            .map((u) => { return {
              name: u.name, id: u.id, username: u.username,
              needsHelp: u.modules.some((sm) => !sm.studentRequest?.satisfied && sm.studentRequest?.requestType === 'HELP'),
              submitted: u.modules.some((sm) => !sm.studentRequest?.satisfied && sm.studentRequest?.requestType === 'EVALUATE'),
              completed: u.modules.some((sm) => sm.progress >= minPercent[sm.id]),
              allowedShow: u.modules.some((sm) => sm.allowedShow),
              modules: u.modules.reduce((resultObject, sm) => {
                resultObject[sm.id] = sm
                return resultObject
              }, {})}})
        lesson.value = result

              for (const user of lesson.value.users) {
                for (const sr of lessonSr.value.scoringRules) {
                  const promise = scoringRulesApi.getScoringRuleUser(sr.id,
                      user.id).then((sr) => {
                    scoringRulesMap.value.push(sr)
                  });
                  allPromises.push(promise);
                }
                await Promise.all(allPromises)
              }
            }) .catch((err) => { error.value = err.code })
      })
      .catch((err) => { error.value = err.code })
})

watch(props, async () => {
  await onMounted()
})

</script>

<template>
  <v-card :title="t('$vuetify.lesson_user_list_title')">
    <template #append>
      <div class="d-flex justify-space-between" style="min-width: 250px;">
        <LessonUserFilterPanel />
      </div>
    </template>
    <LoadingScreen :items="lesson" :error="error">
      <template #content>
        <v-table>
          <LessonUserListHeader />
          <tbody>
            <LessonUserListRow v-for="user in lesson.users" :key="user.id" :user="user" />
          </tbody>
        </v-table>
      </template>
    </LoadingScreen>
  </v-card>
</template>
