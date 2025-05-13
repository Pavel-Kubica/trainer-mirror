<script setup>
import {ref, onMounted, provide} from 'vue'
import { useLocale } from 'vuetify'
import {lessonApi, lessonUserApi, scoringRuleApi as scoringRulesApi} from '@/service/api'

import LoadingScreen from '@/components/custom/LoadingScreen.vue'
import ScoringRuleUserListHeader from "@/components/lesson/scoringRules/userList/ScoringRuleUserListHeader.vue";
import ScoringRuleUserListRow from "@/components/lesson/scoringRules/userList/ScoringRuleUserListRow.vue";

const props = defineProps(['lessonId'])
const { t } = useLocale()

const lesson = ref(null)
const error = ref('')
const scoringRulesMap = ref([])
const allPromises = []
provide('lesson', lesson)
provide ('scoringRulesMap',scoringRulesMap)

onMounted(async() => {
  lessonApi.lessonDetail(props.lessonId)
      .then((result) => {
        lesson.value = result
        lessonUserApi.lessonUserList(props.lessonId)
            .then(async (result) => {
              lesson.value.users = result.users
                  .sort((u1, u2) => u1.username > u2.username)
              for (const user of lesson.value.users) {
                for (const sr of lesson.value.scoringRules) {
                  const promise = scoringRulesApi.getScoringRuleUser(sr.id,
                  user.id).then((sr) => {
                    scoringRulesMap.value.push(sr)
                  });
                allPromises.push(promise);
                }
              }
              await Promise.all(allPromises)
            })
            .catch((err) => { error.value = err.code })
      }) .catch((err) => { error.value = err.code })



})
</script>

<template>
  <v-card :title="t('$vuetify.lesson_user_list_title')">
    <LoadingScreen :items="lesson" :error="error">
      <template #content>
        <v-table>
          <ScoringRuleUserListHeader />
          <tbody>
            <ScoringRuleUserListRow v-for="user in lesson.users" :key="user.id" :user="user" />
          </tbody>
        </v-table>
      </template>
    </LoadingScreen>
  </v-card>
</template>
