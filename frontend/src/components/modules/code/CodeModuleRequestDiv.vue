<script setup>
import { inject } from 'vue'
import { useLocale } from 'vuetify'
import ModuleRequestDiv from "@/components/lesson/ModuleRequestDiv.vue";

const runtime = inject('runtime')
const run = inject('run')
const codeData = inject('codeData')
const { t } = useLocale()

const sendSigterm = () => {
  for (let workerHost of runtime.value.workerHosts)
    workerHost.kill()
}
</script>

<template>
  <ModuleRequestDiv>
    <template #runtime>
      <v-btn v-if="runtime.workerHosts.length" class="flex-grow-1" color="red" variant="tonal"
             @click="sendSigterm">
        {{ t('$vuetify.code_module.terminate') }}
      </v-btn>
      <div class="d-flex justify-center align-center flex-grow-1">
        <v-progress-circular v-if="runtime.running" size="28" indeterminate />
      </div>
    </template>
    <template #runbtn>
      <v-btn style="color: rgb(var(--v-theme-anchor))" class="flex-grow-1"
             variant="tonal" @click="run">
        {{ codeData.codeType!=='SHOWCASE' ? t('$vuetify.code_module.run_tests') : t('$vuetify.code_module.run_showcase') }}
      </v-btn>
    </template>
  </ModuleRequestDiv>
</template>
