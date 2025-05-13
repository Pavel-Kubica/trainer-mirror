<script setup>
import {inject, onMounted, ref, watch} from 'vue'
import { useLocale } from 'vuetify'
import {lessonUserFilter, getErrorMessage, smBorder, anonEmoji, unsatisfiedModuleRequest} from '@/plugins/constants'
import { useUserStore } from '@/plugins/store'
import * as Nav from '@/service/nav'
import {lessonModuleApi} from '@/service/api'

import TooltipIconButton from '@/components/custom/TooltipIconButton.vue'
import TooltipEmojiSpan from '@/components/custom/TooltipEmojiSpan.vue'
import LessonUserFilterPanel from '@/components/lesson/LessonUserFilterPanel.vue'
import router from '@/router'

const userStore = useUserStore()
const props = defineProps(['lessonId', 'moduleId', 'user', 'scoringRuleId'])
const { t } = useLocale()

const lessonUsers = inject('lessonUsers')
const lesson = ref(null)
const module = ref(null)
const error = ref(null)

const reload = async () => {
  console.log("les user list left drawer reload")
  lessonModuleApi.lessonModuleUserList(props.lessonId, props.moduleId)
      .then((result) => {
        console.log("les user list left drawer - ", result)
        lesson.value = {id: props.lessonId, name: result.name}
        module.value = result.module
        lessonUsers.value = result.users
        const usersFiltered = result.users.filter((u) => lessonUserFilter(userStore, u))
        if (props.user === "-1" && usersFiltered.length)
          router.replace(new Nav.LessonModuleUser(lesson.value, module.value, usersFiltered[0]).routerPath())
      })
      .catch((err) => { error.value = err.code })
}

const reloadHook = inject('reloadHook')
watch(reloadHook, () => reload())
onMounted(async () => await reload())
</script>

<template>
  <v-card class="mx-auto">
    <v-list density="comfortable">
      <v-list-item :to="new Nav.LessonUserList({id: lessonId}, t).routerPath()">
        <template #prepend>
          <v-icon size="small" icon="mdi-arrow-left" />
        </template>
        <template #title>
          <strong>{{ t('$vuetify.lesson_user_list_left_drawer_users') }}</strong>
        </template>
        <template #append>
          <TooltipIconButton icon="mdi-eye" tooltip="$vuetify.lesson_user_list_left_drawer_module" class="ms-4" density="compact"
                             icon-size="18" :to="new Nav.ModuleDetail({id: lessonId}, {id: moduleId}).routerPath()" />
        </template>
      </v-list-item>
      <v-list-item>
        <div class="ps-2 d-flex justify-space-between">
          <LessonUserFilterPanel />
        </div>
      </v-list-item>
      <template v-for="lessonUser in lessonUsers" :key="lessonUser.id">
        <v-list-item v-show="lessonUserFilter(userStore, lessonUser)" :active="`${lessonUser.id}` === user"
                     :style="{'border-left': smBorder(lessonUser.module, module)}"
                     :to="new Nav.LessonModuleUser(lesson, module, lessonUser).routerPath()">
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
</template>
