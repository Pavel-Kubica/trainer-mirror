<script setup>
import { useUserStore } from '@/plugins/store'
import * as Nav from '@/service/nav'
import TooltipIconButton from "@/components/custom/TooltipIconButton.vue";

const userStore = useUserStore()
defineProps(['module', 'removeModule'])

const canEdit = (module) => {
  return module.author === userStore.user.username || module.editors.includes(userStore.user.username) ||
      userStore.isAdmin || userStore.isGuarantor
}

</script>

<template>
  <tr>
    <td>{{ module.name }}</td>
    <td class="d-flex row justify-end">
      <router-link v-if="canEdit(module)" class="text-decoration-none" :to="new Nav.ModuleEdit(module).routerPath()">
        <TooltipIconButton icon="mdi-pencil" />
      </router-link>
      <router-link class="text-decoration-none" :to="new Nav.ModuleRead(module).routerPath()">
        <TooltipIconButton icon="mdi-eye" tooltip="$vuetify.lesson_edit_modules_read"
                           color="blue" />
      </router-link>
      <TooltipIconButton icon="mdi-delete" tooltip="$vuetify.lesson_edit_modules_remove"
                         color="red" @click="removeModule(module)" />
    </td>
  </tr>
</template>
