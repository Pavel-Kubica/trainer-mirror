<script setup>
import {ref} from 'vue'
import {useLocale} from 'vuetify'
import DiscussionList from "@/components/modules/discussion/DiscussionList.vue";

const {t} = useLocale()
defineProps(['moduleId'])
const menu = ref(false)

</script>

<template>
  <v-menu v-model="menu" location="bottom" :close-on-content-click="false">
    <template #activator="{ props }">
      <v-btn v-bind="props" class="mb-2 mx-2 pa-2 ml-auto" width="15%" color="blue" variant="tonal">
        {{ t('$vuetify.module_edit_discussion') }}
      </v-btn>
    </template>
    <DiscussionList :module-id="moduleId" />
  </v-menu>
</template>
