<script setup>
import CourseUserList from '@/components/CourseUserList.vue'
</script>

<template>
  <v-main>
    <CourseUserList :course="$route.params.course" />
  </v-main>
</template>
