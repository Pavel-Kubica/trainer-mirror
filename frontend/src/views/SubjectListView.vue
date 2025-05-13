<script setup>
import { inject } from 'vue'
import * as Nav from '@/service/nav'
import SubjectList from '@/components/SubjectList.vue';
import {useUserStore} from "@/plugins/store";
import SubjectListLeftDrawer from "@/components/subjects/SubjectListLeftDrawer.vue";

const appState = inject('appState')
const userStore = useUserStore()
appState.value.navigation = [new Nav.CourseList(), new Nav.SubjectsList(userStore.semester)]
appState.value.leftDrawer = true

</script>

<template>
  <v-navigation-drawer v-model="appState.leftDrawer">
    <SubjectListLeftDrawer :user-id="$route.params.user" :semester="$route.params.semester" />
  </v-navigation-drawer>
  <v-main>
    <SubjectList :user-id="$route.params.user" :semester="$route.params.semester" />
  </v-main>
</template>
