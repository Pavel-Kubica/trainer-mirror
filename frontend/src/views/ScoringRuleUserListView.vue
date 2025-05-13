<script setup>
import ScoringRuleUserList from "@/components/lesson/scoringRules/userList/ScoringRuleUserList.vue";
import {inject} from "vue";
const appState = inject('appState')
appState.value.leftDrawer = undefined
</script>

<template>
  <v-main>
    <ScoringRuleUserList :lesson-id="$route.params.lesson" :scoring-rule-id="$route.params.scoringRule" />
  </v-main>
</template>
