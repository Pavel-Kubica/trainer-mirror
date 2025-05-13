<script setup>
import {ref, inject, onMounted, provide} from 'vue'
import { useLocale } from 'vuetify'
import {lessonUserApi, scoringRuleApi} from '@/service/api'
import * as Nav from '@/service/nav'

import LoadingScreen from '@/components/custom/LoadingScreen.vue'
import ScoringRuleDetailUserListRow from "@/components/lesson/scoringRules/userList/ScoringRuleDetailUserListRow.vue";
import ScoringRuleDetailUserListHeader from "@/components/lesson/scoringRules/userList/ScoringRuleDetailUserListHeader.vue";

const appState = inject('appState')
const props = defineProps(['lessonId','scoringRuleId'])
const { t } = useLocale()

const lesson = ref(null)
const scoringRule = ref(null)
const modulesScoringRule = ref([])
const error = ref('')
provide('lesson', lesson)
provide('scoringRule', scoringRule)

onMounted(async() => {
  scoringRuleApi.scoringRuleDetail(props.scoringRuleId)
      .then((result) => {
        scoringRule.value = result
        modulesScoringRule.value = result.modules
      })
      .catch((err) => { error.value = err.code })
  lessonUserApi.lessonUserList(props.lessonId)
      .then(async (result) => {
        appState.value.navigation = [new Nav.CourseList(), new Nav.CourseDetail(result.course), new Nav.LessonUserList(result, t)]
        result.modules = result.modules.filter((module) => {
          return module.type !== 'TEXT' && scoringRule.value.modules.some((ruleModule) => ruleModule.id === module.id);
        });
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
      })
      .catch((err) => { error.value = err.code })
})
</script>

<template>
  <v-card :title="t('$vuetify.lesson_user_list_title')">
    <LoadingScreen :items="lesson" :error="error">
      <template #content>
        <v-table>
          <ScoringRuleDetailUserListHeader />
          <tbody>
            <ScoringRuleDetailUserListRow v-for="user in lesson.users" :key="user.id" :user="user" />
          </tbody>
        </v-table>
      </template>
    </LoadingScreen>
  </v-card>
</template>
