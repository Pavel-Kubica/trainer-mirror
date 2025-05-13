<script setup>
import {computed, inject, onMounted, ref} from 'vue'
import { useLocale } from 'vuetify'
import {anonEmoji, unsatisfiedModuleRequest} from '@/plugins/constants'
import { useUserStore } from '@/plugins/store'
import {studentModuleApi} from "@/service/api";
import * as Nav from '@/service/nav'

import TooltipEmojiSpan from '@/components/custom/TooltipEmojiSpan.vue'
import TooltipIconButton from '@/components/custom/TooltipIconButton.vue'

const userStore = useUserStore()
const lesson = inject('lesson')
const scoringRule = inject('scoringRule')
const { t } = useLocale()
const props = defineProps(['user'])

const studentModulesMap = ref({})
const completedModules = ref(0)


onMounted(async() => {
  const modulePromises = scoringRule.value.modules.map(async (module) => {
    if (!studentModulesMap.value) studentModulesMap.value = {}
    studentModulesMap.value[module.id] = await studentModuleApi.getStudentModuleByModuleUserLesson(module.id, lesson.value.id, props.user.id)

  })
  await Promise.all(modulePromises)
  completedModules.value = Object.values(studentModulesMap.value)
      .filter(item => item.completedOn !== null)
      .length;
})

const modulesComplete = computed(() => {
  return completedModules.value.toString()
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
    <td>
      <div :style="{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        gap:'10px',
        backgroundColor: modulesComplete>=(scoringRule.toComplete ?? 0) ? '#e9f5ea' : '#fde8e6',
        borderRadius: '15px',
        padding: '10px',
        color: 'black',
        margin: '5px',
        width: '40px',
      }">
        <div>{{ modulesComplete }}/{{ scoringRule.toComplete ?? 0 }}</div>
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
