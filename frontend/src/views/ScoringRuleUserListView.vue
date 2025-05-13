<script setup>
import ScoringRuleUserList from "@/components/lesson/scoringRules/userList/ScoringRuleUserList.vue";
</script>

<template>
  <v-main>
    <ScoringRuleUserList :lesson-id="$route.params.lesson" :scoring-rule-id="$route.params.scoringRule" />
  </v-main>
</template>
