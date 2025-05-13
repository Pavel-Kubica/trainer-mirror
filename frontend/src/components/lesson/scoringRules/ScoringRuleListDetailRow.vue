<script setup>
import {useUserStore} from "@/plugins/store";
import {lessonModuleApi, scoringRuleApi, studentModuleApi} from "@/service/api";
import {computed, inject, onMounted, provide, ref, watch} from "vue";
import {useLocale} from "vuetify";
import TooltipIconButton from "@/components/custom/TooltipIconButton.vue";
import ScoringRuleModulesListDetailRow from "@/components/lesson/scoringRules/ScoringRuleModulesListDetailRow.vue";
import Vuedraggable from "vuedraggable";
import ScoringRuleAddModuleDialog from "@/components/lesson/scoringRules/ScoringRuleAddModuleDialog.vue";
import { OhVueIcon, addIcons } from "oh-vue-icons";
import { BiLayoutTextSidebarReverse } from "oh-vue-icons/icons/bi";
import ScoringRuleDescriptionDialog from "@/components/lesson/scoringRules/ScoringRuleDescriptionDialog.vue";
import DeleteDialog from "@/components/custom/DeleteDialog.vue";


const props = defineProps(['scoringRule','reload','edit','lessonId','lesson'])
const scoringRuleData = ref(props.scoringRule)
const userStore = useUserStore()
const {t} = useLocale()
const deleteDialog = ref(false)
const addModuleDialog = ref(false)
const completedModules = ref(0)
const modulesScoringRule = ref([])
const error = ref(null)
const editScoringRule = inject('editScoringRuleEntity')
const createEditDialog = inject('createEditDialog')
const editRuleDialog = inject('editRuleDialog')
const descriptionDialog = ref(false)
const studentModulesMap = ref({})
const hiddenScoringRules = ref({})

provide('descriptionDialog',descriptionDialog)
provide('deleteDialog', deleteDialog)
provide('scoringRuleData',scoringRuleData)
provide('editRuleDialog',editRuleDialog)

addIcons(BiLayoutTextSidebarReverse);

const moduleListState = ref({
  searchId: '',
  searchText: '',
  searchAuthors: [],
  searchTypes: [],
  searchTopics: [],
  searchSubjects: []
});

const reloadRule = async () => {
  scoringRuleApi.scoringRuleDetail(props.scoringRule.id)
      .then((result) => {
        scoringRuleData.value = result
      })
      .catch((err) => {
        error.value = err.code
      })
}

const loadModulesDetail = async () => {
  const ruleId = props.scoringRule.id
  const lessonId = props.lessonId
    const result = await scoringRuleApi.scoringRuleDetail(ruleId)
    scoringRuleData.value = result
    modulesScoringRule.value = result.modules
    if (!props.edit) {
      const modulePromises = modulesScoringRule.value.map(async (module) => {
        if (!studentModulesMap.value) studentModulesMap.value = {}
        studentModulesMap.value[module.id] = await studentModuleApi.getStudentModuleByModuleUserLesson(module.id, lessonId, userStore.user.id)

      })
      await Promise.all(modulePromises)
      completedModules.value = Object.values(studentModulesMap.value)
          .filter(item => item.completedOn !== null)
          .length;
    }
}

const removeModuleFromRule = async (module) => {
  await lessonModuleApi.getLessonModule(props.lessonId,module.id)
      .then((result) => {
        const lessonModuleId = result.id
        scoringRuleApi.removeScoringRuleModule(props.scoringRule.id, lessonModuleId)
            .then(props.reload)
            .catch((err) => { error.value = err.code })
      })
      .catch((err) => {
        error.value = err.code
      }).then(reloadRule())

}

const deleteScoringRule = async () => {
  await scoringRuleApi.deleteScoringRule(props.scoringRule.id)
  deleteDialog.value = false
  await props.reload()
}

const addModuleDialogDismiss = (shouldReload) => {
  addModuleDialog.value = false
  if (shouldReload) props.reload()
}


const modulesComplete = computed(() => {
  return completedModules.value.toString()
})

const modulesComplete2 = computed(() => {
  return completedModules.value
})
const deleteDialogTexts = computed(() => {
  return {
    itemName: scoringRuleData.value.name,
    title: '$vuetify.lesson_edit_rules_delete',
    start: '$vuetify.lesson_edit_rule_delete_text_p1',
    middle: '$vuetify.lesson_edit_rule_delete_text_p2',
    end: '$vuetify.irreversible_action',
  }
})

watch(props, loadModulesDetail)
onMounted(async () => {
  await loadModulesDetail()
})

