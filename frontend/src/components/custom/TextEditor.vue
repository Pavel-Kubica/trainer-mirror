<script setup>
import { onMounted } from 'vue'
import { moduleApi } from '@/service/api'
import axios from 'axios'
import { useUserStore } from '@/plugins/store'
import { MdEditor, MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import '@/assets/iconfont.js'

const props = defineProps(['editable'])
const store = useUserStore()

const onUploadImg = async (images, callback) => {
  const results = await Promise.all(images.map((image) => moduleApi.postImage(image)))
  console.log(results)
  callback(results.map((imagePath) => axios.defaults.baseURL + imagePath))
}

onMounted(() => {
  document.querySelectorAll(".md-editor-preview a").forEach((anchor) => {
    anchor.target = '_blank'
  })
})
</script>

<template>
  <MdEditor v-if="props.editable" language="en-US" preview-theme="github" :theme="store.darkMode ? 'dark' : 'light'"
            :no-iconfont="true" :scroll-auto="false" class="px-4" :toolbars="['code', 'image']" @onUploadImg="onUploadImg" />
  <MdPreview v-else language="en-US" preview-theme="github" :code-foldable="false" :theme="store.darkMode ? 'dark' : 'light'"
             :no-iconfont="true" :show-code-row-number="false" :read-only="true" class="px-4 pb-2" :toolbars="[]" />
</template>

<style>
  p.md-editor-mermaid {
    background: rgb(var(--v-theme-mermaidBg));
    margin-bottom: 16px;
  }
  .md-editor-code-flag {
    display: none;
  }
  .md-editor-code-head{
    justify-content: end !important;
  }
  .md-editor-preview {
    word-break: break-word;
  }
</style>
