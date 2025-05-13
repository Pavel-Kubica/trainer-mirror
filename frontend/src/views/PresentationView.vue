<script setup>
import {computed, inject} from "vue";
import * as Nav from "@/service/nav";

import MdFileListDisplayer from "@/components/MdFileListDisplayer.vue";
import presentation from "@/resources/presentation";
import {useUserStore} from "@/plugins/store";

const appState = inject('appState')
appState.value.leftDrawer = undefined
appState.value.navigation = [new Nav.CourseList(), new Nav.Presentation()]

const userStore = useUserStore()
const presentationFiles = computed(() => userStore.locale === "customCs" ? presentation.mdFilesCz : presentation.mdFilesEn)
</script>

<template>
  <MdFileListDisplayer :md-files="presentationFiles" :markdown-id="0" />
</template>


