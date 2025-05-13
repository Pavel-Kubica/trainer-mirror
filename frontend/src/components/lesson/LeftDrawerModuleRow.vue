<script setup>
import * as Nav from "@/service/nav";
import TooltipIconButton from "@/components/custom/TooltipIconButton.vue";
import ModuleTooltipTitle from "@/components/lesson/ModuleTooltipTitle.vue";
import {useUserStore} from "@/plugins/store";
import {inject, ref} from "vue";
import {lessonModuleApi} from "@/service/api";
import {useLocale} from "vuetify";
const userStore = useUserStore()

const props = defineProps(['lesson', 'module', 'isTeacher', 'userId'])
const appState = inject('appState')
const {t} = useLocale()

const removeDialog = inject('deleteDialog', undefined);
const removeDialogTexts = inject('deleteDialogTexts')
const doRemove = inject('doDelete')
const modulesReloadHook = inject('modulesReloadHook')
const lessonModuleToRemove = ref(null)

const onClickRemoveButton = () => {
  lessonModuleToRemove.value = {lessonId: props.lesson.id, moduleId: props.module.id, name: props.module.name};
  removeDialogTexts.value = {
    title: "$vuetify.lesson_edit_modules_remove",
    itemName: lessonModuleToRemove.value?.name,
    textStart: "$vuetify.module_remove_dialog_text_p1",
    textMiddle: "$vuetify.module_remove_dialog_text_p2",
    textEnd: "$vuetify.module_remove_dialog_text_p3",
    textConfirmButton: "$vuetify.action_remove"
  }
  doRemove.value = () => {
    lessonModuleApi.removeLessonModule(lessonModuleToRemove.value.lessonId, lessonModuleToRemove.value.moduleId).then(() => {
      modulesReloadHook.value = new Date().getTime();
      appState.value.notifications.push({
        type: "success", title: t('$vuetify.module_remove_success_title'), text: t('$vuetify.module_remove_success_text')
      })
    }).catch((err) => {
      appState.value.notifications.push({
        type: "error", title: t('$vuetify.module_remove_failure_title'), text: t('$vuetify.module_remove_failure_text', err.code)
      })
    })
    removeDialog.value = false;
  }

  removeDialog.value = true
}

const canEdit = (module) => {
  if (!module) return false;
  return module.author === userStore.user.username || module.editors.includes(userStore.user.username);
};

</script>

<template>
  <div class="d-flex justify-space-between align-center">
    <ModuleTooltipTitle :lesson="lesson" :module="module" class="module-row-handle-text" style="justify-self: start; max-width: 75%; overflow-wrap: break-word" />
    <div v-if="isTeacher && !userId && !userStore.anonymous" class="d-flex justify-end">
      <TooltipIconButton :icon="canEdit(module) ? 'mdi-pencil' : 'mdi-eye'" icon-size="20" color="rgb(var(--v-theme-anchor))"
                         :tooltip="canEdit(module) ? '$vuetify.module_edit' :'$vuetify.module_view'"
                         density="compact" :to="new Nav.LessonModuleEdit(lesson, module).routerPath()" />
      <TooltipIconButton icon="mdi-delete" icon-size="20" color="red" tooltip="$vuetify.lesson_edit_modules_remove"
                         density="compact" @click.prevent.stop="onClickRemoveButton" />
    </div>
  </div>
</template>