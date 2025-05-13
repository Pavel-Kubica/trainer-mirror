<script setup>

import {ref} from "vue";
import {useLocale} from "vuetify";

const {t} = useLocale()

// onClickLeft and onClickRight return whether or not the switch succeeded
const props = defineProps(['textLeft', 'textRight', 'onClickLeft', 'onClickRight', 'startSelectionLeft'])

const leftSelected = ref(props.startSelectionLeft)

const clickLeft = () => {
  if (leftSelected.value) return;
  leftSelected.value = props.onClickLeft();
}

const clickRight = () => {
  if (!leftSelected.value) return;
  leftSelected.value = !props.onClickRight();
}

</script>

<template>
  <div class="d-flex row">
    <div class="font-weight-medium" style="width: 50%; padding: 0; margin: 0; align-content: center; text-align: center"
         :style="leftSelected ? {'background-color': '#00000020'} : {'cursor': 'pointer'}"
         @click="clickLeft">
      {{ t(textLeft) }}
    </div>
    <div class="font-weight-medium" style="width: 50%; padding: 0; margin: 0; align-content: center; text-align: center"
         :style="leftSelected ? {'cursor': 'pointer'} : {'background-color': '#00000020'}"
         @click="clickRight">
      {{ t(textRight) }}
    </div>
  </div>
</template>

<style scoped>

</style>