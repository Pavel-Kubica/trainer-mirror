<script setup>
import {ref, provide, inject} from 'vue'
import { useDisplay } from 'vuetify'

import CodeModuleTestDialog from '@/components/modules/code/CodeModuleTestDialog.vue'
import CodeModuleTestPanels from '@/components/modules/code/CodeModuleTestPanels.vue'
import CodeModuleRequestDiv from '@/components/modules/code/CodeModuleRequestDiv.vue'

const { mobile } = useDisplay()
const testDialogShown = ref(false)
const testDialogModel = ref(null)
const codeData = inject('codeData')
provide('codeData',codeData)
provide('testDialogShown', testDialogShown)
provide('testDialogModel', testDialogModel)
</script>

<template>
  <CodeModuleTestDialog />
  <v-navigation-drawer v-if="!mobile" location="right" class="pa-2" width="400">
    <CodeModuleTestPanels />

    <template #append>
      <CodeModuleRequestDiv />
    </template>
  </v-navigation-drawer>
  <v-card v-else class="mt-4 mb-2">
    <CodeModuleTestPanels class="mx-2 mt-2" />
    <CodeModuleRequestDiv class="pt-4" />
  </v-card>
</template>
