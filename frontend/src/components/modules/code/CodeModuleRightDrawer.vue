<script setup>
import {ref, provide, inject} from 'vue'
import { useDisplay } from 'vuetify'

import CodeModuleTestDialog from '@/components/modules/code/CodeModuleTestDialog.vue'
import CodeModuleTestPanels from '@/components/modules/code/CodeModuleTestPanels.vue'
import CodeModuleTestButton from '@/components/modules/code/CodeModuleTestButton.vue'
import SideBySideToggle from "@/components/custom/SideBySideToggle.vue";
import ModuleRequestDrawer from "@/components/lesson/ModuleRequestDrawer.vue";

const { mobile } = useDisplay()
const testDialogShown = ref(false)
const testDialogModel = ref(null)
const codeData = inject('codeData')
provide('codeData',codeData)
provide('testDialogShown', testDialogShown)
provide('testDialogModel', testDialogModel)

const testsToggled = ref(true)

</script>

<template>
  <CodeModuleTestDialog />
  <v-navigation-drawer v-if="!mobile" location="right" width="400">
    <SideBySideToggle style="height: 8%; border-bottom: thin grey solid"
                      :start-selection-left="true"
                      text-left="$vuetify.right_drawer_compilation" text-right="$vuetify.right_drawer_requests"
                      :on-click-left="() => { testsToggled = true; return true; }" :on-click-right="() => { testsToggled = false; return true; }" />
    <div v-if="testsToggled" class="pa-2 d-flex flex-column justify-space-between" style="height: 92%">
      <CodeModuleTestPanels />
      <CodeModuleTestButton />
    </div>
    <ModuleRequestDrawer v-else />
  </v-navigation-drawer>
  <v-card v-else class="mt-4 mb-2">
    <CodeModuleTestPanels class="mx-2 mt-2" />
    <CodeModuleTestButton class="pt-4" />
    <ModuleRequestDrawer />
  </v-card>
</template>
