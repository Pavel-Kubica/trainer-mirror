<script setup>
import {inject} from 'vue'
import { useLocale } from 'vuetify'

const props = defineProps(['testId', 'addTest', 'readOnly'])
const codeData = inject('codeData')
const testToDelete = inject('testToDelete')
const fileToDelete = inject('fileToDelete')
const deleteDialog = inject('deleteDialog')

const { t } = useLocale()
const translate = (key) => t(`$vuetify.code_module.edit_test_${key}`)

const checkParamAndTimeLimit = () => {
  if (codeData.value.tests[props.testId].parameter)
    codeData.value.tests[props.testId].parameter = codeData.value.tests[props.testId].parameter.toString().replace(/[^0-9]/g, '');
  if (codeData.value.tests[props.testId].timeLimit)
    codeData.value.tests[props.testId].timeLimit = codeData.value.tests[props.testId].timeLimit.toString().replace(/[^0-9]/g, '');
}

</script>

<template>
  <v-text-field v-model="codeData.tests[testId].name" :label="translate('name')" />
  <v-text-field v-model="codeData.tests[testId].description" :label="translate('description')" />
  <v-text-field v-model="codeData.tests[testId].parameter" :label="translate('parameter')" type="text" @input="checkParamAndTimeLimit">
    <template #append-inner>
      <v-icon icon="mdi-information-outline" />
      <v-tooltip activator="parent" location="left">
        {{ translate('parameter_tooltip') }}
      </v-tooltip>
    </template>
  </v-text-field>
  <v-text-field v-model="codeData.tests[testId].timeLimit" :label="translate('time_limit')" type="text" @input="checkParamAndTimeLimit" />

  <v-switch v-model="codeData.tests[testId].checkMemory" :label="translate('check_memory')"
            class="ps-2" color="primary" hide-details />
  <v-switch v-model="codeData.tests[testId].hidden" :label="translate('hidden')"
            class="ps-2" color="primary" hide-details />
  <v-switch v-if="codeData.codeType === 'WRITE_ASSERT'" v-model="codeData.tests[testId].shouldFail"
            :label="translate('should_fail')" class="ps-2" color="primary" hide-details />

  <v-btn v-if="codeData.tests[testId].id" class="ms-2 mt-2 mb-4" color="red" :disabled="readOnly" :block="true" size="large" variant="tonal"
         @click="() => { testToDelete = codeData.tests[testId]; fileToDelete = null; deleteDialog = true }">
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
