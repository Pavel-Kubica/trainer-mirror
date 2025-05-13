<script setup>
import {inject, onMounted, ref} from 'vue'
import { useUserStore } from '@/plugins/store'
import {anonEmoji} from "@/plugins/constants";
import TooltipEmojiSpan from "@/components/custom/TooltipEmojiSpan.vue";
import {scoringRuleApi as scoringRulesApi} from "@/service/api";


const props = defineProps(['user'])
const lesson = inject('lesson')
const scoringRulesMap = inject('scoringRulesMap')
const scoringRulesUser = ref([])
const allPromises = []
const allScoringRulesPoints = ref(0)
const userScoringRulesPoints = ref(0)
const userStore = useUserStore()


onMounted(async() => {
  console.log("lesson - ", lesson.value)
  console.log("props.user - ", props.user.id)
  console.log("scoringRulesMap row - ", scoringRulesMap.value.filter((sr) => sr.id === 4))

  for (const sr of lesson.value.scoringRules) {
    const promise = scoringRulesApi.getScoringRuleUser(sr.id,
        props.user.id).then((sr) => {
      scoringRulesUser.value.push(sr)
    });
    allPromises.push(promise);
  }
  await Promise.all(allPromises)

  let completed = lesson.value.scoringRules.filter((sr) =>
      scoringRulesUser.value.filter(sru => sru.rule.id === sr.id && sru.completed).length)

  allScoringRulesPoints.value = lesson.value.scoringRules.reduce((total, item) => total + item.points,0)
  userScoringRulesPoints.value = completed.reduce((total, item) => total + item.points,0)


})


const userFilter = (user) => (!userStore.solvedFilter || user.completed) &&
    (!userStore.requestedHelpFilter || user.needsHelp || user.submitted) &&
    (!userStore.allowedShowFilter || user.allowedShow)

</script>

<template>
  <tr v-show="userFilter(user)">
    <td>
      {{ userStore.anonymous ? anonEmoji(user.username) : user.name }}
      <TooltipEmojiSpan v-if="user.needsHelp" emoji="😿" />
      <TooltipEmojiSpan v-if="user.submitted" emoji="⬆️" />
    </td>
    <td>
      <div :style="{
        display: 'inline-block',
        backgroundColor: userScoringRulesPoints>=allScoringRulesPoints ? '#e9f5ea' : '#fde8e6',
        borderRadius: '15px',
        padding: '5px 10px',
        textAlign: 'center',
        color: 'black',
        margin: '5px',
      }">
        <div>
          {{ userScoringRulesPoints }}/{{ allScoringRulesPoints }}
        </div>
      </div>
    </td>
    <td v-for="sr in lesson.scoringRules" :key="sr.id">
      <div :style="{
        display: 'inline-block',
        backgroundColor: scoringRulesUser.filter(scoringRule => scoringRule.rule.id === sr.id)[0]?.studentModules.filter(sm => sm.completedOn).length>=(sr.toComplete ?? 0) ? '#e9f5ea' : '#fde8e6',
        borderRadius: '15px',
        padding: '5px 10px',
        textAlign: 'center',
        color: 'black',
        margin: '5px',
      }">
        {{ scoringRulesUser.filter(scoringRule => scoringRule.rule.id === sr.id)[0]?.studentModules.filter(sm => sm.completedOn).length }}/{{ sr.toComplete ?? 0 }}
      </div>
    </td> 
  </tr>
</template>
