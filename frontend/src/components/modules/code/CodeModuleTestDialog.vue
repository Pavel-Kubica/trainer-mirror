<script setup>
import { ref, inject, watch } from 'vue'
import { useLocale } from 'vuetify'
import { createPatch } from 'diff'
import { html } from 'diff2html'
import { useUserStore } from '@/plugins/store'

const userStore = useUserStore()
const { t } = useLocale()
const codeData = inject('codeData')
const testDialogShown = inject('testDialogShown')
const testDialogModel = inject('testDialogModel')

const generateDiffHtml = () => {
  if (testDialogModel.value.diffHtml !== true)
    return testDialogModel.value.diffHtml

  // Generate the diff
  const diff = createPatch(t('$vuetify.code_module.error_output'), testDialogModel.value.outputText, testDialogModel.value.refText)
  return html(diff, {drawFileList: false, matching: 'lines',
    colorScheme: (userStore.darkMode ? 'dark' : 'light')})
}

const tabList = [
  t('$vuetify.code_module.tab_comparison'),
  t('$vuetify.code_module.tab_student'),
  t('$vuetify.code_module.tab_reference')
]
const selectedTab = ref(0)
watch(testDialogShown, () => setTimeout(() => selectedTab.value = 0, 300))
</script>

<template>
  <v-dialog v-model="testDialogShown" width="auto">
    <v-card>
      <div v-if="testDialogModel?.errorText" class="test-overflow overflow-auto">
        <v-card-item>
          <h4>{{ t('$vuetify.code_module.tab_compilation') }}</h4>
          <pre>{{ testDialogModel.errorText }}</pre>
        </v-card-item>
      </div>
      <template v-else>
        <v-tabs v-if="codeData.codeType !== 'SHOWCASE'" v-model="selectedTab">
          <v-tab v-for="(item, id) in tabList" :key="id" :value="id">
            {{ item }}
          </v-tab>
        </v-tabs>
        <div class="test-overflow overflow-auto">
          <v-card-item>
            <h4>{{ t('$vuetify.code_module.tab_input') }}</h4>
            <pre>{{ testDialogModel.inputText }}</pre>
          </v-card-item>
          <v-window v-if="testDialogModel" v-model="selectedTab" class="overflow-x-auto">
            <v-window-item v-for="(item, id) in tabList" :key="id" :value="id">
              <h4 v-if="codeData.codeType === 'SHOWCASE'" class="mx-4">
                {{ t('$vuetify.code_module.error_output') }}
              </h4>
              <v-card-item v-if="id === 0" v-html="generateDiffHtml()" />
              <v-card-item v-else class="mx-2">
                <h4>{{ item }}</h4>
                <pre>{{ id === 1 ? testDialogModel.outputText : testDialogModel.refText }}</pre>
              </v-card-item>
            </v-window-item>
          </v-window>
        </div>
      </template>
      <v-card-item class="pb-6">
        <v-btn :block="true" type="button" color="red" variant="tonal" @click="testDialogShown = false">
          {{ t('$vuetify.close') }}
        </v-btn>
      </v-card-item>
    </v-card>
  </v-dialog>
</template>

<style>
.d2h-info, .d2h-changed-tag {
  display: none !important;
}
.d2h-file-wrapper {
  margin-bottom: 0 !important;
}
.test-overflow .v-card-item__content {
 overflow: initial !important;
}
</style>
<style src="../../../../public/css/diff2html.min.css"></style>
