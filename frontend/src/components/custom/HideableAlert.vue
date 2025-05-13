<script setup>
import { onMounted } from 'vue'

const props = defineProps(['notification', 'onClick', 'onHide', 'timeout'])
onMounted(() => { setTimeout(props.onHide, props.timeout) })
</script>

<template>
  <v-alert class="mt-4 me-8 text-pre-wrap" :type="notification.type" :title="notification.title"
           :text="notification.text" @click="onClick" />
</template>
