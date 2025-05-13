
<script setup>

const props = defineProps(['value','maxStars'])



</script>

<template>
  <div class="star-rating">
    <div
      v-for="i in props.maxStars"
      :key="i"
      :class="['star', i <= props.value ? 'filled' : '']"
    >
      ★
    </div>
  </div>
</template>


<style scoped>
.star-rating {
  display: inline-block;
}

.star {
  display: inline-block;
  font-size: 28px;
  cursor: pointer;
  margin: 2px;
  color: rgb(222, 222, 222);
}

.filled {
  color: #08043c;
}
</style>