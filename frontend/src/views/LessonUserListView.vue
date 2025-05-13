<script setup>
import LessonUserList from '@/components/LessonUserList.vue'
</script>

<template>
  <v-main>
    <LessonUserList :lesson-id="$route.params.lesson" />
  </v-main>
</template>
