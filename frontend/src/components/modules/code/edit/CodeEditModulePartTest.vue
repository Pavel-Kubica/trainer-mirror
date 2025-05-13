<script setup>
import { inject } from 'vue'
import { useLocale } from 'vuetify'

defineProps(['testId', 'addTest', 'deleteTest', 'readOnly'])
const codeData = inject('codeData')

const { t } = useLocale()
const translate = (key) => t(`$vuetify.code_module.edit_test_${key}`)
</script>

<template>
  <v-text-field v-model="codeData.tests[testId].name" :label="translate('name')" />
  <v-text-field v-model="codeData.tests[testId].description" :label="translate('description')" />
  <v-text-field v-model="codeData.tests[testId].parameter" :label="translate('parameter')" type="number" />
  <v-text-field v-model="codeData.tests[testId].timeLimit" :label="translate('time_limit')" type="number" />
  <v-switch v-model="codeData.tests[testId].checkMemory" :label="translate('check_memory')"
            class="ps-2" color="primary" hide-details />
  <v-switch v-model="codeData.tests[testId].hidden" :label="translate('hidden')"
            class="ps-2" color="primary" hide-details />
  <v-switch v-if="codeData.codeType === 'WRITE_ASSERT'" v-model="codeData.tests[testId].shouldFail"
            :label="translate('should_fail')" class="ps-2" color="primary" hide-details />

  <v-btn v-if="codeData.tests[testId].id" class="ms-2 mt-2 mb-4" color="red" :disabled="readOnly" :block="true" size="large" variant="tonal"
         @click="() => deleteTest(codeData.tests[testId])">
    {{ t('$vuetify.code_module.delete_test') }}
  </v-btn>
  <div v-else>
    <v-btn v-if="!readOnly" class="ms-2 mt-2 mb-4" color="green" :block="true" size="large" variant="tonal"
           :disabled="!codeData.tests[testId].parameter ||
             !codeData.tests[testId].name || !codeData.tests[testId].description" @click="() => addTest(codeData.tests[testId])">
      {{ t('$vuetify.code_module.create_test') }}
    </v-btn>
    <v-tooltip activator="parent" location="top">
      {{ t('$vuetify.code_module.create_test_requirements') }}
    </v-tooltip>
  </div>
</template>
