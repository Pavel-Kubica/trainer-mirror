<script setup>
import * as Nav from '@/service/nav'
import TooltipIconButton from "@/components/custom/TooltipIconButton.vue";
import {useLocale} from 'vuetify'

import {inject, onMounted, provide, ref} from "vue";
import {courseApi, courseUserApi} from "@/service/api";
import CourseDeleteDialog from "@/components/course/CourseDeleteDialog.vue";
import {getErrorMessage} from "@/plugins/constants";
import {useUserStore} from "@/plugins/store";


const appState = inject('appState')
const userStore = useUserStore()
const {t} = useLocale()
const teacherCourse = ref([])
const error = ref(null)
const props = defineProps(['course'])

const emit = defineEmits(['courseDeleted'])
const amountOfWeeks = ref(0)
const numOfUsers = ref(0)
const displayDeleteDialog = ref(false)
provide('displayDeleteDialog', displayDeleteDialog)


onMounted(async () => {
  await courseUserApi.courseTeachers(props.course.id)
      .then((result) => {
        teacherCourse.value = result
        if (userStore.isTeacher(props.course)) {
          teacherCourse.value.push(userStore.user);
        }
      })
      .catch((err) => {
        error.value = err.code
      })
  await courseApi.courseDetail(props.course.id)
      .then((result) => {
        amountOfWeeks.value = result.weeks.length
      })
      .catch((err) => {
        error.value = err.value
      })
  await courseUserApi.courseUsers(props.course.id)
      .then((result) => {
        numOfUsers.value = result.length
      })
      .catch((err) => {
        error.value = err.value
      })
})

const deleteCourse = async () => {
  try {
    const res = ref(null)
    res.value = courseApi.courseDetail(props.course.id);
    if (amountOfWeeks.value > 0) {
      appState.value.notifications.push({
        type: "error", title: t('$vuetify.course_deletion_title'),
        text: t('$vuetify.course_cant_delete_weeks_present')
      })
      displayDeleteDialog.value = false
      return
    }
    if (numOfUsers.value > 0){
      appState.value.notifications.push({
        type: "error", title: t('$vuetify.course_deletion_title'),
        text: t('$vuetify.course_cant_delete_users_present')
      })
      displayDeleteDialog.value = false
      return
    }
    await courseApi.deleteCourse(props.course.id);
    displayDeleteDialog.value = false
    appState.value.notifications.push({
      type: "success", title: t('$vuetify.course_deletion_title'), text: t('$vuetify.course_deletion_success_text')
    })
    emit("courseDeleted");
  } catch (err) {
    appState.value.notifications.push({
      type: "error", title: t('$vuetify.course_deletion_title'),
      text: getErrorMessage(t, err.code)
    })
  }
};
</script>

<template>
  <!--tmp. will be redone-->
  <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0">
  <tr>
    <td width="50px">
      <span class="material-symbols-outlined">group</span>
    </td>
    <td width="200px">
      {{ t('$vuetify.subject_course_short_name') }}: {{ course.shortName }}
    </td>
    <td width="50%">
      <div style="display: flex; justify-content: center;">
        <div v-if="teacherCourse[0]"
             style="justify-content:center; display: flex; align-items: center; background-color: #F6A3E1; width: 120px; border-radius: 10px; color: black;">
          <span class="material-symbols-outlined">person</span>
          <span>{{ teacherCourse[0]?.username }}</span>
        </div>
        <div v-if="teacherCourse[1]"
             style="justify-content:center; display: flex; align-items: center; background-color: #F6A3E1; width: 120px; border-radius: 10px; color: black; margin-left: 3px">
          <span class="material-symbols-outlined">person</span>
          <span>{{ teacherCourse[1]?.username }}</span>
        </div>
        <div v-if="teacherCourse[2]"
             style="justify-content:center; display: flex; align-items: center; background-color: #F6A3E1; width: 30px; border-radius: 10px; color: black; margin-left: 3px">
          <span>+{{ teacherCourse.length - 2 }}</span>
        </div>
      </div>
    </td>
    <td class="d-flex flex-row justify-end">
      <TooltipIconButton icon="mdi-pencil" color="rgb(var(--v-theme-anchor))"
                         :to="new Nav.CourseEdit(course).routerPath()" />
      <TooltipIconButton icon="mdi-delete" color="red" @click="displayDeleteDialog=true" />
      <router-link :to="new Nav.CourseDetail(course).routerPath()" class="d-flex justify-end text-decoration-none">
        <v-btn variant="text" icon="mdi-arrow-right" />
      </router-link>
    </td>
  </tr>

  <CourseDeleteDialog :course="course" @deleteButtonClicked="deleteCourse" />
</template>