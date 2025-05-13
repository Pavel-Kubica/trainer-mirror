<script setup>
import { ref, inject } from 'vue'
import { useLocale } from 'vuetify'
import Vuedraggable from 'vuedraggable'

import * as Nav from '@/service/nav'
import ModuleList from '@/components/lesson/ModuleList.vue'
import ModuleListRow from "@/components/lesson/edit/ModuleListRow.vue";
import LoadingScreen from "@/components/custom/LoadingScreen.vue";
import {useUserStore} from "@/plugins/store";

const addModuleDialog = ref(false)
const lessonData = inject('lessonData')
const props = defineProps(['removeModule', 'reload', 'error'])
const userStore = useUserStore()
const { t } = useLocale()

const moduleListState = ref({
  searchId: '',
  searchText: '',
  searchAuthors: [userStore.user.username],
  searchTypes: [],
  searchTopics: [],
  searchSubjects: []
});

const addModuleDialogDismiss = (shouldReload) => {
  addModuleDialog.value = false
  if (shouldReload) props.reload()
}
</script>

<template>
  <v-dialog v-model="addModuleDialog" width="80%">
    <ModuleList :lesson-id="lessonData?.id" :dismiss="addModuleDialogDismiss" :reload="reload" :state="moduleListState" />
  </v-dialog>
  <LoadingScreen :items="lessonData" :error="error">
    <template #items>
      <v-table v-if="lessonData.modules.length">
        <Vuedraggable v-model="lessonData.modules" tag="tbody" item-key="id">
          <template #item="{ element: module }">
            <ModuleListRow :module="module" :remove-module="removeModule" />
          </template>
        </Vuedraggable>
      </v-table>
      <span v-else>
        <em>{{ t('$vuetify.lesson_edit_modules_empty') }}</em>
      </span>

      <v-card-actions class="mb-4" style="justify-content: space-between">
        <v-btn color="purple" size="large" variant="tonal" @click="addModuleDialog = true">
          {{ t('$vuetify.lesson_edit_modules_add') }}
        </v-btn>
        <router-link class="text-decoration-none" :to="new Nav.ModuleCreate().routerPath()">
          <v-btn color="green" size="large" variant="tonal">
            {{ t('$vuetify.lesson_edit_modules_create') }}
          </v-btn>
        </router-link>
      </v-card-actions>
    </template>
  </LoadingScreen>
</template>
