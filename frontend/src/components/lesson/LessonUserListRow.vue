<script setup>
import {inject, onMounted, ref, watch} from 'vue'
import { useLocale } from 'vuetify'
import {anonEmoji, unsatisfiedModuleRequest} from '@/plugins/constants'
import { useUserStore } from '@/plugins/store'
import * as Nav from '@/service/nav'
import TooltipEmojiSpan from '@/components/custom/TooltipEmojiSpan.vue'
import TooltipIconButton from '@/components/custom/TooltipIconButton.vue'
import {scoringRuleApi as scoringRulesApi} from "@/service/api";

const props = defineProps(['user'])
const lesson = inject('lesson')
const lessonSr = inject('lessonSr')
//const scoringRulesMap = inject('scoringRulesMap')
const scoringRules = inject('scoringRules',ref([]))
const scoringRulesUser = ref([])
const allPromises = []
const allScoringRulesPoints = ref(0)
const userScoringRulesPoints = ref(0)
const userStore = useUserStore()
const { t } = useLocale()

watch(scoringRules, (newVal) => {
  scoringRules.value = newVal
});

onMounted(async() => {

  for (const sr of lessonSr.value.scoringRules) {
    const promise = scoringRulesApi.getScoringRuleUser(sr.id,
        props.user.id).then((sr) => {
      scoringRulesUser.value.push(sr)
    });
    allPromises.push(promise);
  }
  await Promise.all(allPromises)

  let completed = lessonSr.value.scoringRules.filter((sr) =>
      scoringRulesUser.value.filter(sru => sru.rule.id === sr.id && sru.completed).length)

  allScoringRulesPoints.value = lessonSr.value.scoringRules.reduce((total, item) => total + item.points,0)
  userScoringRulesPoints.value = completed.reduce((total, item) => total + item.points,0)


})

const userFilter = (user) => (!userStore.solvedFilter || user.completed) &&
    (!userStore.requestedHelpFilter || user.needsHelp || user.submitted) &&
    (!userStore.allowedShowFilter || user.allowedShow)

const mapRequestEmoji = (sr) => {
  if (!sr) return ''
  return sr.requestType === 'HELP' ? '😿' : (sr.requestType === 'EVALUATE' ? '⬆️' : '')
}
const mapProgressEmoji = (smod) => (smod.sm?.progress >= smod.module.minPercent ? '✅' : (smod.sm?.progress > 0 ? '♻️' : (smod.sm ? '🚫' : '⭕️') ))
</script>

<template>
  <tr v-show="userFilter(user)">
    <td>
      {{ userStore.anonymous ? anonEmoji(user.username) : user.name }}
      <TooltipEmojiSpan v-if="user.needsHelp" emoji="😿" />
      <TooltipEmojiSpan v-if="user.submitted" emoji="⬆️" />
    </td>
    <td v-for="sr in lessonSr.scoringRules" :key="sr.id">
      <div :style="{
        display: 'inline-block',
        backgroundColor: scoringRulesUser.filter(scoringRule => scoringRule.rule.id === sr.id)[0]?.studentModules.filter(sm => sm.completedOn).length>=(sr.toComplete ?? 0) ? '#e4e4e4' : 'rgba(253,232,230,0)',
        borderRadius: '15px',
        padding: '5px 10px',
        textAlign: 'center',
        color: 'black',
        margin: '5px',
      }">
        {{ scoringRulesUser.filter(scoringRule => scoringRule.rule.id === sr.id)[0]?.studentModules.filter(sm => sm.completedOn).length }}/{{ sr.toComplete ?? 0 }}
      </div>
    </td>
    <td v-for="moduleSmPair in lesson.modules.map((mod) => ({ module: mod, sm: user.modules[mod.id] }))"
        :key="moduleSmPair.module.id"
    >
      <TooltipEmojiSpan :emoji="mapProgressEmoji(moduleSmPair)" />
      <TooltipEmojiSpan v-if="moduleSmPair.sm?.completedEarly" emoji="⚡️" />
      <TooltipEmojiSpan v-if="moduleSmPair.module.manualEval && moduleSmPair.sm?.completed" emoji="🏆"
                        :parameter="moduleSmPair.sm ? new Date(moduleSmPair.sm?.completed).toLocaleString() : ''" />
      <TooltipEmojiSpan :emoji="mapRequestEmoji(moduleSmPair.sm?.studentRequest)" :if="unsatisfiedModuleRequest(moduleSmPair.sm)" />
      <TooltipEmojiSpan v-if="moduleSmPair.sm?.allowedShow" emoji="👁" />
    </td>

    <td>
      <TooltipIconButton icon="mdi-eye" color="rgb(var(--v-theme-anchor))"
                         :tooltip="t('$vuetify.lesson_user_list_row_detail')"
                         :to="new Nav.LessonUserDetail(lesson, user, false).routerPath()" />
    </td>
  </tr>
</template>
