<script setup>
import LoadingScreen from "@/components/custom/LoadingScreen.vue";
import { gitlabImportApi } from "@/service/api";
import { inject, onMounted, provide, ref, watch } from "vue";
import { useUserStore } from "@/plugins/store";
import { useLocale } from "vuetify";
import TooltipIconButton from "@/components/custom/TooltipIconButton.vue";

const props = defineProps(["repositoryId", "moduleId", "addFile"]);
const gitlabFileList = ref(null);
const userStore = useUserStore();
const error = ref(null);
const searchName = ref("");
const path = ref("");
const addAsSelected = ref({});
const currentPage = ref(1);
const maxPage = ref(1);
const { t } = useLocale();
const codeDataInject = inject("codeData");
const moduleDataInject = inject("moduleData");
const importFromGitlabDialogShow = inject("importFromGitlabDialogShow");

provide("error", error);

const reloadDialog = async () => {
  gitlabFileList.value = null;
  gitlabImportApi
    .listAllFiles(
      userStore.gitlabToken,
      props.repositoryId,
      searchName.value,
      path.value,
      currentPage.value
    )
    .then((result) => {
      gitlabFileList.value = result.data;
      maxPage.value = result.nextPage;
    })
    .catch((err) => {
      error.value = err.code;
    });
};

const removeAfterLastSlash = (str) => {
  return str.substring(0, str.lastIndexOf("/"));
};

const findFileName = (filePath) => {
  return gitlabFileList.value.find((file) => file.path === filePath).name;
};

const addToModule = async () => {
  await Object.entries(addAsSelected.value).forEach(([key, value]) => {
    gitlabImportApi
      .getFileData(userStore.gitlabToken, props.repositoryId, key)
      .then((result) => {
        if (value === t("$vuetify.import_gitlab_file_list_type_description")) {
          moduleDataInject.value.assignment = result.content
        }
        if (value === t("$vuetify.import_gitlab_file_list_type_default_code")) {
          console.log(result);
          codeDataInject.value.files[
            codeDataInject.value.files.length - 1
          ].content = result.content;
          codeDataInject.value.files[
            codeDataInject.value.files.length - 1
          ].name = findFileName(key);
          props.addFile(
            codeDataInject.value.files[codeDataInject.value.files.length - 1]
          );
        }
        if (value === t("$vuetify.import_gitlab_file_list_type_test_code")) {
          codeDataInject.value.codeHidden = result.content
        }
      });
  });
  importFromGitlabDialogShow.value = false;
};

onMounted(async () => {
  await reloadDialog();
});

watch(path, async () => {
  currentPage.value = 1;
  await reloadDialog();
});

watch(currentPage, async () => {
  await reloadDialog();
});
</script>

<template>
  <LoadingScreen :items="gitlabFileList">
    <template #content>
      <div class="d-flex mt-4" style="gap: 16px">
        <v-text-field
          v-model="searchName"
          :label="t('$vuetify.gitlab_import_list_header_name')"
        />
        <v-btn
          variant="tonal"
          color="blue"
          :text="t('$vuetify.import_gitlab_list_list_header_search')"
          height="56px"
          @click="reloadDialog()"
        />
        <v-btn
          variant="tonal"
          color="green"
          height="56px"
          :text="t('$vuetify.import_gitlab_file_list_add_to_module')"
          @click="addToModule()"
        />
      </div>
      <v-table v-if="gitlabFileList.length" class="mb-4">
        <thead>
          <tr>
            <th
              v-for="title in ['name', 'path', 'add_file_as']"
              :key="title"
              :class="title === 'action' ? 'text-end' : ''"
            >
              {{ t(`$vuetify.gitlab_import_file_list_header_${title}`) }}
            </th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="file in gitlabFileList" :key="file.id">
            <td>
              <v-icon
                v-if="file.type === 'tree'"
                icon="mdi-folder"
                color="blue-lighten-2"
              />
              {{ file.name }}
            </td>
            <td>{{ file.path }}</td>
            <td width="20%">
              <div v-if="file.type === 'blob'" class="d-flex justify-end">
                <v-select
                  v-model="addAsSelected[file.path]"
                  clearable
                  variant="outlined"
                  :label="
                    t('$vuetify.gitlab_import_file_list_header_add_file_as')
                  "
                  :items="[
                    t('$vuetify.import_gitlab_file_list_type_description'),
                    t('$vuetify.import_gitlab_file_list_type_default_code'),
                    t('$vuetify.import_gitlab_file_list_type_test_code'),
                  ]"
                />
              </div>
              <div v-else-if="file.type === 'tree'" class="d-flex justify-end">
                <TooltipIconButton
                  icon="mdi-plus"
                  tooltip="$vuetify.gitlab_import_list_header_check_files_folder"
                  @click="path = file.path"
                />
              </div>
            </td>
          </tr>
        </tbody>
      </v-table>
      <v-row class="ma-2">
        <v-btn
          v-if="path !== ''"
          variant="tonal"
          @click="path = removeAfterLastSlash(path)"
        >
          {{ t("$vuetify.gitlab_return_back_folder") }}
        </v-btn>
      </v-row>
      <v-row justify="center" align="center" class="mb-2">
        <TooltipIconButton
          icon="mdi-menu-left"
          tooltip="$vuetify.gitlab_import_page_back"
          @click="currentPage > 1 ? currentPage-- : currentPage"
        />
        <p>{{ currentPage }}</p>
        <TooltipIconButton
          icon="mdi-menu-right"
          tooltip="$vuetify.gitlab_import_page_front"
          @click="currentPage < maxPage ? currentPage++ : currentPage"
        />
      </v-row>
    </template>
  </LoadingScreen>
</template>

<style scoped></style>
