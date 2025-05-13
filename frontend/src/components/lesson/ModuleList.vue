<script setup>
import {ref, onMounted, provide, toRefs} from 'vue'
import { useLocale } from 'vuetify'
import {lessonApi, moduleApi, subjectApi, topicApi} from '@/service/api'
import LoadingScreen from '@/components/custom/LoadingScreen.vue'
import ModuleListDialogRow from '@/components/lesson/ModuleListDialogRow.vue'
import DeleteDialog from "@/components/custom/DeleteDialog.vue";
import {diacriticInsensitiveStringIncludes} from "@/plugins/constants";

const props = defineProps(['lessonId', 'dismiss', 'state', 'reload'])
const { t } = useLocale()
const { searchId, searchText, searchAuthors, searchTypes, searchTopics, searchSubjects } = toRefs(props.state)

const modules = ref(null)
const lesson = ref(null)
const error = ref(null)
const topics = ref(null)
const subjects = ref(null)
provide('modules', modules)
provide('lesson', lesson)
provide('error', error)
provide('topics', topics)
provide('subjects', subjects)

const moduleDeleteDialog = ref(false)
const moduleToDelete = ref(null)
provide('deleteDialog', moduleDeleteDialog)
provide('moduleToDelete', moduleToDelete)

const customSearch = (module) =>
     (!searchId.value || `${module.id}`.includes(searchId.value))
  && (!searchText.value || diacriticInsensitiveStringIncludes(module.name.toLowerCase(), searchText.value.toLowerCase()))
  && (!searchAuthors.value.length || searchAuthors.value.includes(module.author))
  && (!searchTypes.value.length || searchTypes.value.includes(module.type))
  && (!searchTopics.value.length || module.topics.map((t) => t.id).some((id) => searchTopics.value.includes(id)))
  && (!searchSubjects.value.length || module.subjects.map((s) => s.id).some((id) => searchSubjects.value.includes(id)))

const topicChipColor = (topic) => {
  if (searchTopics.value.includes(topic.id) || searchTopics.value.length === 0) return 'purple'
  return 'grey'
}

const subjectChipColor = (subject) => {
  if (searchSubjects.value.includes(subject.id) || searchSubjects.value.length === 0) return 'indigo'
  return 'grey'
}

const clearSearch = () => {
  searchId.value = ''
  searchText.value = ''
  searchAuthors.value = []
  searchTypes.value = []
  searchTopics.value = []
  searchSubjects.value = []
}

const deleteModule = (module) => {
  moduleDeleteDialog.value = false;
  moduleApi.deleteModule(module.id)
      .then(() => { reloadDialog(); props.reload(); })
      .catch((err) => { error.value = err.code })
}

const reloadDialog = () => {
  Promise.all([
    props.lessonId != null ? lessonApi.lessonDetail(props.lessonId) : null,
    moduleApi.moduleListShort(),
    topicApi.listTopics(),
    subjectApi.listSubjects()
  ])
  .then((result) => {
    let [ls, mod, top, sub] = result
    lesson.value = ls
    modules.value = mod
    topics.value = top.sort((a, b) => a.name.localeCompare(b.name))
    subjects.value = sub.sort((a, b) => a.code.localeCompare(b.code))
  })
  .catch((err) => { error.value = err.code })
}
onMounted(async () => { reloadDialog() })

</script>

