<script setup>


import StarRatingStatic from "@/components/custom/StarRatingStatic.vue";

const props = defineProps(['rating'])
</script>

<template>
  <tr>
    <td>
      <StarRatingStatic :max-stars="5" :value="props.rating.points" />
    </td>
    <td>{{ props.rating.text }}</td>
    <td>
      <span>
        {{ new Date(rating.published).toLocaleDateString() }}
      </span>
    </td>
  </tr>
</template>
