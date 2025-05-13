<script setup>
import { inject, ref, provide } from 'vue'
import {useRoute} from 'vue-router'

import LessonModuleDetail from '@/components/LessonModuleDetail.vue'
import LessonSolutionsLeftDrawer from '@/components/lesson/solutions/LessonSolutionsLeftDrawer.vue'
import DeleteDialog from "@/components/custom/DeleteDialog.vue";

const usersReloadHook = ref(0)
const modulesReloadHook = ref(0)
const lessonUsers = ref(null)
provide('usersReloadHook', usersReloadHook)
provide('modulesReloadHook', modulesReloadHook)
provide('lessonUsers', lessonUsers)

const appState = inject('appState')
appState.value.leftDrawer = true

const route = useRoute()
const listUsers = ref(route.name === 'lesson-solutions-module')
provide('listUsers', listUsers)

const deleteDialog = ref(false);
const deleteDialogTexts = ref({
  itemName: '',
  middle: '',
  end: '$vuetify.irreversible_action',
}) // title and start text is filled in later
const doDelete = ref(() => {})
provide('deleteDialog', deleteDialog)
provide('deleteDialogTexts', deleteDialogTexts)
provide('doDelete', doDelete)

</script>

<template>
  <DeleteDialog :item-name="deleteDialogTexts.itemName"
                :title="deleteDialogTexts.title"
                :text-start="deleteDialogTexts.start" :text-before-line-break="deleteDialogTexts.middle" :text-second-line="deleteDialogTexts.end"
                :on-cancel="() => deleteDialog = false"
                :on-confirm="doDelete"
                :text-confirm-button="'$vuetify.action_delete'" />
  <v-navigation-drawer v-model="appState.leftDrawer" width="300">
    <LessonSolutionsLeftDrawer :lesson-id="$route.params.lesson" :module-id="$route.params.module" :user-id="$route.params.user" :users-toggled="$route.name === 'lesson-solutions-module'" />
  </v-navigation-drawer>
  <LessonModuleDetail :lesson-id="$route.params.lesson" :module-id="$route.params.module" :user="$route.params.user" />
</template>
