<script setup>
import { inject } from 'vue'
import { useLocale } from 'vuetify'
import * as Nav from '@/service/nav'
import {courseShortName, LESSON_EDIT_ITEM_RULE, LESSON_EDIT_ITEMS} from '@/plugins/constants'
import { useUserStore } from '@/plugins/store'

const appState = inject('appState')
const lessonEditItem = inject('lessonEditItem')

const props = defineProps(['week', 'lesson'])
const userStore = useUserStore()
const { t } = useLocale()

const selectItem = (item) => {
  lessonEditItem.value = item
  if (!props.lesson) return
  userStore.lessonEditItemIds[props.lesson.id] = item.id
}
</script>

<template>
  <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0&icon_names=sports_score">
  <v-navigation-drawer v-if="week" v-model="appState.leftDrawer">
    <v-list v-model="lessonEditItem" density="comfortable">
      <v-list-item :to="lesson ? new Nav.LessonDetail(lesson).routerPath() : new Nav.CourseDetail(week.course).routerPath()">
        <template #prepend>
          <v-icon size="small" icon="mdi-arrow-left" />
        </template>
        <template #title>
          <strong>{{ lesson ? `${week.name} (${lesson.name})` : courseShortName(week.course) }}</strong>
        </template>
      </v-list-item>

      <template v-for="item in LESSON_EDIT_ITEMS" :key="item.id">
        <v-list-item v-if="lesson || item.id !== LESSON_EDIT_ITEM_RULE" :title="t(item.name)"
                     :active="lessonEditItem.id === item.id" @click="() => selectItem(item)">
          <template #prepend>
            <span v-if="!item.icon.startsWith('mdi')" class="material-symbols-outlined"
                  :style="{ fontSize: '24px', marginRight: '25px' }">
              {{ item.icon }}
            </span>
            <v-icon v-else size="small" :icon="item.icon" />
          </template>
        </v-list-item>
      </template>
    </v-list>
  </v-navigation-drawer>
</template>
