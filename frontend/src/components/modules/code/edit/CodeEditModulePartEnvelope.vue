<script setup>
import { inject } from 'vue'
import { useLocale } from 'vuetify'
import {
  CODE_MODULE_ENVELOPE_TYPE_VALUES
} from '@/plugins/constants'
import CodeEditor from '@/components/custom/CodeEditor.vue'

const codeData = inject('codeData')
const { t } = useLocale()
const translate = (key) => t(`$vuetify.code_module.edit_${key}`)
</script>

<template>
  <v-select v-model="codeData.envelopeType" :label="translate('envelope_type')"
            :items="CODE_MODULE_ENVELOPE_TYPE_VALUES(t)" item-title="title" item-value="item" />
  <CodeEditor v-if="codeData.envelopeType === 'ENV_CUSTOM'" class="mb-4" code-key="customEnvelope" />
</template>
