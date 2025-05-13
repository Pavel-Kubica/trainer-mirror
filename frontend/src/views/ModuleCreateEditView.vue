<script setup>
import {inject, ref, watch} from 'vue'
import ModuleCreateEdit from '@/components/ModuleCreateEdit.vue'
import { useRoute } from 'vue-router'
import {moduleApi} from "@/service/api";
import {useUserStore} from "@/plugins/store";

const userStore = useUserStore()
const route = useRoute();

const canEdit = (module) => {
  return module.author.username === userStore.user.username || module.editors.includes(userStore.user.username) ||
      userStore.isAdmin || userStore.isGuarantor
}

// Readonly if we are not creating a new module, and we cannot edit this one
const readOnly = ref(false);
watch(() => route.fullPath, async () => {
  readOnly.value = route.name !== 'module-create' && !canEdit(await moduleApi.moduleDetail(route.params.module));
}, { immediate: true })

const appState = inject('appState')
appState.value.leftDrawer = true
</script>

<template>
  <ModuleCreateEdit :module-id="$route.params.module" :lesson-id="$route.params.lesson" :read-only="readOnly" />
</template>
