<script setup>
import { inject } from 'vue'
import { useLocale } from 'vuetify'
import { useUserStore } from '@/plugins/store'
import * as Nav from '@/service/nav'
import {unsatisfiedModuleRequest} from "@/plugins/constants";

const lesson = inject('lesson')
const lessonSr = inject('lessonSr')
const userStore = useUserStore()
const { t } = useLocale()

const userFilterModule = (module, user) => {
  const solution = user.modules[module.id]
  if (!solution)
    return false
  return (!userStore.solvedFilter || solution.progress >= module.minPercent) &&
      (!userStore.requestedHelpFilter || unsatisfiedModuleRequest(module)) &&
      (!userStore.allowedShowFilter || solution.allowedShow)
}
</script>

<template>
  <thead>
    <tr>
      <th>{{ t('$vuetify.lesson_user_list_header_user') }}</th>
      <th v-for="sr in lessonSr.scoringRules" :key="sr.id">
        <span>{{ sr.shortName }}</span>
      </th>
      <th v-for="moduleUsersPair in lesson.modules.map((mod) => ({
        module: mod,
        users: lesson.users.filter((u) => userFilterModule(mod, u))
          .sort((u1, u2) => new Date(u1.modules[mod.id].completed) - new Date(u2.modules[mod.id].completed))
      }))" :key="moduleUsersPair.module.id">
        <template v-if="moduleUsersPair.users.length">
          <router-link class="text-decoration-none"
                       :to="new Nav.LessonSolutionsModule(lesson, moduleUsersPair.module, {id: -1}).routerPath()">
            {{ moduleUsersPair.module.name }}
            <v-tooltip activator="parent" location="top">
              {{ t('$vuetify.lesson_user_list_header_solution') }}
            </v-tooltip>
          </router-link>
        </template>
        <span v-else>{{ moduleUsersPair.module.name }}</span>
      </th>

      <th>{{ t('$vuetify.lesson_user_list_header_detail') }}</th>
    </tr>
  </thead>
</template>
