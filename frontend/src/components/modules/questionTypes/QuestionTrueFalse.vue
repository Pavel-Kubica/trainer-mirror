<script setup>

import {inject, onMounted} from "vue";
import {useLocale} from "vuetify";
const { t } = useLocale()

const props = defineProps(['correctAnswers'])

const correctAnswers = inject('correctAnswers')
const options = inject('options')

onMounted( () => {
  options.value = ['True', 'False']
  if(props.correctAnswers){
    correctAnswers.value[0] = props.correctAnswers[0]
  }
  else{
    correctAnswers.value = []
  }

})
</script>

<template>
  <v-card-item>
    <v-row class="mt-5 ml-2 mb-4 align-center">
      <v-card color="green" style="width: 10%" class="text-center justify-center">
        <v-card-title class="ma-8">
          True
        </v-card-title>
      </v-card>
      <v-checkbox v-model="correctAnswers[0]" class="ml-5" :label="t('$vuetify.quiz_module.correct_option')" value="True" />
    </v-row>

    <v-row class="mt-5 ml-2 mb-4 align-center">
      <v-card color="red" style="width: 10%" class="text-center justify-center">
        <v-card-title class="ma-8">
          False
        </v-card-title>
      </v-card>
      <v-checkbox v-model="correctAnswers[0]" class="ml-5" :label="t('$vuetify.quiz_module.correct_option')" value="False" />
    </v-row>
  </v-card-item>
</template>

<style scoped>

</style>