<script setup>
import * as Nav from '@/service/nav'
import {onMounted, ref} from "vue";
import {lessonModuleApi} from "@/service/api";
import {useLocale} from "vuetify";
import {useUserStore} from "@/plugins/store";

const {current, t} = useLocale()

const userStore = useUserStore()
const props = defineProps(['lesson'])

const moduleWithActiveQuizroom = ref(null)

onMounted(() => {
  lessonModuleApi.lessonModuleWithActiveQuizroom(props.lesson.id).then((module) => {
    if (module)
      moduleWithActiveQuizroom.value = module
  })
})
</script>

<template>
  <tr>
    <td style="width: 20%">
      {{ lesson.name }}
    </td>
    <td style="width: 20%">
      {{ lesson.week.course.name }}
    </td>
    <td>
      <strong class="text-center">
        {{ new Date(props.lesson.timeStart).toLocaleDateString(current === 'customCs' ? 'cs' : 'en') }}
        –
        {{ new Date(props.lesson.timeLimit).toLocaleDateString(current === 'customCs' ? 'cs' : 'en') }}
      </strong>
    </td>
    <td v-if="userStore.isLoggedIn && !userStore.isAnyTeacher &&
          moduleWithActiveQuizroom && JSON.stringify(moduleWithActiveQuizroom) !== '{}'"
        style="width: 25%; text-align: center">
      <router-link :to="new Nav.ModuleDetail(lesson, moduleWithActiveQuizroom).routerPath()">
        <v-btn variant="outlined" :color="userStore.darkMode ? 'red-lighten-2' : 'red-darken-2'">
          {{ t('$vuetify.active_quiz') }}
          <template #append>
            <v-icon icon="mdi-arrow-right" />
          </template>
        </v-btn>
      </router-link>
    </td>
    <td class="d-flex justify-end">
      <router-link class="text-decoration-none" :to="new Nav.LessonDetail(lesson).routerPath()">
        <v-btn variant="text" icon="mdi-arrow-right" />
      </router-link>
    </td>
  </tr>
</template>
