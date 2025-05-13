<script setup>
import {inject} from 'vue'
import {scoringRuleApi} from '@/service/api'
import { useUserStore } from '@/plugins/store'
import TooltipIconButton from '@/components/custom/TooltipIconButton.vue'
import TooltipEmojiSpan from '@/components/custom/TooltipEmojiSpan.vue'
import {useLocale} from "vuetify";
const props = defineProps(['customSearch', 'dismiss', 'reloadDialog', 'reloadSource', 'topicChipColor', 'subjectChipColor'])
const userStore = useUserStore()

const {t} = useLocale()

const lessonModules = inject('lessonModules')
const lesson = inject('lesson')
const scoringRule = inject('scoringRule')
const error = inject('error')

const addModule = async (lessonModule) => {
  if (isModuleInScoringRule(lessonModule)) return;
  lessonModules.value = null
  scoringRuleApi.putScoringRuleModule(scoringRule.value.id, lessonModule.id)
      .then(() => {
        props.reloadSource()
        props.reloadDialog()
      })
      .catch((err) => { error.value = err.code })
}

function isModuleInScoringRule(lessonModule) {
  return scoringRule.value.modules.some((mod) => mod.id === lessonModule.module.id);
}

</script>

<template>
  <tr v-for="lessonModule in lessonModules.filter(customSearch)" :key="lessonModule.id">
    <td>#{{ lessonModule.module.id }}</td>
    <td>
      {{ lessonModule.module.name }}
      <TooltipEmojiSpan v-if="lessonModule.module.hidden" emoji="👻" />
    </td>
    <td>{{ lessonModule.module.author }}</td>
    <td>{{ lessonModule.module.type }}</td>
    <td>
      <div class="d-flex flex-wrap" style="gap: 3px; padding: 5px">
        <v-chip v-for="topic in lessonModule.module.topics" :key="topic.id" class="mr-2" :color="topicChipColor(topic)">
          {{ topic.name }}
        </v-chip>
      </div>
    </td>
    <td>
      <div class="d-flex flex-wrap" style="gap: 3px; padding: 5px">
        <v-chip v-for="subject in lessonModule.module.subjects" :key="subject.id" class="mr-2" :color="subjectChipColor(subject)">
          {{ subject.code }}
        </v-chip>
      </div>
    </td>
    <td>
      <div class="d-flex justify-end">
        <TooltipIconButton v-if="!isModuleInScoringRule(lessonModule) && (lesson && (userStore.isTeacher || userStore.isAdmin || lessonModule.module.author === userStore.user.username ||
                             lessonModule.module.editors.includes(userStore.user.username)))" icon="mdi-plus" tooltip="$vuetify.action_add"
                           color="rgb(var(--v-theme-anchor))" @click="() => addModule(lessonModule)" />
        <div v-else style="display: flex; align-items: center; justify-content: center; width: 48px; height: 48px">
          <v-icon icon="mdi-plus" color="grey" size="24" style="align-self: center" />
          <v-tooltip activator="parent" location="top">
            {{ t('$vuetify.module_list_cannot_add') }}
          </v-tooltip>
        </div>
      </div>
    </td>
  </tr>
</template>


