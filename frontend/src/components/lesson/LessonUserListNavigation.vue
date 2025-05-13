<script setup>
import { inject } from 'vue'
import { useLocale } from 'vuetify'
import { lessonUserFilter } from '@/plugins/constants'
import { useUserStore } from '@/plugins/store'
import * as Nav from '@/service/nav'

const userStore = useUserStore()
const props = defineProps(['user'])
const { t } = useLocale()

const lesson = inject('lesson')
const module = inject('module')
const lessonUsers = inject('lessonUsers')

const prevUser = () => {
  return lessonUsers.value.filter((u) => lessonUserFilter(userStore, u))[currentIndex() - 1]
}
const nextUser = () => {
  return lessonUsers.value.filter((u) => lessonUserFilter(userStore, u))[currentIndex() + 1]
}
const currentIndex = () => {
  return lessonUsers.value.filter((u) => lessonUserFilter(userStore, u)).findIndex((lu) => `${lu.id}` === props.user)
}
</script>

<template>
  <v-card flat>
    <div v-if="lessonUsers" class="ms-4 mb-4 d-flex justify-space-between align-center">
      <router-link v-if="currentIndex() > 0"
                   :to="new Nav.LessonSolutionsModule(lesson, module, prevUser()).routerPath()">
        <!-- We use <button> here as a workaround, for some reason if we use v-btn, it breaks layout upon navigating through the sidebar in solution view -->
        <button class="v-btn v-btn--elevated v-theme--customLight v-btn--density-compact v-btn--size-default v-btn--variant-elevated me-4" style="color: blue">
          {{ t('$vuetify.lesson_user_list_previous') }}
        </button>
      </router-link>
      <button v-else class="v-btn v-btn--disabled v-theme--customLight v-btn--density-compact v-btn--size-default v-btn--variant-elevated me-4" style="background-color: #00000020" disabled>
        {{ t('$vuetify.lesson_user_list_previous') }}
      </button>

      <router-link v-if="currentIndex() + 1 < lessonUsers.filter((u) => lessonUserFilter(userStore, u)).length"
                   :to="new Nav.LessonSolutionsModule(lesson, module, nextUser()).routerPath()">
        <button class="v-btn v-btn--elevated v-theme--customLight v-btn--density-compact v-btn--size-default v-btn--variant-elevated me-4" style="color: blue">
          {{ t('$vuetify.lesson_user_list_next') }}
        </button>
      </router-link>
      <button v-else class="v-btn v-btn--disabled v-theme--customLight v-btn--density-compact v-btn--size-default v-btn--variant-elevated me-4" style="background-color: #00000020" disabled>
        {{ t('$vuetify.lesson_user_list_next') }}
      </button>
    </div>
  </v-card>
</template>
