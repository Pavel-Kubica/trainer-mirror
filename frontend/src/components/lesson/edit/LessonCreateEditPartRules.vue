<script setup>
import {ref, provide, watch, onMounted} from 'vue'
import { useLocale } from 'vuetify'

import LoadingScreen from "@/components/custom/LoadingScreen.vue";
import ScoringRuleEditDialog from "@/components/lesson/scoringRules/ScoringRuleEditDialog.vue";
import ScoringRuleListDetailRow from "@/components/lesson/scoringRules/ScoringRuleListDetailRow.vue";
import {lessonApi} from "@/service/api";
import ScoringRuleCreateDialog from "@/components/lesson/scoringRules/ScoringRuleCreateDialog.vue";

const error = ref('')
const lessonData1 = ref(null)
const props = defineProps(['reload', 'error', 'edit','lessonId','user'])
const editScoringRule = ref(null)
const createEditDialog = ref(false)
const createRuleDialog = ref(false)
const editRuleDialog = ref(false)
const deleteScoringRulesDialog = ref(false)
provide('createEditDialog',createEditDialog)
provide('createRuleDialog',createRuleDialog)
provide('editRuleDialog',editRuleDialog)
provide('deleteScoringRulesDialog',deleteScoringRulesDialog)
provide('editScoringRuleEntity',editScoringRule)

const { t } = useLocale()


const loaded = async () => {
  if (props.lessonId) {
     lessonApi.lessonDetail(props.lessonId)
        .then((result) => {
          lessonData1.value = result
        })
        .catch((err) => {
          error.value = err.code
        })
  }
}

function fetchLesson() {
  lessonApi.lessonDetail(props.lessonId)
      .then((result) => {
        lessonData1.value = result
      })
      .catch((err) => { error.value = err.code })
}
provide('fetchLesson', fetchLesson)

const reload = async () => {
    lessonApi.lessonDetail(props.lessonId)
        .then((result) => {
          lessonData1.value = result
        })
        .catch((err) => { error.value = err.code })
}
provide('callback',reload)

watch(props, loaded)
onMounted(loaded)


</script>

<template>
  <ScoringRuleCreateDialog :lesson-id="props.lessonId" :callback="reload" />
  <ScoringRuleEditDialog :lesson-id="props.lessonId" :scoring-rule="editScoringRule" :callback="reload" />
  <div style="display: flex; justify-content: center;">
    <v-card-text>
      <LoadingScreen v-if="reload" :items="props.edit ? lessonData1 : lessonData1" :error="error">
        <template #items>
          <v-card width="100%" style="text-align: left;" :title="!edit ? t('$vuetify.lesson_edit_tab_rules') : ''">
            <v-table v-if="(props.edit ? lessonData1.scoringRules.length : lessonData1.scoringRules.length)">
              <ScoringRuleListDetailRow v-for="scoringRule in lessonData1.scoringRules" :key="scoringRule.id" :scoring-rule="scoringRule" :reload="reload" :edit="edit" :lesson-id="props.lessonId" :lesson="lessonData1" />
            </v-table>
            <span v-if="!(props.edit ? lessonData1.scoringRules.length : lessonData1.scoringRules.length)">
              <span style="padding-left: 10px;">{{ t('$vuetify.lesson_edit_rules_empty') }}</span>
            </span>
          </v-card>
          <v-card-actions class="mb-4" style="justify-content: space-between">
            <v-btn v-if="edit" color="green" size="large" variant="tonal" @click="() => { editScoringRule = null; createRuleDialog = true }">
              {{ t('$vuetify.lesson_edit_rules_create') }}
            </v-btn>
          </v-card-actions>
        </template>
      </LoadingScreen>
    </v-card-text>
  </div>
</template>