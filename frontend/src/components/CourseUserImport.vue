<script setup>
import { ref, inject, onMounted } from 'vue'
import { useLocale } from 'vuetify'
import { courseApi, courseUserApi } from '@/service/api'
import router from '@/router'
import * as Nav from '@/service/nav'
import LoadingScreen from '@/components/custom/LoadingScreen.vue'

const appState = inject('appState')
const props = defineProps(['course'])
const { t } = useLocale()

const userList = ref('')
const course = ref([])
const error = ref('')

const importStudents = () => {
  const students = userList.value.split("\n")
      .map((line) => line.trim().split(";"))
      .map((trimmedLine) => ({name: trimmedLine[0], username: trimmedLine[1], role: trimmedLine[2]}))
  courseUserApi.courseUserImport(props.course, students)
      .then(() => {
        appState.value.notifications.push({
          type: "success", title: t(`$vuetify.course_user_imported_title`),
          text: t(`$vuetify.course_user_imported_text`),
        })
        router.push(new Nav.CourseUserList({id: props.course}).routerPath())
      })
      .catch((err) => { error.value = err.code })
}

onMounted(async() => {
  courseApi.courseDetail(props.course)
      .then((result) => {
        appState.value.navigation = [new Nav.CourseList(), new Nav.CourseDetail(result), new Nav.CourseUserImport(result)]
        course.value = result
      })
      .catch((err) => { error.value = err.code })
})
</script>

<template>
  <v-card :title="t('$vuetify.course_user_import_title')">
    <LoadingScreen :items="course" :error="error">
      <template #content>
        <v-label>{{ t('$vuetify.course_user_import_label') }}</v-label>
        <v-form class="pt-2">
          <v-textarea v-model="userList" :placeholder="'Ondřej Wrzecionko;wrzecond;TEACHER\nJan Matoušek;matouj10;TEACHER\nKarel Novák;novak250;STUDENT'" rows="10" />
          <v-btn :block="true" variant="tonal" color="blue" @click="importStudents">
            {{ t('$vuetify.action_import') }}
          </v-btn>
        </v-form>
      </template>
    </LoadingScreen>
  </v-card>
</template>
