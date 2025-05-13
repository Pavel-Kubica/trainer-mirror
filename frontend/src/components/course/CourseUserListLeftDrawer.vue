<script setup>
import { onMounted, ref } from 'vue'
import * as Nav from '@/service/nav'
import {courseApi} from '@/service/api'
import LoadingScreen from "@/components/custom/LoadingScreen.vue";
import {courseShortName} from "../../plugins/constants";

const props = defineProps(['courseId'])

const course = ref(null)

onMounted(async () => {
  course.value = await courseApi.courseDetail(props.courseId);
})
</script>

<template>
  <v-card class="mx-auto">
    <LoadingScreen :items="course">
      <template #items>
        <v-list-item class="pt-4 pb-3" :to="new Nav.CourseDetail(course).routerPath()">
          <template #prepend>
            <v-icon size="small" icon="mdi-arrow-left" />
          </template>
          <template #title>
            <strong>{{ courseShortName(course) }}</strong>
          </template>
        </v-list-item>
      </template>
    </LoadingScreen>
  </v-card>
</template>
