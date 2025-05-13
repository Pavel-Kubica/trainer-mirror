<script setup>
import { inject } from 'vue'
import { useLocale } from 'vuetify'
import { testBorder, testTitle } from '@/plugins/constants'

const { t } = useLocale()
const runtime = inject('runtime')
const testDialogShown = inject('testDialogShown')
const testDialogModel = inject('testDialogModel')
</script>

<template>
  <v-expansion-panels v-model="runtime.openedTests" multiple>
    <v-expansion-panel v-for="test in runtime.tests" :key="test.id"
                       class="ma-2" :style="{'border-left': testBorder(test)}">
      <v-expansion-panel-title>
        <div class="pe-4 d-flex justify-space-between fill-width z-index-1">
          <h3>
            {{ test.name }}
            <v-tooltip activator="parent" location="top">
              {{ t('$vuetify.code_module.test_name_tooltip') }}
            </v-tooltip>
          </h3>
          <h3 style="white-space: pre">
            {{ testTitle(test) }}
            <v-tooltip activator="parent" location="top">
              {{ t('$vuetify.code_module.tests_passed_total_tooltip') }}
            </v-tooltip>
          </h3>
        </div>
      </v-expansion-panel-title>
      <v-expansion-panel-text>
        <v-alert v-if="test.error" class="mb-2" type="error" variant="tonal" :text="test.error" />
        <v-btn v-if="test.errorText || ((test.diffHtml || test.inputText) && !test.hidden)" class="mb-4" color="rgb(var(--v-theme-anchor))" variant="tonal"
               :block="true" @click="() => {
                 testDialogModel = test; testDialogShown = true
               }">
          {{ t(`$vuetify.code_module.tests_show_${test.errorText ? 'error' : 'outputs'}`) }}
        </v-btn>
        <template v-if="test.description">
          <b>{{ t('$vuetify.code_module.tests_description') }}</b>
          {{ test.description }}
        </template>
      </v-expansion-panel-text>
    </v-expansion-panel>
  </v-expansion-panels>
</template>

<style scoped>
.fill-width {
  width: 100%
}

.z-index-1 {
  z-index: 1;
}
</style>
