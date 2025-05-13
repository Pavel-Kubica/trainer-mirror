<script setup>
import {ref, onMounted, provide, toRefs} from 'vue'
import { useLocale } from 'vuetify'
import { templateApi, } from '@/service/api'
import LoadingScreen from '@/components/custom/LoadingScreen.vue'
import TemplateListDialogRow from "@/components/templates/TemplateListDialogRow.vue";

const props = defineProps(['lessonId', 'dismiss', 'state', 'reload'])
const { t } = useLocale()
const { searchId, searchText, searchAuthors, } = toRefs(props.state)

const templates = ref(null)
provide('templates', templates)

const customSearch = (template) =>
    (!searchId.value || `${template.id}`.includes(searchId.value))
  && (!searchText.value || template.name.toLowerCase().includes(searchText.value.toLowerCase()))
  && (!searchAuthors.value.length || searchAuthors.value.includes(template.author.username))

const clearSearch = () => {
  searchId.value = ''
  searchText.value = ''
  searchAuthors.value = [searchAuthors.value[0]]
}

const reloadDialog = () => {templateApi.getTemplates().then((result) => {
    templates.value = result}).catch((err) => { err.value = err.code })}

onMounted(async () => { reloadDialog()})
</script>

<template>
  <v-card :title="t('$vuetify.template_module_test_title')">
    <LoadingScreen :items="templates">
      <template #content>
        <div class="d-flex mt-4" style="gap: 16px">
          <v-text-field v-model="searchId" class="flex-grow-0" type="number" :label="t('$vuetify.module_list_header_id')" />
          <v-text-field v-model="searchText" :label="t('$vuetify.module_list_header_name')" />
          <v-select v-model="searchAuthors[0]" :label="t('$vuetify.module_list_header_author')"
                    :items="[...new Set(templates.map((tp) => tp.author.username))].sort()" />
        </div>
        <v-table v-if="templates.length" class="mb-4">
          <thead>
            <tr>
              <th v-for="title in ['id', 'name', 'author', 'action']" :key="title"
                  :class="title === 'action' ? 'text-end' : ''">
                {{ t(`$vuetify.module_list_header_${title}`) }}
              </th>
            </tr>
          </thead>
          <tbody>
            <TemplateListDialogRow :custom-search="customSearch" :dismiss="dismiss" :reload-dialog="reloadDialog" :reload-source="reload" />
            <tr v-if="!templates.filter(customSearch).length">
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
