<script setup>

import {inject, onMounted} from "vue";
const { t } = useLocale()

const props = defineProps(['correctAnswers', 'options'])

const options = inject('options')
const correctAnswers = inject('correctAnswers')

import TextEditor from "@/components/custom/TextEditor.vue";
import {useLocale} from "vuetify";

const removeOption = async (index) => {

  if(options.value.length <= 2)
    return

  let corrIndex = correctAnswers.value.indexOf(options.value[index])
  if (corrIndex !== -1)
    correctAnswers.value.splice(corrIndex, 1)

  options.value.splice(index, 1)
}

onMounted(() => {
  options.value = ['','']
  correctAnswers.value = []


  if(props.correctAnswers){
    correctAnswers.value = props.correctAnswers
    options.value = props.options
  }

})
</script>

<template>
  <v-row v-for="(option, index) in options"
         :key="index"
         class="mt-15 ml-1 mb-8 align-center">
    <TextEditor v-model="options[index]" class=" max-height-tab mb-4 assignment-editor" :placeholder="t('$vuetify.quiz_module.option')" style="width: 80%" :editable="true" />
    <v-checkbox v-model="correctAnswers" :disabled="options[index] === '' || !options[index]" class="ml-5" :label="t('$vuetify.quiz_module.correct_option')" :value="option" />
    <v-btn color="red" variant="text" icon="mdi-delete" @click="removeOption(index)">
      <v-icon>mdi-delete</v-icon>
      <v-tooltip
        activator="parent"
        location="bottom"
      >
        {{ t('$vuetify.action_delete') }}
      </v-tooltip>
    </v-btn>
  </v-row>
  <v-btn class="ml-1" color="green" variant="outlined" @click="options.push('')">
    {{ t('$vuetify.quiz_module.add_option') }}
  </v-btn>
</template>

<style scoped>

</style>