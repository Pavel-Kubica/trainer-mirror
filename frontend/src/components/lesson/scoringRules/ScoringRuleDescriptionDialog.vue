<script setup>
import {inject, onMounted, ref, watch} from 'vue'
import {useLocale} from "vuetify";
import {scoringRuleApi} from "@/service/api";
import LoadingScreen from "@/components/custom/LoadingScreen.vue";

const descriptionDialog = inject('descriptionDialog')
const scoringRuleData = ref(null)
const props = defineProps(['scoringRule', 'callback'])
const {t} = useLocale()
const appState = inject('appState')
const loading = ref(false)
const error = ref(null)

const submitAllowed = () => {
  return scoringRuleData.value && scoringRuleData.value.name &&
      scoringRuleData.value.shortName && scoringRuleData.value.points &&
      scoringRuleData.value.until && !(props.scoringRule
          && (scoringRuleData.value.toComplete > props.scoringRule.modules.length || scoringRuleData.value.toComplete < 0))
}


const submit = () => {
  if (!submitAllowed())
    return false
  loading.value = true
  const promise =  scoringRuleApi.editScoringRule(props.scoringRule.id, scoringRuleData.value)
  promise
      .then(async () => {
        appState.value.notifications.push({
          type: "success", title: t(`$vuetify.rule_edit_notification_title`),
          text: t(`$vuetify.rule_edit_notification_text`),
        })
        props.callback()
        loading.value = false
        descriptionDialog.value = false

      })
      .catch((err) => {
        error.value = err.code
      })
}

const loaded = async () => {
  if (props.scoringRule) {
    scoringRuleData.value = Object.assign({}, props.scoringRule)
  }
  loading.value = false
}

watch(props, loaded)
onMounted(loaded)

</script>


<template>
  <v-dialog v-model="descriptionDialog" width="auto">
    <v-card :title="t(`$vuetify.lesson_edit_rules_edit_description`)" class="mx-auto px-6 py-8" width="380px">
      <LoadingScreen :items="!loading" :error="error">
        <template #content>
          <v-form @submit.prevent="submit">
            <!-- <v-text-field v-model="scoringRuleData.description" :label="t(`$vuetify.rule_create_edit_description`)" autofocus /> -->
            <v-textarea v-model="scoringRuleData.description"
                        :label="t(`$vuetify.rule_create_edit_description`)"
                        rows="5"
                        counter="1000"
                        auto-grow
                        clearable
            />
          </v-form>
        </template>
      </LoadingScreen>
      <v-card-actions class="d-flex justify-center">
        <v-btn class="mr-15" variant="tonal" color="deep-purple-darken-1" @click="descriptionDialog = false">
          {{ t('$vuetify.dialog_close') }}
        </v-btn>
        <v-btn class="ml-15" variant="tonal" color="info" type="submit" :disabled="!submitAllowed()" @click="submit">
          {{ t(`$vuetify.action_edit`) }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>
