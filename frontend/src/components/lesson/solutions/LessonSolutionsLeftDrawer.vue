<script setup>
import {inject, onBeforeMount, ref, watch} from 'vue'
import {lessonUserFilter} from '@/plugins/constants'
import { useUserStore } from '@/plugins/store'
import * as Nav from '@/service/nav'
import {lessonModuleApi} from '@/service/api'

import router from '@/router'
import SideBySideToggle from "@/components/custom/SideBySideToggle.vue";
import LessonDetailLeftDrawer from "@/components/lesson/LessonDetailLeftDrawer.vue";
import LessonSolutionsLeftDrawerUsers from "@/components/lesson/solutions/LessonSolutionsLeftDrawerUsers.vue";

const userStore = useUserStore()
const props = defineProps(['lessonId', 'moduleId', 'userId', 'scoringRuleId', 'usersToggled'])

const lessonUsers = inject('lessonUsers')
const listingUsers = inject('listUsers')
const lesson = ref(null)
const module = ref(null)
const user = ref(null)

const error = ref(null)

const reloadLessonUsers = async () => {
  if (props.moduleId) {
    lessonModuleApi.lessonModuleUserList(props.lessonId, props.moduleId)
        .then((lessonModuleUserList) => {
          lessonUsers.value = lessonModuleUserList.users
          lesson.value = {id: props.lessonId, name: lessonModuleUserList.lesson.name}
          module.value = lessonModuleUserList.module
          user.value = lessonModuleUserList.users.find((u) => u.id === +props.userId)
          const usersFiltered = lessonModuleUserList.users.filter((u) => lessonUserFilter(userStore, u))
          if (props.userId === "-1" && usersFiltered.length)
            router.replace(new Nav.LessonSolutionsModule(lesson.value, module.value, usersFiltered[0]).routerPath())
        })
        .catch((err) => {
          error.value = err.code
        })
  }
}

const listUsers = () => {
  if (!props.moduleId) return false;
  listingUsers.value = true;
  router.push(new Nav.LessonSolutionsModule({id: props.lessonId}, {id: props.moduleId ? props.moduleId : -1}, {id: props.userId}).routerPath())
  return true;
}

const listModules = () => {
  listingUsers.value = false;
  router.push(new Nav.LessonSolutionsUser({id: props.lessonId}, {id: props.userId}, {id: props.moduleId}).routerPath())
  return true;
}
const usersReloadHook = inject('usersReloadHook')
watch(usersReloadHook, () => reloadLessonUsers())
watch(() => props.moduleId, reloadLessonUsers)
onBeforeMount(() => reloadLessonUsers())
</script>

<template>
  <SideBySideToggle style="height: 60px; border-bottom: thin grey solid"
                    :text-left="'$vuetify.lesson_solutions_users'" :on-click-left="listUsers"
                    :text-right="'$vuetify.lesson_solutions_modules'" :on-click-right="listModules" :start-selection-left="usersToggled" />
  <!-- :key re-renders the component upon navigating through the sidebar. If we don't do this, the layout crumbles in solution view. Changing the key should not lead to additional API calls. -->
  <LessonSolutionsLeftDrawerUsers v-if="module && usersToggled" :key="$route.fullPath" :selected-user-id="userId" :lesson="lesson" :module="module" />
  <LessonDetailLeftDrawer v-else :lesson-id="lessonId" :module-id="moduleId" :user-id="userId" :scoring-rule-id="scoringRuleId" />
</template>
