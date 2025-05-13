<script setup>
import {inject} from "vue";
import * as Nav from "@/service/nav";

import MdFileListDisplayer from "@/components/MdFileListDisplayer.vue";
import presentation from "@/resources/presentation";

const appState = inject('appState')

appState.value.navigation = [new Nav.CourseList(), new Nav.Presentation()]

</script>

<template>
  <MdFileListDisplayer :translated-md-files="presentation" :markdown-id="0" />
</template>


