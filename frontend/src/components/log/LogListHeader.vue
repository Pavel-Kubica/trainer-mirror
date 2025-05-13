<script setup>
import {useUserStore} from "@/plugins/store";
import LogDeleteDialog from "@/components/log/LogDeleteDialog.vue";
import {useLocale} from "vuetify";
const { t } = useLocale()

const store = useUserStore()
</script>

<template>
  <v-card class="mx-8 my-4">
    <v-toolbar :color="store.darkMode ? 'grey-darken-4' : 'white'">
      <v-toolbar-title class="font-weight-bold">
        {{ t('$vuetify.log_list') }}
      </v-toolbar-title>
      <LogDeleteDialog />
    </v-toolbar>
  </v-card>
</template>