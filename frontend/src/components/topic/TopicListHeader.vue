<script setup>
import {useUserStore} from "@/plugins/store";
import {useLocale} from 'vuetify'
import {inject} from "vue";

const editDialog = inject('editDialog')
const editTopic = inject('editTopicEntity')

const store = useUserStore()
const {t} = useLocale()
</script>
<template>
  <v-card class="mx-8 my-4">
    <v-toolbar :color="store.darkMode ? 'grey-darken-4' : 'white'">
      <v-toolbar-title class="font-weight-bold">
        {{ t('$vuetify.topic_list_title') }}
      </v-toolbar-title>
      <v-btn v-if="store.isAdmin || store.isGuarantor" color="light-green-darken-1"
             @click="(e) => { e.stopPropagation(); editTopic = null; editDialog = true }">
        {{ t('$vuetify.topic_list_create') }}
      </v-btn>
    </v-toolbar>
  </v-card>
</template>
