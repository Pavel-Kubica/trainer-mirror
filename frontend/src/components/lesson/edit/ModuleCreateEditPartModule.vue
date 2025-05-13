<script setup>
import { inject, ref, watch, onMounted } from 'vue'
import { useLocale } from 'vuetify'
import { useUserStore } from '@/plugins/store'
import {
  DIFFICULTY_VALUES,
  MODULE_TYPE_VALUES,
  MODULE_EDIT_CODE_INFO,
  MODULE_EDIT_CODE_TEST,
  MODULE_EDIT_CODE_STUD,
  MODULE_EDIT_CODE_ENV,
  MODULE_EDIT_CODE_TEAC,
  MODULE_EDIT_QUIZ_CREATE,
  MODULE_EDIT_SELFTEST_CREATE,
  MODULE_EDIT_ITEM_TEXT, MODULE_EDIT_ITEMS
} from '@/plugins/constants'

const props = defineProps(['lesson', 'teachers', 'topics', 'subjects', 'readOnly'])
const moduleData = inject('moduleData')
const moduleEditTabList = inject('moduleEditTabList')
const userStore = useUserStore()
const { t } = useLocale()
const moduleEditors = ref([])
const moduleTopics = inject('moduleTopics')
const moduleSubjects = inject('moduleSubjects')

const translate = (key) => t(`$vuetify.module_edit_${key}`)

const clearOtherTabs = () => {
  if (moduleData.value.type !== 'CODE') {
    delete moduleEditTabList.value[MODULE_EDIT_CODE_INFO]
    delete moduleEditTabList.value[MODULE_EDIT_CODE_TEST]
    delete moduleEditTabList.value[MODULE_EDIT_CODE_STUD]
    delete moduleEditTabList.value[MODULE_EDIT_CODE_ENV]
    delete moduleEditTabList.value[MODULE_EDIT_CODE_TEAC]
  }
  if(moduleData.value.type !== 'QUIZ'){
    delete moduleEditTabList.value[MODULE_EDIT_QUIZ_CREATE]

  }
  if(moduleData.value.type !== 'SELFTEST'){
    delete moduleEditTabList.value[MODULE_EDIT_SELFTEST_CREATE]
  }
  if(moduleEditTabList.value[MODULE_EDIT_ITEM_TEXT] === undefined){
    moduleEditTabList.value[MODULE_EDIT_ITEM_TEXT] = MODULE_EDIT_ITEMS[MODULE_EDIT_ITEM_TEXT]
  }
  if(moduleData.value.type === 'TEMPLATE'){
    delete moduleEditTabList.value[MODULE_EDIT_CODE_STUD]
    delete moduleEditTabList.value[MODULE_EDIT_ITEM_TEXT]
  }
}

/**
 * Custom logic for selecting all teachers
 * Vuetify could implement this in the future
 */
watch(moduleEditors, () => {
  if (!props.readOnly) {
    moduleData.value.editors = moduleEditors.value
  }
})

onMounted(() =>
    moduleEditors.value = moduleData.value.editors
)

</script>

<template>
  <v-text-field v-model="moduleData.name" :label="translate('name')" />
  <v-select v-if="!moduleData.id" v-model="moduleData.type" :label="translate('type')"
            :items="MODULE_TYPE_VALUES(t)" item-title="title" item-value="item" @update:modelValue="clearOtherTabs" />
  <v-select v-if="moduleData.type !== 'TEMPLATE'" v-model="moduleData.depends" :label="translate('depend')"
            :items="lesson.modules.filter((mod) => mod.id !== +moduleData.id)" item-title="name" item-value="id" />
  <v-select :label="translate('author')" disabled :model-value="moduleData.author.name" />
  <v-select v-model="moduleEditors" :label="translate('editors')"
            :disabled="moduleData.author.username !== userStore.user.username || props.readOnly" multiple :items="teachers" item-title="name" item-value="id" />
  <v-select v-model="moduleData.difficulty" :label="translate('difficulty')"
            :items="DIFFICULTY_VALUES(t)" item-title="title" item-value="item" />
  <v-select v-if="moduleData.type !== 'TEMPLATE'" v-model="moduleSubjects" :label="translate('subject')" multiple
            :items="subjects" item-title="name" item-value="id" />
  <v-select v-if="moduleData.type !== 'TEMPLATE'" v-model="moduleTopics" :label="translate('topic')" multiple
            :items="topics" item-title="name" item-value="id" />
  <v-label class="ms-2">
    {{ t('$vuetify.module_edit_min_percent', moduleData.minPercent) }}
  </v-label>
  <v-slider v-if="moduleData.type !== 'TEMPLATE'" v-model="moduleData.minPercent" color="primary" class="mx-6" min="0" max="100" step="1" />
  <div v-if="moduleData.type !== 'TEMPLATE'" class="mb-2 mx-2">
    <v-label>{{ translate('settings') }}</v-label>
    <v-switch v-model="moduleData.lockable" :label="translate('lockable')" class="mx-2"
              color="primary" hide-details />
    <v-switch v-model="moduleData.timeLimit" :label="translate('time_limit')" class="mx-2"
              color="primary" hide-details />
    <v-switch v-model="moduleData.manualEval" :label="translate('manual_eval')" class="mx-2"
              color="primary" hide-details />
    <v-switch v-model="moduleData.hidden" :label="translate('hidden')" class="mx-2"
              color="primary" hide-details />
  </div>
</template>
