<script setup>

import TextEditor from "@/components/custom/TextEditor.vue";

import {inject, onMounted, ref} from "vue";

const options = inject('options')
const correctAnswers = inject('correctAnswers')
const numberOfOptions = ref(2)
const optionOrder = inject('optionOrder')


onMounted(() => {
  options.value = []
  correctAnswers.value = []
  optionOrder.value = []
})

</script>

<template>
  <v-row v-for="index in numberOfOptions"
         :key="index"
         class="mt-15 ml-1 mb-8 align-center">
    <TextEditor v-model="options[index - 1]" placeholder="Možnosť" style="height: 200px; width: 80%" editable="true" />
    <v-spacer />
    <v-text-field v-model="optionOrder[index - 1]" style="width: 5%" class="mt-8 mr-8" :placeholder="index.toString()" type="number" label="Poradie" />
  </v-row>
  <v-btn color="green" variant="outlined" @click="numberOfOptions++">
    Pridat moznost
  </v-btn>
</template>

<style scoped>

</style>