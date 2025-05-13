<script setup>
import {inject} from 'vue'
import { useUserStore } from '@/plugins/store'
import TooltipIconButton from '@/components/custom/TooltipIconButton.vue'
import {useLocale} from "vuetify";
import {
  DEFAULT_CODE_CONTENT_C, MODULE_EDIT_CODE_TEST,
  DEFAULT_CODE_FILENAME_C
} from "@/plugins/constants";

const props = defineProps(['customSearch', 'dismiss', 'reloadDialog'])
const userStore = useUserStore()
const { t } = useLocale()

const codeData = inject('codeData')
const templates = inject('templates')
const moduleEditTabList = inject('moduleEditTabList')
const templateDeleteDialog = inject('deleteDialog')
const templateToDelete = inject('templateToDelete')

const useAsTemplate = async (template) => {
  console.log(template)
      codeData.value = {
        codeType: template.codeType,  interactionType: 'EDITOR', codeHidden: template.codeHidden,  fileLimit: template.fileLimit, referencePublic: false,
        envelopeType: template.envelopeType, customEnvelope: template.customEnvelope, libraryType: template.libraryType, assignment: '', tests: template.tests,
        files: [{name: DEFAULT_CODE_FILENAME_C, tmp: true, id: -1, codeLimit: 1024, content: DEFAULT_CODE_CONTENT_C, reference: '', headerFile: template.headerFile}],
      }
      codeData.value.tests.forEach((test) => {
        test.id = -1
        test.realId = null
        console.log(test)
      })
      codeData.value.files.push({name: t('$vuetify.code_module.new_file')})
      codeData.value.tests.push({name: t('$vuetify.code_module.new_test')})

  moduleEditTabList.value[MODULE_EDIT_CODE_TEST].tabList = codeData.value.tests.map(
      (test, ix) => ({id: ix + 100, name: test.name})
  )
  props.reloadDialog()
}

</script>

<template>
  <tr v-for="template in templates.filter(customSearch)" :key="template.id">
    <td>#{{ template.id }}</td>
    <td>
      {{ template.name }}
    </td>
    <td>{{ template.author.username }}</td>
    <td>
      <div class="d-flex">
        <TooltipIconButton v-if="(template.author.username === userStore.user.username)" icon="mdi-delete" color="red"
                           @click="templateToDelete = template; templateDeleteDialog = true" />
        <div v-else style="display: flex; align-items: center; justify-content: center; width: 48px; height: 48px">
          <v-icon icon="mdi-delete" color="grey" size="24" style="align-self: center" />
          <v-tooltip activator="parent" location="top">
            {{ t('$vuetify.module_list_cannot_delete') }}
          </v-tooltip>
        </div>
        <div class="d-flex justify-end">
          <TooltipIconButton icon="mdi-plus" tooltip="$vuetify.template_module_test_title" color="rgb(var(--v-theme-anchor))"
                             @click="useAsTemplate(template)" />
        </div>
      </div>
    </td>
  </tr>
</template>
