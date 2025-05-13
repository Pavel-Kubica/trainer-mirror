<script setup>
import MdFileListDisplayer from "@/components/MdFileListDisplayer.vue";
import {useRoute} from "vue-router";
import guides from "@/resources/guides.js"
import * as Nav from "@/service/nav";

const route = useRoute();

</script>

<template>
  <MdFileListDisplayer :translated-md-files="guides[route.params.guide]" :markdown-id="route.params.markdown"
                       :return-route="new Nav.GuideDetail(guides[route.params.guide]).routerPath()"
  />
</template>

