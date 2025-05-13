<script setup>
import { onMounted, ref } from 'vue'
import { VDataTable } from 'vuetify/components'
import { getErrorMessage, anonEmoji } from '@/plugins/constants'
import { useUserStore } from '@/plugins/store'
import * as Nav from '@/service/nav'
import { courseUserApi } from '@/service/api'

const userStore = useUserStore()
const props = defineProps(['courseId', 'userId'])

const courseUsers = ref([])
const error = ref(null)

import { useLocale } from 'vuetify'
const { t } = useLocale()

const tableHeaders = () => [{title: t('$vuetify.course_user_table_name'), sortable: true, key: 'name'}]
const searchText = ref('')

onMounted(async () => {
  courseUserApi.courseUsers(props.courseId)
      .then((result) => {
        courseUsers.value = result.users
      })
      .catch((err) => { error.value = err.code })
})
</script>

<template>
  <v-card class="mx-auto">
    <v-list density="comfortable">
      <v-list-item :to="new Nav.CourseUserList({id: courseId}).routerPath()">
        <template #prepend>
          <v-icon size="small" icon="mdi-arrow-left" />
        </template>
        <template #title>
          <strong>{{ t('$vuetify.course_user_list_left_drawer_users') }}</strong>
        </template>
      </v-list-item>
      <v-list-item class="mb-2">
        <template #title>
          <v-text-field v-model="searchText" :label="t('$vuetify.action_search')" prepend-inner-icon="mdi-magnify"
                        density="compact" variant="outlined" single-line hide-details />
        </template>
      </v-list-item>
      <v-data-table :items-per-page="50" :headers="tableHeaders()" :items="courseUsers"
                    items-per-page-text="" :search="searchText" item-value="id">
        <template #headers="{ }" />
        <template #[`item`]="{ item }">
          <v-list-item :active="`${item.id}` === userId"
                       :to="new Nav.CourseUserDetail({id: courseId}, item).routerPath()">
            <template #title>
              {{ userStore.anonymous ? anonEmoji(item.username) : item.name }}
            </template>
          </v-list-item>
        </template>
      </v-data-table>
      <v-list-item v-if="error" :title="getErrorMessage(t, error)" />
    </v-list>
  </v-card>
</template>

<style>
.v-data-table-footer {
  margin-top: 12px !important;
}
</style>
