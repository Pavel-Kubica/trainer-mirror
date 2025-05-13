<script setup>
import { inject } from 'vue'
import LoadingScreen from '@/components/custom/LoadingScreen.vue'

defineProps(['maxHeight'])
const fileDownloadedUrl = inject('fileDownloadedUrl')
const error = inject('error')
</script>

<template>
  <v-card class="mb-2 pa-2">
    <LoadingScreen :items="fileDownloadedUrl" :error="error">
      <template #content>
        <img id="preview"
             :class="`mb-2 img-preview ${maxHeight ? 'max-height-tab' : ''}`" alt="Preview" :src="fileDownloadedUrl">
      </template>
    </LoadingScreen>
  </v-card>
</template>

<style scoped>
.img-preview {
  position: relative;
  left: 5%;
  right: 5%;
  top: 8px;
  bottom: 8px;
  width: 400px;
  object-fit: contain;
}

@media only screen and (min-width: 1280px) {
  .img-preview {
    width: 90%;
  }
}
</style>
