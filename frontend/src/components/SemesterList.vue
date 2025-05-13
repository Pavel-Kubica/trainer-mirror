<script setup>
import {onMounted, ref, provide} from 'vue'
import { semesterApi } from '@/service/api'
import LoadingScreen from '@/components/custom/LoadingScreen.vue'
import SemesterListRow from "@/components/semester/SemesterListRow.vue";
import SemesterListHeader from "@/components/semester/SemesterListHeader.vue";


const semesters = ref(null)
const error = ref(null)

onMounted(async () => {
  fetchSemesters()
})

function fetchSemesters() {
  semesterApi.getAllSemesters()
      .then((result) => {
        semesters.value = result
      })
      .catch((err) => {error.value = err.code})
}
provide('fetchSemesters', fetchSemesters)
</script>

<template>
  <SemesterListHeader />
  <v-card class="mx-8 my-4">
    <LoadingScreen :items="semesters" :error="error">
      <template #table>
        <SemesterListRow v-for="semester in semesters" :key="semester.id" :semester="semester"
                         @semesterDeleted="fetchSemesters" />
      </template>
    </LoadingScreen>
  </v-card>
</template>