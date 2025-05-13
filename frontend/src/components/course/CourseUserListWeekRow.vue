<script setup>

import {ref} from "vue";
import CourseDetailLessonRow from "@/components/course/CourseDetailLessonRow.vue";

defineProps(['week'])
const collapsed = ref(true)

</script>

<template>
  <tr style="cursor: pointer" @click="collapsed = !collapsed">
    <th v-ripple.center colspan="5">
      <div style="display: grid; width: 100%; height: 32px; grid-template-columns: 0.2fr 1.8fr 60% 2fr; align-items: center">
        <v-btn style="grid-column: 1" variant="text" density="compact" :icon="collapsed ? 'mdi-chevron-down' : 'mdi-chevron-up'" />
        <span style="grid-column: 2; display: flex; justify-content: space-between">
          <span style="text-align: start" class="pl-4">{{ week.name }}</span>
        </span>
        <span style="grid-column: 3" class="text-center">
          {{ new Date(week.from).toLocaleDateString(current === 'customCs' ? 'cs' : 'en') }}
          –
          {{ new Date(week.until).toLocaleDateString(current === 'customCs' ? 'cs' : 'en') }}
        </span>
      </div>
    </th>
  </tr>
  <template v-if="!collapsed">
    <CourseDetailLessonRow v-for="lesson in week.lessons" :key="lesson.id" style="width: 95%; margin: 0 2.5%" :lesson="lesson" />
  </template>
</template>

<style scoped>

</style>