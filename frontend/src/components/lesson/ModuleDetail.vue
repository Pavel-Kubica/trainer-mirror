<script setup>
import { inject } from 'vue'
import { useLocale } from 'vuetify'
import { moduleLocked } from '@/plugins/constants'
import CodeModule from '@/components/modules/CodeModule.vue'
import QuizModule from '@/components/modules/QuizModule.vue'
import AssignmentModule from '@/components/modules/AssignmentModule.vue'
import ModuleUnlockCard from '@/components/lesson/ModuleUnlockCard.vue'
import { useUserStore } from '@/plugins/store'
import SelftestModule from "@/components/modules/SelftestModule.vue";

const userStore = useUserStore()
const lessonUser = inject('lessonUser')
const lesson = inject('lesson')
const module = inject('module')
defineProps(['user', 'reload'])
const { t } = useLocale()
</script>

<template>
  <v-card v-if="lessonUser && userStore.anonymous && !module.allowedShow" class="mb-4 pa-2 overflow-y-auto">
    <em>{{ t('$vuetify.tab_not_allowed_share') }}</em>
  </v-card>
  <ModuleUnlockCard v-else-if="moduleLocked(lesson, module)" :teacher="user" :reload="reload" />
  <CodeModule v-else-if="module.type === 'CODE'" :teacher="user" />
  <QuizModule v-else-if="module.type === 'QUIZ'" :module="module" :lesson="lesson.id" :teacher="user" :course="lesson.week.course" />
  <AssignmentModule v-else-if="module.type === 'ASSIGNMENT'" :teacher="user" />
  <SelftestModule v-else-if="module.type === 'SELFTEST'" :module="module" :lesson="lesson.id" :teacher="user" />
  <!-- TextModule has no content -->
</template>
