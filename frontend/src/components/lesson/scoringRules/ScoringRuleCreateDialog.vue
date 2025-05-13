<script setup>
import {computed, inject, ref} from 'vue'
import {useLocale} from "vuetify";
import {getErrorMessage} from "@/plugins/constants";
import {scoringRuleApi} from "@/service/api";

import LocaleAwareDatepicker from "@/components/custom/LocaleAwareDatepicker.vue";

const scoringRuleData = ref(null)
const form = ref(null)
const isSubmitted = ref(false)

const createRuleDialog = inject('createRuleDialog')
const appState = inject('appState')
const props = defineProps(['lessonId', 'callback'])

const {t} = useLocale()


scoringRuleData.value = {
  lesson: props.lessonId,
  name: "",
  shortName: "",
  points: "",
  until: "",
  toComplete: "",
}


const dateRules = computed(() => {
  return [
    (v) => ((v || !isSubmitted.value) ? true : `${t('$vuetify.scoring_rule_date_required')}`),
  ];
});

const pointsRules = computed(() => {
  return [
    (points) => ((!isNaN(points) && points || !isSubmitted.value) ? true : `${t('$vuetify.scoring_rule_points_number')}`),
  ]
})

const nameRules = computed(() => {
  return [
    (v) => (!isSubmitted.value || v ? true : `${t('$vuetify.scoring_rule_name_required')}`),
  ];
});

const shortNameRules = computed(() => {
  return [
    (v) => ((v || !isSubmitted.value) ? true : `${t('$vuetify.scoring_rule_short_name_required')}`),
  ];
});

const createRule = async () => {
  isSubmitted.value = true
  if (!form.value.validate()) return
  if (!scoringRuleData.value.name || !scoringRuleData.value.shortName ||
      !scoringRuleData.value.points || !scoringRuleData.value.until)
    return
  try {
    await scoringRuleApi.createScoringRule(scoringRuleData.value)
    createRuleDialog.value = false
    appState.value.notifications.push({
      type: "success", title: t('$vuetify.rule_create_notification_title'),
      text: t(`$vuetify.rule_create_notification_text`)
    })
    props.callback()
  } catch (err) {
    appState.value.notifications.push({
      type: "error", title: t('$vuetify.rule_create_title'),
      text: getErrorMessage(t, err.code)
    })
  }
}


const translate = (key) => t(`$vuetify.rule_create_edit_${key}`)
</script>


<template>
  <v-dialog v-model="createRuleDialog">
    <v-form ref="form" @submit.prevent="createRule">
      <v-card :title="translate(`dialog_create`)" class="mx-auto px-6 py-8" width="380px">
        <v-card-text>
          <v-text-field v-model="scoringRuleData.name" :rules="nameRules" :label="translate('name')" autofocus />
          <v-text-field v-model="scoringRuleData.shortName" :rules="shortNameRules" :label="translate('short_name')" autofocus />
          <v-text-field v-model="scoringRuleData.points" :rules="pointsRules" :label="translate('points')" autofocus />
          <v-text-field v-if="props.scoringRule" v-model="scoringRuleData.toComplete" :label="translate('to_complete')" type="number" :suffix="`/ ${modulesLen}`" :min="0" :max="modulesLen" autofocus />
          <v-label class="mt-2">
            {{ translate('until') }}
          </v-label>
          <v-input v-model="scoringRuleData.until" :rules="dateRules" class="mt-2" hide-details="auto">
            <LocaleAwareDatepicker v-model="scoringRuleData.until" :rules="dateRules" :with-time="true" />
          </v-input>
        </v-card-text>
        <v-card-actions class="d-flex justify-center">
          <v-btn class="mr-15" variant="tonal" color="deep-purple-darken-1" @click="createRuleDialog=false; isSubmitted=false">
            {{ t('$vuetify.dialog_close') }}
          </v-btn>
          <v-btn class="ml-15" variant="tonal" color="blue" type="submit">
            {{ t('$vuetify.dialog_save') }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-form>
  </v-dialog>
</template>