<template>
  <DeleteDialog title="$vuetify.module_list_delete_module_title"
                text-start="$vuetify.module_list_delete_module_text_p1"
                :item-name="moduleToDelete?.name"
                text-before-line-break="$vuetify.module_list_delete_module_text_p2"
                text-second-line="$vuetify.irreversible_action"
                :on-cancel="() => moduleDeleteDialog = false"
                :on-confirm="() => deleteModule(moduleToDelete)"
                :text-confirm-button="'$vuetify.action_delete'" />
  <v-card :title="lesson ? t('$vuetify.module_list_add_title') : t('$vuetify.template_module_test_title')">
    <LoadingScreen :items="modules" :error="error">
      <template #content>
        <div class="d-flex mt-4" style="gap: 16px">
          <v-text-field v-model="searchId" style="flex: 0 0 15.5%" type="number" :label="t('$vuetify.module_list_header_id')" />
          <v-text-field v-model="searchText" style="flex: 0 0 15.5%" :label="t('$vuetify.module_list_header_name')" />
          <v-select v-model="searchAuthors" style="flex: 0 0 15.5%" :label="t('$vuetify.module_list_header_author')"
                    :items="[...new Set(modules.map((md) => md.author))].sort()"
                    multiple>
            <template #selection="{ index, item }">
              <v-chip v-if="searchAuthors.length && searchAuthors.length <= 2" size="small">
                <span>{{ item.title }}</span>
              </v-chip>
              <span v-else-if="searchAuthors.length && index === 0" class="text-caption align-self-center">
                {{ t('$vuetify.module_list_header_authors_selected', searchAuthors.length) }}
              </span>
            </template>
          </v-select>
          <v-select v-model="searchTypes" style="flex: 0 0 15.5%" :label="t('$vuetify.module_list_header_type')"
                    :items="lesson ? [...new Set(modules.map((md) => md.type))].sort() : ''" multiple />
          <v-autocomplete v-model="searchTopics" style="flex: 0 0 15.5%" :items="topics" item-title="name" item-value="id"
                          :label="t('$vuetify.module_list_header_topics')"
                          :no-data-text="t('$vuetify.module_list_topics_empty')"
                          multiple>
            <template #selection="{ index, item }">
              <v-chip v-if="searchTopics.length && searchTopics.length <= 2" size="small">
                <span>{{ item.title }}</span>
              </v-chip>
              <span v-else-if="searchTopics.length && index === 0" class="text-caption align-self-center">
                {{ t('$vuetify.module_list_header_topics_selected', searchTopics.length) }}
              </span>
            </template>
          </v-autocomplete>
          <v-autocomplete v-model="searchSubjects" style="flex: 0 0 15.5%" :items="subjects" item-title="code" item-value="id"
                          :label="t('$vuetify.module_list_header_subjects')"
                          :no-data-text="t('$vuetify.module_list_subjects_empty')"
                          multiple>
            <template #selection="{ index, item }">
              <v-chip v-if="searchSubjects.length && searchSubjects.length <= 2" size="small">
                <span>{{ item.title }}</span>
              </v-chip>
              <span v-else-if="searchSubjects.length && index === 0" class="text-caption align-self-center">
                {{ t('$vuetify.module_list_header_subjects_selected', searchSubjects.length) }}
              </span>
            </template>
          </v-autocomplete>
        </div>
        <v-table v-if="modules.length" class="mb-4">
          <thead>
            <tr>
              <th v-for="title in ['id', 'name', 'author', 'type', 'topics', 'subjects', 'action']" :key="title"
                  :class="title === 'action' ? 'ml-6' : ''">
                {{ t(`$vuetify.module_list_header_${title}`) }}
              </th>
            </tr>
          </thead>
          <tbody>
            <ModuleListDialogRow :custom-search="customSearch" :dismiss="dismiss" :reload-dialog="reloadDialog" :reload-source="reload"
                                 :topic-chip-color="topicChipColor" :subject-chip-color="subjectChipColor" />
            <tr v-if="!modules.filter(customSearch).length">
              <td colspan="7">
                {{ t('$vuetify.module_list_search_empty') }}
              </td>
            </tr>
          </tbody>
        </v-table>
        <span v-else class="mb-4">{{ t('$vuetify.module_list_empty') }}</span>
      </template>
    </LoadingScreen>
    <template #append>
      <v-btn class="ms-4" variant="tonal" @click="clearSearch">
        {{ t('$vuetify.module_list_clear_filters') }}
      </v-btn>
      <v-btn class="ms-4" variant="tonal" @click="dismiss(false)">
        {{ t('$vuetify.close') }}
      </v-btn>
    </template>
  </v-card>
</template>
