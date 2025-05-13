<script setup>
import {ref} from 'vue';
import {useUserStore} from "@/plugins/store";
const props = defineProps(['log'])

const error = ref(null)

const store = useUserStore()

</script>

<template>
  <v-toolbar :color="store.darkMode ? 'grey-darken-4' : 'white'">
    <v-toolbar-title>
      <span style="font-size: 0.8em;">{{ props.log.username }}</span>
    </v-toolbar-title>
    <v-toolbar-title>
      <span style="font-size: 0.8em;">{{ props.log.ipAddress }}</span>
    </v-toolbar-title>
    <v-toolbar-title>
      <span style="font-size: 0.8em;">{{ props.log.client }}</span>
    </v-toolbar-title>
    <v-toolbar-title>
      <span style="font-size: 0.8em;">{{ props.log.timestamp }}</span>
    </v-toolbar-title>
    <v-toolbar-title>
      <span style="font-size: 0.8em;">{{ props.log.entity }}</span>
    </v-toolbar-title>
    <v-toolbar-title>
      <span style="font-size: 0.8em;">{{ props.log.entityId }}</span>
    </v-toolbar-title>
    <v-toolbar-title>
      <span style="font-size: 0.8em;">{{ props.log.operation }}</span>
    </v-toolbar-title>
  </v-toolbar>
  <h1>{{ error }}</h1>
</template>



