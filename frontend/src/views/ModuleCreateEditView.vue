<script setup>
import { inject } from 'vue'
import { onBeforeRouteLeave } from 'vue-router'
import ModuleCreateEdit from '@/components/ModuleCreateEdit.vue'
import { useRoute } from 'vue-router'

const route = useRoute();
const readOnly = route.name === 'module-read';


const appState = inject('appState')
appState.value.leftDrawer = true
onBeforeRouteLeave((to) => {
  if (['module-create', 'module-edit'].includes(to.name)) return
  appState.value.leftDrawer = undefined
  appState.value.rightDrawer = undefined
})
</script>

<template>
  <ModuleCreateEdit :module-id="$route.params.module" :lesson-id="$route.params.lesson" :read-only="readOnly" />
</template>
