<script setup>
import * as Nav from '@/service/nav'
import TooltipIconButton from "@/components/custom/TooltipIconButton.vue";
import {provide, ref} from "vue";

const props = defineProps(['module','removeModule','edit','lesson','studentModule'])
const displayDeleteDialog = ref(false)
//const emit = defineEmits(['updateModule'])
provide('displayDeleteDialog', displayDeleteDialog)

</script>
<template>
  <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0">
  <tr>
    <td colspan="3">
      <div
        :style="{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          backgroundColor: !props.edit ? (props.studentModule?.completedOn ? '#e9f5ea' : '#fde8e6') : '#e4e4e4' ,
          borderRadius: '15px',
          padding: '5px 10px',
          color: 'black',
          margin: '3px 0',
        }"
      >
        <span>{{ module.name }}</span>
        <div style="display: flex; gap: 10px;">
          <!-- Reduced gap for icons -->
          <router-link v-if="edit" class="text-decoration-none" :to="new Nav.LessonModuleEdit(lesson, module).routerPath()" target="_blank">
            <TooltipIconButton icon="mdi-eye" tooltip="$vuetify.lesson_edit_modules_read" color="blue" style="font-size: 16px; width: 32px; height: 20px;" />
          </router-link>
          <TooltipIconButton v-if="edit" icon="mdi-delete" tooltip="$vuetify.rule_edit_modules_remove" color="red" style="font-size: 16px; width: 32px; height: 20px;" @click="removeModule(module)" />
          <router-link class="text-decoration-none" to="">
            <v-btn variant="text" icon="mdi-arrow-right" style="font-size: 16px; width: 32px; height: 20px;"
                   :to="new Nav.ModuleDetail(props.lesson, module).routerPath()" />
          </router-link>
        </div>
      </div>
    </td>
  </tr>
</template>