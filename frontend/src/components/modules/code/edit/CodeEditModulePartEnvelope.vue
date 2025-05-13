<script setup>
import {inject, onBeforeMount, watch} from 'vue'
import { useLocale } from 'vuetify'
import {
  CODE_MODULE_ENVELOPE_TYPE_VALUES
} from '@/plugins/constants'
import CodeEditor from '@/components/custom/CodeEditor.vue'
import {getEnvelope} from "@/plugins/code";

defineProps(['readOnly'])

const codeData = inject('codeData')
const { t } = useLocale()
const translate = (key) => t(`$vuetify.code_module.edit_${key}`)
watch(() => codeData.value.envelopeType, () => codeData.value.templateEnvelope = getEnvelope(codeData.value))
onBeforeMount(() => codeData.value.templateEnvelope = getEnvelope(codeData.value))
</script>

<template>
  <v-select v-model="codeData.envelopeType" :label="translate('envelope_type')"
            :items="CODE_MODULE_ENVELOPE_TYPE_VALUES(t)" item-title="title" item-value="item" />
  <CodeEditor v-if="codeData.envelopeType === 'ENV_CUSTOM'" class="mb-4" code-key="customEnvelope" :disabled="readOnly" />
  <CodeEditor v-else class="mb-4" code-key="templateEnvelope" :disabled="true" />
</template>
