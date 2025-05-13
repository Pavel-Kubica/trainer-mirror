<script setup>
import LoadingScreen from "@/components/custom/LoadingScreen.vue";
import {ref} from "vue";
import {useLocale} from "vuetify";
import GuideListRow from "@/components/guide/GuideListRow.vue";

const error = ref(null)
const { t } = useLocale()

import guides from "@/resources/guides.js"



</script>

<template>
  <v-card :title="t('$vuetify.guides')">
    <LoadingScreen :items="guides" :error="error">
      <template #content>
        <v-table :class="'mt-0'">
          <GuideListRow :guides="guides" />
        </v-table>
      </template>
    </LoadingScreen>
  </v-card>
</template>
