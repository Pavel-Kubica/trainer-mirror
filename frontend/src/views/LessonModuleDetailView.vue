<script setup>
import {inject, ref, provide} from 'vue'

import LessonModuleDetail from '@/components/LessonModuleDetail.vue'
import LessonDetailLeftDrawer from '@/components/lesson/LessonDetailLeftDrawer.vue'
import ModuleList from "@/components/lesson/ModuleList.vue";
import DeleteDialog from "@/components/custom/DeleteDialog.vue";

const lessonUsers = ref(null)
const modulesReloadHook = ref(0)
const file = ref([])
const addModuleDialog = ref(false)
const addModuleTargetLessonId = ref(false)
provide('modulesReloadHook', modulesReloadHook)
provide('lessonUsers', lessonUsers)
provide('fileDropped', file)
provide('addModuleDialog', addModuleDialog);
provide('addModuleTargetLessonId', addModuleTargetLessonId);

const moduleListState = ref({
  searchId: '',
  searchText: '',
  searchAuthors: [],
  searchTypes: [],
  searchTopics: [],
  searchSubjects: []
});

const appState = inject('appState')
appState.value.leftDrawer = true

function onDrop(e) {
  e.preventDefault()
  file.value = e.dataTransfer.files
}

const dragOver = (e) => {
  e.preventDefault()
}

const addModuleDialogDismiss = () => {
  addModuleDialog.value = false;
}

const reloadLeftDrawer = () => {
  modulesReloadHook.value = new Date().getTime();
}

const deleteDialog = ref(false);
const deleteDialogTexts = ref({
  title: "",
  itemName: "",
  textStart: "",
  textMiddle: "",
  textEnd: "",
  textConfirmButton: ""
})
const doDelete = ref(() => {})
provide('deleteDialog', deleteDialog)
provide('deleteDialogTexts', deleteDialogTexts)
provide('doDelete', doDelete)

</script>

<template>
  <div style="min-height: 100%" @drop="onDrop" @dragover="dragOver">
    <DeleteDialog :title="deleteDialogTexts.title"
                  :item-name="deleteDialogTexts.itemName"
                  :on-confirm="doDelete"
                  :on-cancel="() => deleteDialog = false"
                  :text-start="deleteDialogTexts.textStart"
                  :text-before-line-break="deleteDialogTexts.textMiddle"
                  :text-second-line="deleteDialogTexts.textEnd"
                  :text-confirm-button="deleteDialogTexts.textConfirmButton" />
    <v-dialog v-model="addModuleDialog" width="80%" height="100%">
      <ModuleList :lesson-id="addModuleTargetLessonId" :dismiss="addModuleDialogDismiss" :reload="reloadLeftDrawer" :state="moduleListState" />
      <!-- height 100% fixes the dialog to the top, but creates an invisible area below that otherwise wouldn't dismiss it -->
      <div style="flex: 1" @click="() => addModuleDialog = false" />
    </v-dialog>
    <v-navigation-drawer v-model="appState.leftDrawer">
      <LessonDetailLeftDrawer :lesson-id="$route.params.lesson" :module-id="$route.params.module" :scoring-rule-id="$route.params.scoringRule"
                              :user-id="$route.params.user" />
    </v-navigation-drawer>
    <LessonModuleDetail :lesson-id="$route.params.lesson" :module-id="$route.params.module" :scoring-rule-id="$route.params.scoringRule" :user="$route.params.user" />
  </div>
</template>
