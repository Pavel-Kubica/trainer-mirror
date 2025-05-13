<script setup>
import { useLocale } from 'vuetify'
import {DIFFICULTY_VALUES, unsatisfiedModuleRequest} from '@/plugins/constants'
import TooltipEmojiSpan from "@/components/custom/TooltipEmojiSpan.vue";

defineProps(['lesson', 'module', 'showDifficulty'])
const { t } = useLocale()

const difficultyTitle = (diff) => {
  const ttl = DIFFICULTY_VALUES(t).find((val) => val.item === diff)?.title
  return ttl ? `(${ttl})` : ''
}
</script>

<template>
  <span v-if="module && module.locked">
    {{ t('$vuetify.module_title_locked') }} 🔑
  </span>
  <span v-else-if="module && module.id !== -1">
    {{ module.name }} {{ showDifficulty && difficultyTitle(module.difficulty ?? '') }}
    <TooltipEmojiSpan v-if="lesson?.timeLimit && module.timeLimit" emoji="⏱️"
                      :parameter="new Date(lesson?.timeLimit ?? 0).toLocaleString()" />
    <TooltipEmojiSpan v-if="module.completed" emoji="🏆" :parameter="new Date(module.completed).toLocaleString()" />
    <TooltipEmojiSpan v-if="module.completedEarly" emoji="⚡️" />
    <TooltipEmojiSpan v-if="unsatisfiedModuleRequest(module) && module.studentRequest?.requestType === 'HELP'" emoji="😿" />
    <TooltipEmojiSpan v-if="unsatisfiedModuleRequest(module) && module.studentRequest?.requestType === 'EVALUATE'" emoji="⬆️" />
    <TooltipEmojiSpan v-if="module.allowedShow" emoji="👁" />
    <TooltipEmojiSpan v-if="module.hidden" emoji="👻" />
  </span>
  <span v-else>{{ t('$vuetify.lesson_detail_intro_text') }}</span>
</template>
