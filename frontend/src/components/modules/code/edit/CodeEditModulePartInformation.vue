<script setup>
import {inject, provide, ref} from 'vue'
import { useLocale } from 'vuetify'
import {
  CODE_MODULE_FILE_LABELS,
  CODE_MODULE_FILE_LIMIT_VALUES,
  CODE_MODULE_INTERACTION_TYPE_VALUES,
  CODE_MODULE_LIBRARY_TYPE_VALUES,
  CODE_MODULE_TYPE_VALUES,
} from '@/plugins/constants'
import {useUserStore} from "@/plugins/store";
import TemplateList from "@/components/templates/TemplateList.vue";
import ModuleList from "@/components/lesson/ModuleList.vue";
import ImportFromGitlabDialog from "@/components/gitlab/ImportFromGitlabDialog.vue";

const useTemplateDialog = ref(false)
const fetchTestsFromModuleDialog = ref(false)
const importFromGitlabDialogShow = ref(false)
const moduleData = inject('moduleData')
const codeData = inject('codeData')
const props = defineProps(['removeModule', 'reload', 'error', 'readOnly', 'moduleId', 'addFile'])
const userStore = useUserStore()
const {t} = useLocale()
const translate = (key) => t(`$vuetify.code_module.edit_${key}`)


const moduleListState = ref({
  searchId: '',
  searchText: '',
  searchAuthors: [userStore.user.username],
  searchTypes: ['CODE'],
  searchTopics: [],
  searchSubjects: []
});

const templateListState = ref({
  searchId: '',
  searchText: '',
  searchAuthors: [userStore.user.username],
});

const fetchTestsFromModuleDialogDismiss = (shouldReload) => {
  fetchTestsFromModuleDialog.value = false
  if (shouldReload) props.reload()
}

const templateListDialogDismiss = (shouldReload) => {
  useTemplateDialog.value = false
  if (shouldReload) props.reload()
}

const importGitlabDialogDismiss = (shouldReload) => {
  importFromGitlabDialogShow.value = false
  if (shouldReload) props.reload()
}

provide('importFromGitlabDialogShow', importFromGitlabDialogShow)

</script>

<template>
  <v-dialog v-model="fetchTestsFromModuleDialog" width="80%">
    <ModuleList :lesson-id="null" :dismiss="fetchTestsFromModuleDialogDismiss" :reload="reload" :state="moduleListState" />
  </v-dialog>
  <v-dialog v-model="useTemplateDialog" width="80%">
    <TemplateList :lesson-id="null" :dismiss="templateListDialogDismiss" :reload="reload" :state="templateListState" />
  </v-dialog>
  <v-dialog v-model="importFromGitlabDialogShow" width="80%">
    <ImportFromGitlabDialog :dismiss="importGitlabDialogDismiss" :module-id="moduleId" :add-file="addFile" />
  </v-dialog>
  <v-card-title>
    <v-btn v-if="moduleData.type !== 'TEMPLATE' && !props.readOnly" class="mr-4" color="purple" variant="tonal" size="large"
           @click="useTemplateDialog = true">
      {{ t('$vuetify.template_use_button') }}
    </v-btn>
    <v-btn v-if="!props.readOnly" class="ma-4" color="indigo" variant="tonal" size="large" @click="fetchTestsFromModuleDialog = true">
      {{ t('$vuetify.template_module_test_button') }}
    </v-btn>
    <v-btn v-if="moduleData.type !== 'TEMPLATE' && !props.readOnly" variant="tonal" color="teal" size="large" class="ma-4" @click="importFromGitlabDialogShow = true">
      {{ t('$vuetify.edit_code_from_gitlab') }}
    </v-btn>
  </v-card-title>
  <v-select v-model="codeData.codeType" :label="translate('code_type')"
            :items="CODE_MODULE_TYPE_VALUES(t)" item-title="title" item-value="item" />
  <v-select v-model="codeData.interactionType" :label="translate('interaction')"
            :items="CODE_MODULE_INTERACTION_TYPE_VALUES(t)" item-title="title" item-value="item" />
  <v-select v-model="codeData.libraryType" :label="translate('library')"
            :items="CODE_MODULE_LIBRARY_TYPE_VALUES(t)" item-title="title" item-value="item" />
  <v-select v-if="codeData.codeType === 'WRITE_IO'" v-model="codeData.fileLimit" :label="translate('file_limit')"
            :items="CODE_MODULE_FILE_LIMIT_VALUES(t)" item-title="title" item-value="size" />
  <v-file-input v-if="['SHOWCASE', 'TEST_IO'].includes(codeData.codeType)"
                v-model="moduleData.file"
                :label="CODE_MODULE_FILE_LABELS(t)[codeData.codeType]" accept="application/x-tar" prepend-icon="mdi-archive" />
  <v-switch v-if="moduleData.type !== 'TEMPLATE'" v-model="codeData.referencePublic" :label="translate('reference_public')"
            class="mb-2 mx-2" color="primary" hide-details />
  <v-switch v-model="codeData.hideCompilerOutput" :label="translate('hide_compiler_output')" class="mb-2 mx-2"
            color="primary" hide-details />
</template>
