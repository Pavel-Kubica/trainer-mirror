<script setup>
import {ref, provide, watch, onMounted} from 'vue'
import { useLocale } from 'vuetify'

import LoadingScreen from "@/components/custom/LoadingScreen.vue";
import {lessonApi, scoringRuleApi, studentModuleApi} from "@/service/api";
import ScoringRuleModulesListDetailRow from "@/components/lesson/scoringRules/ScoringRuleModulesListDetailRow.vue";
import {useUserStore} from "@/plugins/store";

const error = ref('')
const scoringRule = ref(null)
const lesson = ref(null)
const rulesPoints = ref(0)
const completedModules = ref(0)
const scoringRules = ref(null)
const modulesScoringRule = ref([])
const studentModulesMap = ref({})

const props = defineProps(['reload', 'error','lessonId','scoringRuleId'])
const editScoringRule = ref(null)
const createEditDialog = ref(false)
provide('createEditDialog',createEditDialog)
const deleteScoringRulesDialog = ref(false)
provide('deleteScoringRulesDialog',deleteScoringRulesDialog)
provide('editScoringRuleEntity',editScoringRule)
const { t } = useLocale()
const userStore = useUserStore()


const loaded = async () => {
  scoringRuleApi.scoringRuleDetail(props.scoringRuleId)
      .then((result) => {
        scoringRule.value = result
        modulesScoringRule.value = result.modules
      })
      .catch((err) => {
        error.value = err.code
      })
  lessonApi.lessonDetail(props.lessonId)
      .then((result) => {
        lesson.value = result
      })
      .catch((err) => {
        error.value = err.code
      })
  scoringRuleApi.lessonScoringRulesList(props.lessonId)
      .then(async (result) => {
        scoringRules.value = result
        rulesPoints.value = result.reduce((acc, sr) => acc + sr.points, 0);
        const modulePromises = modulesScoringRule.value.map(async (module) => {
          if (!studentModulesMap.value) studentModulesMap.value = {}
          studentModulesMap.value[module.id] = await studentModuleApi.getStudentModuleByModuleUserLesson(module.id, props.lessonId, userStore.user.id)

        })
        await Promise.all(modulePromises)
        completedModules.value = Object.values(studentModulesMap.value)
            .filter(item => item.completedOn !== null)
            .length;

      })
      .catch((err) => { error.value = err.code })

}

const reload = async () => {

}

watch(props, loaded)
onMounted(loaded)


</script>

<template>
  <v-card-text>
    <v-card width="100%">
      <v-card-title class="d-flex justify-space-between align-center text-h5" :style="{ fontWeight: '450' }">
        <span>{{ scoringRule?.name }}</span>
        <span style="text-align: right;">
          <span :style="{
            backgroundColor: scoringRule?.students.filter((module) => module.completedOn!==null).length
              >= (scoringRule?.toComplete ?? 0) ? '#e9f5ea' : '#fde8e6',
            borderRadius: '15px',
            padding: '10px',
            margin: '5px',
          }">
            {{ scoringRule?.students.filter((module) => module.completedOn!==null).length }}/{{ scoringRule?.toComplete || 0 }}
          </span>
          <span>{{ scoringRule?.points }} {{ t(`$vuetify.lesson_edit_rule_points`) }}</span>
        </span>
      </v-card-title>
      <LoadingScreen :items="scoringRule" :error="error">
        <template #table>
          <div>
            <div v-if="scoringRule?.description" class="mb-3">
              <span :style="{ whiteSpace: 'normal', fontSize: '18px' }">{{ scoringRule?.description }}</span>
            </div>
            <v-table v-if="scoringRule && scoringRule.students.length && lesson">
              <ScoringRuleModulesListDetailRow
                v-for="studentModule in scoringRule.students"
                :key="studentModule.id"
                :student-module="studentModule"
                :module="studentModule.module"
                :reload="reload"
                :lesson="studentModule.lesson"
              />
            </v-table>
          </div>
        </template>
      </LoadingScreen>
    </v-card>
  </v-card-text>
</template>