<script setup>
import {onMounted, ref} from "vue";
import {courseApi} from "@/service/api";
import LoadingScreen from "@/components/custom/LoadingScreen.vue";
import CourseUserListWeekRow from "@/components/course/CourseUserListWeekRow.vue";

const props = defineProps(['courseId', 'user'])

const course = ref(null)
const error = ref(null)

onMounted(async () => {
  course.value = await courseApi.courseDetail(props.courseId);
})

</script>

<template>
  <LoadingScreen :items="course" :error="error">
    <template #content>
      <v-table v-for="week in course.weeks" :key="week.id">
        <CourseUserListWeekRow :week="week" />
      </v-table>
    </template>
  </LoadingScreen>
</template>