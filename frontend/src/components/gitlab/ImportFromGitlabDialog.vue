<script setup>
import LoadingScreen from "@/components/custom/LoadingScreen.vue";
import {useLocale} from "vuetify";
import {onMounted, provide, ref, watch} from "vue";
import {useUserStore} from "@/plugins/store";
import {gitlabImportApi} from "@/service/api";
import TooltipIconButton from "@/components/custom/TooltipIconButton.vue";
import ImportFromGitlabFIleDialog from "@/components/gitlab/ImportFromGitlabFIleDialog.vue";

defineProps(['dismiss', 'moduleId', 'addFile'])
const {t} = useLocale()
const gitlabProjectsList = ref(null)
const userStore = useUserStore()
const error = ref(null)
const currentPage = ref(1)
const maxPage = ref(1)
const repositoryId = ref(-1)

const searchName = ref("")
const ownedType = ref(t('$vuetify.import_gitlab_list_list_header_type_true'))
const ownedTypeBool = ref(false)

provide('error', error)

const reloadDialog = async () => {
  gitlabProjectsList.value = null
  ownedTypeBool.value = ownedType.value === t('$vuetify.import_gitlab_list_list_header_type_true');
  gitlabImportApi.listAllProject(userStore.gitlabToken, ownedTypeBool.value, searchName.value, currentPage.value).then((result) => {
    gitlabProjectsList.value = result.data
    maxPage.value = result.nextPage
  }).catch((err) => {
    error.value = err.code
  })
}

onMounted(async () => {
  await reloadDialog()
})

watch(currentPage, async () => {
  await reloadDialog()
});
</script>

<template>
  <v-card title="Import From Gitlab">
    <ImportFromGitlabFIleDialog v-if="repositoryId !== -1" :repository-id="repositoryId" :module-id="moduleId" :add-file="addFile" />
    <LoadingScreen v-else :items="gitlabProjectsList">
      <template #content>
        <div class="d-flex mt-4" style="gap: 16px">
          <v-text-field v-model="searchName"
                        :label="t('$vuetify.gitlab_import_list_header_name')" />
          <v-select v-model="ownedType" :label="t('$vuetify.module_list_header_type')"
                    :items="[t('$vuetify.import_gitlab_list_list_header_type_true'), t('$vuetify.import_gitlab_list_list_header_type_false')]" />
          <v-btn variant="tonal" color="blue" :text="t('$vuetify.import_gitlab_list_list_header_search')" height="56px"
                 @click="currentPage = 1; reloadDialog()" />
        </div>
        <v-table v-if="gitlabProjectsList.length" class="mb-4">
          <thead>
            <tr>
              <th v-for="title in ['name', 'description', 'check_files']" :key="title"
                  :class="title === 'action' ? 'text-end' : ''">
                {{ t(`$vuetify.gitlab_import_list_header_${title}`) }}
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="project in gitlabProjectsList" :key="project.id">
              <td> {{ project.name }}</td>
              <td> {{ project.description ? project.description : "---" }}</td>
              <td>
                <div class="d-flex justify-end">
                  <TooltipIconButton icon="mdi-plus" tooltip="$vuetify.gitlab_import_list_header_check_files" @click="repositoryId = project.id" />
                </div>
              </td>
            </tr>
          </tbody>
        </v-table>
        <v-row justify="center" align="center" class="mb-2">
          <TooltipIconButton icon="mdi-menu-left" tooltip="$vuetify.gitlab_import_page_back"
                             @click="currentPage > 1 ? currentPage-- : currentPage" />
          <p>{{ currentPage }}</p>
          <TooltipIconButton icon="mdi-menu-right" tooltip="$vuetify.gitlab_import_page_front"
                             @click="currentPage < maxPage ? currentPage++ : currentPage" />
        </v-row>
      </template>
    </LoadingScreen>
    <template #append>
      <v-btn v-if="repositoryId !== -1" class="ms-4" variant="tonal" @click="repositoryId = -1">
        {{ t('$vuetify.gitlab_back') }}
      </v-btn>
      <v-btn class="ms-4" variant="tonal"
             @click="searchName = ''; ownedType = t('$vuetify.import_gitlab_list_list_header_type_false'); currentPage = 1; reloadDialog()">
        {{ t('$vuetify.module_list_clear_filters') }}
      </v-btn>
      <v-btn class="ms-4" variant="tonal" @click="dismiss(false)">
        {{ t('$vuetify.close') }}
      </v-btn>
    </template>
  </v-card>
</template>

<style scoped>

</style>