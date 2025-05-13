<script setup>

import TextEditor from "@/components/custom/TextEditor.vue";

import {inject, onMounted, ref} from "vue";


const statements = inject('statements')
const correctAnswers = inject('correctAnswers')
const numberOfOptions = ref(2)

onMounted(() => {
  statements.value = []
  correctAnswers.value = []
})
</script>

<template>
  <v-row v-for="index in numberOfOptions"
         :key="index"
         class="mt-15 ml-1 mb-8 align-center">
    <TextEditor v-model="correctAnswers[index - 1]" placeholder="Možnosť" style="height: 200px; width: 45%" editable="true" />
    <v-spacer />
    <TextEditor v-model="statements[index - 1]" placeholder="Výrok" style="height: 200px; width: 45%" class="mr-4" editable="true" />
  </v-row>
  <v-btn color="green" variant="outlined" @click="numberOfOptions++">
    Pridat moznost
  </v-btn>
</template>

<style scoped>

</style>