<script setup>
import {inject, } from 'vue'
import {isSolvable, moduleBg, moduleBorder} from '@/plugins/constants'
import TextEditor from '@/components/custom/TextEditor.vue'
import ModuleTooltipTitle from '@/components/lesson/ModuleTooltipTitle.vue'
import {useLocale} from "vuetify";
import * as Nav from "@/service/nav";

// lesson is always defined
// module can be null/undefined, in which case we want to display lesson description
const props = defineProps(['requestToggle','isTeacher', 'solutionView']);
const lesson = inject('lesson')
const module = inject('module')
const { t } = useLocale();


</script>

<template>
  <v-card class="my-2 pa-2" :style="{'border-left': moduleBorder(lesson, module)}">
    <div class="d-flex justify-space-between">
      <v-card-title class="rounded px-4 flex-grow-1" :style="{background: moduleBg(module)}">
        <ModuleTooltipTitle :lesson="lesson" :module="module" :show-difficulty="true" />
        <v-progress-linear v-if="module && module.type !== 'TEXT'" color="rgb(var(--v-theme-progress))" :model-value="module.progress" />
      </v-card-title>
      <v-btn v-if="!solutionView && isTeacher && module && isSolvable(module)"
             variant="tonal" class="text-primary ml-6"
             :to="new Nav.LessonSolutionsModule(lesson, module, {id: -1}).routerPath()">
        {{ t('$vuetify.solutions') }}
      </v-btn>
    </div>
    <v-card-text :class="'overflow-y-auto pa-0 ma-0' + (module && module.type !== 'TEXT' ? module.type !== 'CODE' ?
      ' max-height-tab' : ' max-height-tab-code' :' max-height-no-tab')">
      <TextEditor :model-value="module ? module.assignment : lesson.description"
                  :editor-id="'md-editor-' + (module ? module.id : 'description')"
                  :editable="false" />
    </v-card-text>
    <div v-if="!isTeacher && !solutionView && module!==undefined">
      <v-btn color="blue" variant="tonal" width="160" @click="props.requestToggle">
        {{ t(`$vuetify.rating_btn`) }}
      </v-btn>
    </div>
  </v-card>
</template>

<style>
.md-editor-preview-wrapper { padding: 10px 0 0 0; }
</style>
