<script setup>

import * as Nav from "@/service/nav";
import {useLocale} from "vuetify";
import {onMounted, ref} from "vue";
import {VDataTable} from "vuetify/components";
import {getErrorMessage} from "@/plugins/constants";
import {semesterApi} from "@/service/api";

const {t} = useLocale()
const semesterUser = ref([])
const error = ref(null)
defineProps(['semester'])

onMounted(async () => {
  await semesterApi.listSemesters()
      .then((result) => {
        semesterUser.value = result
      })
      .catch((err) => {
        error.value = err.code
      })
})

</script>

<template>
  <v-card class="mx-auto">
    <v-list density="comfortable">
      <v-list-item :to="new Nav.CourseList().routerPath()">
        <template #prepend>
          <v-icon size="small" icon="mdi-arrow-left" />
        </template>
        <template #title>
          <strong>{{ t('$vuetify.subject_list_drawer') }}</strong>
        </template>
      </v-list-item>
      <v-data-table :items-per-page="100" :items="semesterUser">
        <template #headers="{}" />
        <template #[`item`]="{ item }">
          <v-list-item :to="new Nav.SubjectsList(item.id).routerPath()" :active="`${item.id}` === semester">
            <v-icon class="mr-2">
              mdi-calendar-month
            </v-icon>
            {{ item.code }}
          </v-list-item>
        </template>
        <template #bottom />
      </v-data-table>
      <v-list-item v-if="error" :title="getErrorMessage(t, error)" />
    </v-list>
  </v-card>
</template>


<style scoped>
.mr-2 {
  margin-right: 4px;
}
</style>