</script>
<template>
  <ScoringRuleDescriptionDialog :lesson-id="props.lessonId" :scoring-rule="scoringRuleData" :callback="reload" />
  <DeleteDialog :item-name="deleteDialogTexts.itemName"
                :title="deleteDialogTexts.title"
                :text-start="deleteDialogTexts.start" :text-before-line-break="deleteDialogTexts.middle" :text-second-line="deleteDialogTexts.end"
                :on-cancel="() => deleteDialog = false"
                :on-confirm="deleteScoringRule"
                :text-confirm-button="'$vuetify.action_delete'" />
  <v-dialog v-model="addModuleDialog" width="100%">
    <ScoringRuleAddModuleDialog :lesson-id="props.lessonId" :dismiss="addModuleDialogDismiss"
                                :reload="reload" :state="moduleListState" :scoring-rule-id="props.scoringRule.id" />
    <!-- height 100% fixes the dialog to the top, but creates an invisible area below that otherwise wouldn't dismiss it -->
    <div style="flex: 1" @click="() => addModuleDialog = false" />
  </v-dialog>
  <tr style="cursor: pointer">
    <th v-ripple.center colspan="5">
      <div class="d-flex justify-space-between align-center" style="gap: 0">
        <span style="padding-left: 20px">
          {{ scoringRuleData.name }}
          <v-btn
            :style="{
              minWidth:'0',
              scale:'0.9',
              padding:'0',
              border:'none',
              background:'transparent',
              boxShadow:'none',
              display:'inline-flex'}"
            @click="(e) => { e.stopPropagation(); 
                             editScoringRule = scoringRuleData; 
                             descriptionDialog = true }">
            <OhVueIcon name="bi-layout-text-sidebar-reverse" scale="1.3" class="ms-2" /></v-btn>
        </span>
        <span class="flex-grow-1 text-center">{{ t('$vuetify.lesson_edit_rule_until') }} {{ new Date(scoringRuleData.until).toLocaleDateString(current === 'customCs' ? 'cs' : 'en') }}</span>
        <div
          :style="{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            backgroundColor: !props.edit ? ((modulesComplete2 === (props.scoringRule.toComplete ? props.scoringRule.toComplete : props.scoringRule.modules.length)) ? '#e9f5ea' : '#fde8e6') : '#e4e4e4' ,
            borderRadius: '15px',
            padding: '5px 10px',
            color: 'black',
            margin: '3px 0',
          }"
        >
          <span v-if="props.edit" class="flex-grow-1 text-center"> {{ t('$vuetify.lesson_edit_rule_to_complete') }}
            {{ scoringRuleData.toComplete ? scoringRule.toComplete : 0 }}/{{ scoringRuleData.modules.length }}</span>
          <span v-else>{{ modulesComplete }}/{{ scoringRuleData.toComplete ? scoringRuleData.toComplete : scoringRuleData.modules.length }}</span>
        </div>
        <span class="flex-grow-1 text-center"> {{ scoringRuleData.points }}{{ t('$vuetify.lesson_edit_rule_points') }}</span>
        <TooltipIconButton v-if="edit" icon="mdi-pencil"
                           @click="(e) => { e.stopPropagation(); editScoringRule = scoringRuleData; createEditDialog = true }" />
        <v-btn v-if="edit" variant="text"
               icon="" @click="(e) => e.stopPropagation()">
          <TooltipIconButton icon="mdi-plus" @click="addModuleDialog = true" />
          <v-tooltip activator="parent" location="top">
            {{ t('$vuetify.rule_new_module_button') }}
          </v-tooltip>
        </v-btn>
        <TooltipIconButton v-if="edit" icon="mdi-delete"
                           @click.stop="() => deleteDialog = true" />
        <v-btn variant="text" :icon="hiddenScoringRules[scoringRuleData.id] ? 'mdi-chevron-down' : 'mdi-chevron-up'"
               @click.prevent="hiddenScoringRules[scoringRuleData.id] = !hiddenScoringRules[scoringRuleData.id]" />
      </div>
    </th>
  </tr>
  <v-table v-if="!hiddenScoringRules[scoringRuleData.id]">
    <Vuedraggable v-model="modulesScoringRule" tag="tbody" item-key="id">
      <template #item="{ element: module }">
        <ScoringRuleModulesListDetailRow :key="module.id" :student-module="!props.edit ? studentModulesMap[module.id] : null" :module="module" :remove-module="removeModuleFromRule" :edit="edit" :lesson="props.lesson" />
      </template>
    </Vuedraggable>
  </v-table>
</template>
<style scoped>

</style>