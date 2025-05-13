<script setup>
import { lessonApi, weekApi } from '@/service/api'
import {inject, onMounted, ref, watch} from "vue";
import {useLocale} from "vuetify";
import * as Nav from "@/service/nav";
import TooltipIconButton from "@/components/custom/TooltipIconButton.vue";
import {
  courseLongName,
  courseShortName,
  diacriticInsensitiveStringIncludes,
  getIconByLessonType, LESSON_TYPES
} from "@/plugins/constants";
import LoadingScreen from "@/components/custom/LoadingScreen.vue";

const appState = inject('appState')
const props = defineProps(['targetWeekId', 'targetCourseId', 'callback'])

const copyDialog = inject('copyDialog')

const {current, t} = useLocale()
const translate = (key) => t(`$vuetify.course_detail_${props.targetWeekId ? 'lesson' : 'week'}_copy_${key}`)

const availableCourses = ref(null)
const copyableItems = ref(null)

const copyWeek = (weekId) => {
  weekApi.cloneWeek(weekId, props.targetCourseId).then(() => {
    appState.value.notifications.push({
      type: "success", title: translate('success_title'), text: translate(`success_text`),
    })
    copyDialog.value = false;
    props.callback();
  })
}

const copyLesson = (lessonId) => {
  lessonApi.cloneLessonToWeek(lessonId, props.targetWeekId).then((clonedLesson) => {
    appState.value.notifications.push({
      type: "success", title: translate('success_title'), text: translate(`success_text`),
      route: new Nav.LessonDetail(clonedLesson).routerPath()});
    copyDialog.value = false;
    props.callback();
  })
}

const search = ref({
  name: "",
  courses: [],
  lessonTypes: [],
})

const searchFilter = (item) => {
  return (!search.value.name.length || diacriticInsensitiveStringIncludes(item.name.toLowerCase(), search.value.name.toLowerCase())) &&
      (!search.value.courses.length || search.value.courses.includes(item.courseId)) &&
      (!props.targetWeekId || !search.value.lessonTypes.length || search.value.lessonTypes.includes(item.type))
}

const resetDialog = async () => {
  search.value.name = ""
  search.value.courses = []
  search.value.lessonTypes = []

  copyableItems.value = null
  availableCourses.value = null

  weekApi.listTaught().then((availableWeeks) => {
    availableCourses.value = []
    copyableItems.value = []
    // Api returns weeks sorted descending by start date, which is what we want
    availableWeeks.forEach((week) => {
      if (!availableCourses.value.some((course) => course.id === week.course.id))
        availableCourses.value.push({...week.course, selectTitle: courseLongName(week.course)})

      if (props.targetWeekId) { // Copying a lesson to targetWeek
        week.lessons.forEach((lesson) =>
        {
          copyableItems.value.push({...lesson, courseId: week.course.id, week: week});
        })
      }
      else { // Copying a week to targetCourse
        copyableItems.value.push({...week, courseId: week.course.id})
      }
    })
  })
}

onMounted(async () => {
  await resetDialog();
})
watch(props, resetDialog)

// Workaround, no way to properly bind the checkbox in the custom item to the proper selected state
const falseRef = ref(false)
const trueRef = ref(true)

</script>

<template>
  <v-dialog v-model="copyDialog" width="80%" height="100%">
    <v-card :title="translate('title')">
      <LoadingScreen :items="copyableItems && availableCourses">
        <template #content>
          <div class="d-flex justify-space-around ga-4">
            <v-select v-if="targetWeekId" v-model="search.lessonTypes" style="flex: 0 0 15%"
                      :label="t('$vuetify.course_detail_copy_search_lesson_types')"
                      :items="[...new Set(copyableItems.map((lesson) => lesson.type))]"
                      :item-title="() => ''"
                      :item-value="(item) => item"
                      :item-props="(item) => {return {selected: search.lessonTypes.includes(item)}}"
                      multiple>
              <template #item="{ props, item }">
                <v-list-item v-bind="props">
                  <template #prepend>
                    <v-checkbox-btn v-if="props.selected" v-model="trueRef" readonly :ripple="false" />
                    <v-checkbox-btn v-else v-model="falseRef" readonly :ripple="false" />
                  </template>
                  <v-list-item-title class="d-flex justify-space-between ga-2">
                    {{ t(LESSON_TYPES[item.raw]) }}
                    <v-icon :icon="getIconByLessonType(item.raw)" />
                  </v-list-item-title>
                </v-list-item>
              </template>
              <template #selection="{ item }">
                <v-icon :icon="getIconByLessonType(item.raw)" />
              </template>
            </v-select>
            <v-text-field v-model="search.name" :style="targetWeekId ? {'flex': '0 0 40%'} : {'flex': '0 0 47%'}"
                          :label="t('$vuetify.course_detail_copy_search_name')" />
            <v-select v-model="search.courses" :style="targetWeekId ? {'flex': '0 0 40%'} : {'flex': '0 0 47%'}"
                      :label="t('$vuetify.course_detail_copy_search_courses')"
                      :items="availableCourses.sort()" item-title="selectTitle" item-value="id"
                      :no-data-text="t('$vuetify.course_detail_copy_no_courses')"
                      multiple>
              <template #selection="{ item }">
                <v-chip size="small">
                  <span>{{ courseShortName(item.raw) }}</span>
                </v-chip>
              </template>
            </v-select>
          </div>
          <template v-for="item in copyableItems.filter(searchFilter)" :key="item.id">
            <div style="display: grid; grid-template-columns: 1fr 1fr 1fr 1fr; text-align: left; align-items: center; padding: 8px 24px; border-bottom: thin #00000020 solid">
              <!-- Copying lesson -->
              <template v-if="targetWeekId">
                <span v-if="targetWeekId" style="grid-column: 1">
                  <v-icon class="mr-2" size="18" :icon="getIconByLessonType(item.type)" />
                  <strong>{{ `${item.name}` }}</strong>
                </span>
                <span style="grid-column: 2">
                  {{ `[${item.week.name}, ${courseShortName(item.week.course)}]` }}
                </span>
              </template>
              <!-- Copying week -->
              <template v-else>
                <strong style="grid-column: 1">{{ `${item.name}` }}</strong>
                <span style="grid-column: 2">{{ `[${courseShortName(item.course)}]` }}</span>
                <span style="grid-column: 3">
                  {{ new Date(item.from).toLocaleDateString(current === 'customCs' ? 'cs' : 'en') }}
                  –
                  {{ new Date(item.until).toLocaleDateString(current === 'customCs' ? 'cs' : 'en') }}
                </span>
              </template>

              <TooltipIconButton icon="mdi-content-copy" style="grid-column: 4; justify-self: end"
                                 color="rgb(var(--v-theme-anchor))" :tooltip="translate('title')"
                                 @click="targetWeekId ? copyLesson(item.id) : copyWeek(item.id)" />
            </div>
          </template>
          <span v-if="copyableItems.filter(searchFilter).length === 0" style="padding: 0 24px 16px 24px">
            {{ translate('no_matches') }}
          </span>
        </template>
      </LoadingScreen>
    </v-card>
    <!-- height 100% fixes the dialog to the top, but creates an invisible area below that otherwise wouldn't dismiss it -->
    <div style="flex: 1" @click="() => copyDialog = false" />
  </v-dialog>
</template>