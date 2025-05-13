<script setup>
import ScoringRuleDetailUserList from "@/components/lesson/scoringRules/userList/ScoringRuleDetailUserList.vue";
import {inject} from "vue";
const appState = inject('appState')
appState.value.leftDrawer = undefined
</script>

<template>
  <v-main>
    <ScoringRuleDetailUserList :lesson-id="$route.params.lesson" :scoring-rule-id="$route.params.scoringRule" />
  </v-main>
</template>
