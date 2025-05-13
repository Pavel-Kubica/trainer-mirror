<script setup>
import MdFileListDisplayer from "@/components/MdFileListDisplayer.vue";
import {useRoute} from "vue-router";
import guides from "@/resources/guides.js"
import * as Nav from "@/service/nav";
import {computed, inject} from "vue";
import {useUserStore} from "@/plugins/store";

const route = useRoute();
const userStore = useUserStore()
const appState = inject('appState')
const mdFiles = computed(() => userStore.locale === 'customEn' ? guides[route.params.guide].mdFilesEn : guides[route.params.guide].mdFilesCz)
appState.value.navigation = [
  new Nav.CourseList(),
  new Nav.Guide(),
  new Nav.GuideDetail(guides[route.params.guide]),
  new Nav.GuideMarkdown(guides[route.params.guide], mdFiles.value[route.params.markdown])
]
appState.value.leftDrawer = true

</script>

<template>
  <MdFileListDisplayer :md-files="mdFiles" :markdown-id="route.params.markdown"
                       :return-route="new Nav.GuideDetail(guides[route.params.guide]).routerPath()"
  />
</template>

