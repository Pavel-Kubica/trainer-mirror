<script setup>
import CourseUserImport from '@/components/CourseUserImport.vue'
</script>

<template>
  <v-main>
    <CourseUserImport :course="$route.params.course" />
  </v-main>
</template>
