<script setup>
import {inject} from "vue";
import {anonEmoji, getErrorMessage, lessonUserFilter, smBorder, unsatisfiedModuleRequest} from "@/plugins/constants";
import * as Nav from "@/service/nav";
import LessonUserFilterPanel from "@/components/lesson/LessonUserFilterPanel.vue";
import TooltipEmojiSpan from "@/components/custom/TooltipEmojiSpan.vue";
import {useUserStore} from "@/plugins/store";
import {useLocale} from "vuetify";

const {t} = useLocale()

const userStore = useUserStore()

defineProps(['selectedUserId', 'lesson', 'module'])

const lessonUsers = inject('lessonUsers')

</script>

<template>
  <v-card>
    <v-list density="comfortable" style="padding: 0">
      <v-list-item>
        <div class="ps-2 d-flex justify-space-between">
          <LessonUserFilterPanel />
        </div>
      </v-list-item>
      <template v-for="lessonUser in lessonUsers.sort((lu1, lu2) => lu1.name.localeCompare(lu2.name))" :key="lessonUser.id">
        <v-list-item v-show="lessonUserFilter(userStore, lessonUser)" :active="+lessonUser.id === +selectedUserId"
                     :style="{'border-left': smBorder(lessonUser.module, module)}"
                     :to="new Nav.LessonSolutionsModule(lesson, module, lessonUser).routerPath()">
          <template #title>
            {{ userStore.anonymous ? anonEmoji(lessonUser.username) : lessonUser.name }}
            <TooltipEmojiSpan v-if="lessonUser.module.completed" emoji="🏆"
                              :parameter="new Date(lessonUser.module.completed).toLocaleString()" />
            <TooltipEmojiSpan v-if="lessonUser.module.completedEarly" emoji="⚡️" />
            <TooltipEmojiSpan v-if="unsatisfiedModuleRequest(lessonUser.module) && lessonUser.module.studentRequest?.requestType === 'HELP'" emoji="😿" />
            <TooltipEmojiSpan v-if="unsatisfiedModuleRequest(lessonUser.module) && lessonUser.module.studentRequest?.requestType === 'EVALUATE'" emoji="⬆️" />
            <TooltipEmojiSpan v-if="lessonUser.module.allowedShow" emoji="👁" />
          </template>
        </v-list-item>
      </template>
      <v-list-item v-if="lessonUsers && !lessonUsers.some((u) => lessonUserFilter(userStore, u))"
                   :title="t('$vuetify.lesson_user_list_empty')" />
      <v-list-item v-if="error" :title="getErrorMessage(t, error)" />
    </v-list>
  </v-card>
  <span style="display: inline-block; width: 100%; text-align: center; padding: 8px 24px; overflow-wrap: break-word">
    {{ t('$vuetify.lesson_solutions_note_visible_lesson') }}
  </span>
</template>

<style scoped>

</style>