<script setup>
import {ref, inject, onMounted, provide, watch} from 'vue'
import {useLocale} from 'vuetify'
import {onBeforeRouteLeave} from 'vue-router'
import router from '@/router'
import {courseApi, courseUserApi, semesterApi, subjectApi, userApi} from '@/service/api'
import * as Nav from '@/service/nav'


import LoadingScreen from '@/components/custom/LoadingScreen.vue'

import UnsavedChangesDialog from '@/components/lesson/UnsavedChangesDialog.vue'
import CourseCreateEditInformation from "@/components/course/CourseCreateEditInformation.vue";
import {getErrorMessage} from "@/plugins/constants";
import {useUserStore} from "@/plugins/store";

const appState = inject('appState')
const props = defineProps(['courseId', 'semesterId', 'subjectId'])
const {t} = useLocale()


const error = ref(null)

const semester = ref(null)
const course = ref(null)
const subject = ref(null)
const teachers = ref([])
const alreadySelectedTeachers = ref([])
const courseTeachers = ref([])
const userStore = useUserStore()
provide('courseTeachers', courseTeachers)
const courseData = ref(null)
provide('courseData', courseData)

const routerNext = ref(() => {
})
const unsavedChangesDialog = ref(false)
provide('unsavedChangesDialog', unsavedChangesDialog)

const reload = async () => {
  courseApi.courseDetail(course.value.id)
      .then((result) => {
        course.value = courseData.value = result
        semester.value = result.semester.value
        subject.value = result.subject.value
        alreadySelectedTeachers.value = courseTeachers.value
      })
      .catch((err) => {
        error.value = err.code
      })
}

const getUsers = async (name) => {
  teachers.value = !name ? await userApi.getAllUsers() :
    teachers.value = await userApi.getAllUsers(name)
}

const submitAllowed = () => {
  return courseData.value.name && (course.value || courseData.value)
}
const submit = () => {
  if (!submitAllowed())
    return false
  const editing = course.value !== null
  const courseDataVal = courseData.value;
  if (courseDataVal.users)
    courseDataVal.users = courseDataVal.users.map(user => user.id);
  const promise = editing ? courseApi.editCourse(props.courseId, courseDataVal) : courseApi.createCourse(courseDataVal)
  promise
      .then(async (result) => {
        const key = editing ? 'edit' : 'create'
        appState.value.notifications.push({
          type: "success", title: t(`$vuetify.course_${key}_notification_title`),
          text: t(`$vuetify.course_${key}_notification_text`),
        })
        courseApi.listCourses().then((result) => {
          userStore.realUser.isTeacher = result.filter((course) => course.role === "TEACHER").map((it) => it.id)
        })
        if (userStore.realUser.username === userStore.user.username) {
          userStore.user = userStore.realUser
        }
        if (course.value) await reload()
        else {
          await router.push(new Nav.CourseEdit(result).routerPath())
        }

      })
      .catch((err) => {
        const key = editing ? 'edit' : 'create'
        error.value = err.code
        appState.value.notifications.push({
          type: "error", title: t(`$vuetify.course_${key}_notification_title`),
          text: getErrorMessage(t, err)
        })
      })
}


const loadCourseDetail = async () => {
  if (props.courseId) {
    courseApi.courseDetail(props.courseId)
        .then(async (result) => {
          teachers.value = await userApi.getAllUsers()
          appState.value.navigation = [new Nav.CourseEdit(result)]
          course.value = result
          semester.value = result.semester
          subject.value = result.subject
          courseData.value = Object.assign({}, result)
        })
        .catch((err) => {
          error.value = err.code
          appState.value.notifications.push({
            type: "error", title: t('$vuetify.course_create_notification_title'),
            text: getErrorMessage(t, err.code)
          })
        })
    alreadySelectedTeachers.value = await courseUserApi.courseTeachers(props.courseId)
    courseTeachers.value = alreadySelectedTeachers.value
    if (userStore.isTeacher({id: Number(props.courseId)})) {
      courseTeachers.value.push(userStore.user)
    }
  } else {
    subjectApi.subjectDetail(props.subjectId)
        .then(async (subjectResult) => {
          subject.value = subjectResult
          semesterApi.semesterDetail(props.semesterId)
              .then(async (semesterResult) => {
                semester.value = semesterResult
                appState.value.navigation = [new Nav.CourseList(), new Nav.CourseCreate(subject.value, semester.value.id)]
                course.value = null
                await getUsers("")
                courseData.value = {
                  name: "",
                  shortName: "",
                  semester: semester.value.id,
                  public: false,
                  //secret: null,
                  subject: subject.value.id,
                }

              })
              .catch((err) => {
                error.value = err.code
              })

        })
  }
  onBeforeRouteLeave((to, from, next) => {
    if (JSON.stringify(course.value) === JSON.stringify(courseData.value) || to.name === 'course-edit') { // ok
      next()
      return
    }
    appState.value.leftDrawer = true
    routerNext.value = next
    unsavedChangesDialog.value = true
  })


}


onMounted(async () => {
  await loadCourseDetail()
})

watch(props, async () => {
  await loadCourseDetail()
})
</script>

<template>
  <UnsavedChangesDialog :router-callback="routerNext" :save-callback="submit" />
  <v-card :title="t('$vuetify.course_create_edit_title')">
    <LoadingScreen :items="courseData" :error="error">
      <template #items>
        <v-card-item>
          <v-form @submit.prevent="submit">
            <CourseCreateEditInformation :teachers="teachers" :reload="getUsers" />
            <v-btn class="mb-2 mx-2" type="submit" color="blue" size="large" variant="tonal"
                   :block="true" :disabled="!submitAllowed()">
              {{ t(`$vuetify.action_${course ? 'edit' : 'create'}`) }}
            </v-btn>
          </v-form>
        </v-card-item>
      </template>
    </LoadingScreen>
  </v-card>
</template>
