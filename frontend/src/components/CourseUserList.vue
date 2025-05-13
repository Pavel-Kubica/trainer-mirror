<script setup>
import { ref, inject, onMounted, provide } from 'vue'
import { useLocale } from 'vuetify'
import { VDataTable } from 'vuetify/components'
import { courseApi, courseUserApi } from '@/service/api'
import { useUserStore } from '@/plugins/store'
import * as Nav from '@/service/nav'
import CourseSecretDialog from '@/components/course/CourseSecretDialog.vue'
import CourseListRemoveDialog from '@/components/course/CourseListRemoveDialog.vue'
import LoadingScreen from '@/components/custom/LoadingScreen.vue'
import CourseUserListTableRow from '@/components/course/CourseUserListTableRow.vue'

const appState = inject('appState')
const props = defineProps(['course'])
const userStore = useUserStore()
const { t } = useLocale()

const deleteCourseUserDialog = ref(false)
const deleteCourseUser = ref(null)
provide('deleteCourseUserDialog', deleteCourseUserDialog)
provide('deleteCourseUser', deleteCourseUser)

const courseSecretDialog = ref(false)
provide('courseSecretDialog', courseSecretDialog)

const courseSecretWasSet = () => {
  courseSecretDialog.value = false
  appState.value.notifications.push({
    type: "success", title: t(`$vuetify.course_secret_changed_title`),
    text: t(`$vuetify.course_secret_changed_text`),
  })
}

const isCourseEditor = () => {
  // TODO: change based on course admin roles
  return ['wrzecond', 'matouj10'].includes(userStore.user.username)
}

const tableHeaders = () => [
  {title: t('$vuetify.course_user_table_name'), sortable: true, key: 'name'},
  {title: t('$vuetify.course_user_table_username'), sortable: true, key: 'username'},
  {title: t('$vuetify.course_user_table_role'), sortable: true, key: 'role'},
  {title: t('$vuetify.course_user_table_progress'), align: 'center', sortable: true, key: 'progress'},
  {title: t('$vuetify.course_user_table_action'), align: 'end', sortable: false}
]

const users = ref([])
const error = ref('')
const searchText = ref('')

const loadCourseUsers = async () => {
  courseUserApi.courseUsers(props.course)
      .then((result) => {
        appState.value.navigation = [new Nav.CourseList(), new Nav.CourseDetail(result.course),
          new Nav.CourseUserList(result.course)]
        users.value = result.users
      })
      .catch((err) => { error.value = err.code })
}
provide('loadCourseUsers', loadCourseUsers)

onMounted(async() => { await loadCourseUsers() })
</script>

<template>
  <CourseListRemoveDialog :course="course" />
  <CourseSecretDialog :title="t('$vuetify.course_secret_change_title')" :text="t('$vuetify.action_change')"
                      :method="(secret) => courseApi.putCourseSecret(props.course, secret)" :action="courseSecretWasSet" />
  <v-card :title="t('$vuetify.course_users')">
    <template #append>
      <div class="d-flex align-center flex-nowrap">
        <v-text-field v-model="searchText" :label="t('$vuetify.action_search')"
                      prepend-inner-icon="mdi-magnify" density="compact" class="me-4"
                      style="min-width: 25vw" variant="outlined" single-line hide-details />
        <v-btn v-if="isCourseEditor() && !error" class="color-anchor me-4"
               :to="new Nav.CourseUserImport(course).routerPath()">
          {{ t('$vuetify.course_import_users') }}
        </v-btn>
        <v-btn class="color-anchor" @click="courseSecretDialog = true">
          {{ t('$vuetify.course_secret_change_title') }}
        </v-btn>
      </div>
    </template>
    <LoadingScreen :items="users" :error="error">
      <template #content>
        <v-data-table :items-per-page="50" :headers="tableHeaders()" :items="users"
                      :search="searchText" item-value="id" class="elevation-1">
          <template #[`item`]="{ item }">
            <CourseUserListTableRow :course="course" :item="item" />
          </template>
        </v-data-table>
      </template>
    </LoadingScreen>
  </v-card>
</template>
