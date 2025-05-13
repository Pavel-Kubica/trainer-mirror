<script setup>
import {inject} from 'vue'
// import { useUserStore } from '@/plugins/store'
import TooltipIconButton from '@/components/custom/TooltipIconButton.vue'
// import * as Nav from '@/service/nav'
import {useLocale} from "vuetify";
import {
  DEFAULT_CODE_CONTENT, MODULE_EDIT_CODE_TEST,
  MODULE_FILE_CODE
} from "@/plugins/constants";

const props = defineProps(['customSearch', 'dismiss', 'reloadDialog'])
// const userStore = useUserStore()
const { t } = useLocale()

const codeData = inject('codeData')
const templates = inject('templates')
const moduleEditTabList = inject('moduleEditTabList')

const useAsTemplate = async (template) => {
  console.log(template)
      codeData.value = {
        codeType: template.codeType,  interactionType: 'EDITOR', codeHidden: template.codeHidden,  fileLimit: template.fileLimit, referencePublic: false,
        envelopeType: template.envelopeType, customEnvelope: template.customEnvelope, libraryType: template.libraryType, assignment: '', tests: template.tests,
        files: [{name: MODULE_FILE_CODE, tmp: true, id: -1, codeLimit: 1024, content: DEFAULT_CODE_CONTENT, reference: '', headerFile: template.headerFile}],
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
      <div class="d-flex justify-end">
        <TooltipIconButton icon="mdi-plus" tooltip="$vuetify.template_module_test_title" color="rgb(var(--v-theme-anchor))"
                           @click="useAsTemplate(template)" />
      </div>
    </td>
  </tr>
</template>
