<script setup>


import { getNameByLessonType} from "@/plugins/constants";

const props = defineProps(['guarant'])

</script>

<template>
  <tr>
    <td>
      <span>
        <v-tooltip activator="parent" location="top">{{ t(getNameByLessonType(lesson.type)) }}</v-tooltip>
      </span>
    </td>
    <td>
      {{ props.guarant.name }}
    </td>
  </tr>
</template>

<style scoped>

</style>