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
                   :to="new Nav.LessonModuleUser(lesson, module, prevUser()).routerPath()">
        <v-btn class="me-4" density="compact">
          {{ t('$vuetify.lesson_user_list_previous') }}
        </v-btn>
      </router-link>
      <v-btn v-else class="me-4" density="compact" disabled>
        {{ t('$vuetify.lesson_user_list_previous') }}
      </v-btn>

      <router-link v-if="currentIndex() + 1 < lessonUsers.filter((u) => lessonUserFilter(userStore, u)).length"
                   :to="new Nav.LessonModuleUser(lesson, module, nextUser()).routerPath()">
        <v-btn class="me-4" density="compact">
          {{ t('$vuetify.lesson_user_list_next') }}
        </v-btn>
      </router-link>
      <v-btn v-else class="me-4" density="compact" disabled>
        {{ t('$vuetify.lesson_user_list_next') }}
      </v-btn>
    </div>
  </v-card>
</template>
