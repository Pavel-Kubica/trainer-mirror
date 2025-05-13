<script setup>

import {inject, onMounted} from "vue";

const options = inject('options')
const correctAnswers = inject('correctAnswers')

onMounted(() => {
  options.value = []
  correctAnswers.value = []
})
</script>

<template>
  <v-text-field v-model="correctAnswers[0]" class="mt-8" label="Odpoveď - preferovane krátky plaintext" />
</template>

<style scoped>

</style>