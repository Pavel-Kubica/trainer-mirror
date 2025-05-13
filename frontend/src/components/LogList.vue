<script setup>
import {onMounted, ref, provide} from 'vue'
import {useUserStore} from "@/plugins/store";
import { logApi } from '@/service/api'
import LoadingScreen from '@/components/custom/LoadingScreen.vue'
import LogListRow from "@/components/log/LogListRow.vue";
import LogListHeader from "@/components/log/LogListHeader.vue";

const store = useUserStore()

const logs = ref(null)
const error = ref(null)

onMounted(async () => {
  fetchLogs()
})

function fetchLogs() {
  logApi.getAllLogs()
      .then((result) => {
        logs.value = result
      })
      .catch((err) => {error.value = err.code})
}
provide('fetchLogs', fetchLogs)
</script>

<template>
  <LogListHeader />
  <v-card class="mx-8 my-4">
    <LoadingScreen :items="logs" :error="error">
      <template #table>
        <v-toolbar :color="store.darkMode ? 'grey-darken-4' : 'white'">
          <v-toolbar-title>
            <span style="font-size: 0.8em;">{{ 'Username' }}</span>
          </v-toolbar-title>
          <v-toolbar-title>
            <span style="font-size: 0.8em;">{{ 'IP address' }}</span>
          </v-toolbar-title>
          <v-toolbar-title>
            <span style="font-size: 0.8em;">{{ 'Client' }}</span>
          </v-toolbar-title>
          <v-toolbar-title>
            <span style="font-size: 0.8em;">{{ 'Timestamp' }}</span>
          </v-toolbar-title>
          <v-toolbar-title>
            <span style="font-size: 0.8em;">{{ 'Entity' }}</span>
          </v-toolbar-title>
          <v-toolbar-title>
            <span style="font-size: 0.8em;">{{ 'Entity ID' }}</span>
          </v-toolbar-title>
          <v-toolbar-title>
            <span style="font-size: 0.8em;">{{ 'Operation' }}</span>
          </v-toolbar-title>
        </v-toolbar>
        <LogListRow v-for="log in logs" :key="log.id" :log="log"
                    @semesterDeleted="fetchLogs" />
      </template>
    </LoadingScreen>
  </v-card>
</template>