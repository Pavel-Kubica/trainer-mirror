<script setup>
import { ref, inject, onMounted, watch } from 'vue'
import { useLocale } from 'vuetify'
import { weekApi } from '@/service/api'
import LocaleAwareDatepicker from '@/components/custom/LocaleAwareDatepicker.vue'
import LoadingScreen from "@/components/custom/LoadingScreen.vue";

const appState = inject('appState')
const createEditDialog = inject('createEditDialog')
const props = defineProps(['courseId', 'course', 'week', 'callback'])
const { t } = useLocale()

const error = ref(null)
const loading = ref(false)
const weekData = ref(null)

const submitAllowed = () => {
  return weekData.value && weekData.value.name && weekData.value.from && weekData.value.until
}
const submit = () => {
  if (!submitAllowed())
    return false
  loading.value = true
  const promise = props.week ? weekApi.editWeek(props.week.id, weekData.value) : weekApi.createWeek(weekData.value)
  promise
      .then(async () => {
        const key = props.week ? 'edit' : 'create'
        appState.value.notifications.push({
          type: "success", title: t(`$vuetify.week_${key}_notification_title`),
          text: t(`$vuetify.week_${key}_notification_text`),
        })
        props.callback()
        loading.value = false
        createEditDialog.value = false
      })
      .catch((err) => {
        error.value = err.code
      })
}

const translate = (key) => t(`$vuetify.week_edit_${key}`)
const addWeek = (datetime) => {
  const moved = new Date((new Date(datetime)).getTime() + 7*24*3600*1000)
  return moved.toISOString()
}
const removeTime = (datetime) => {
  const date = new Date(datetime)
  date.setHours(0, 0, 0, 0)
  return date.toISOString()
}
const loaded = () => {
  const lastWeekFrom = props.course?.weeks?.length ? removeTime(addWeek(props.course.weeks.map((w) => w.from).sort().at(-1))) : null
  const lastWeekUntil = props.course?.weeks?.length ? removeTime(addWeek(props.course.weeks.map((w) => w.until).sort().at(-1))) : null
  if (props.week)
  {
    weekData.value = Object.assign({}, props.week)
  }
  else{
    weekData.value = {
      courseId: props.courseId, name: "", from: lastWeekFrom, until: lastWeekUntil,
    }
  }

  loading.value = false
}

watch(props, loaded)
onMounted(loaded)
</script>

<template>
  <v-dialog v-model="createEditDialog" width="auto">
    <v-card>
      <v-card-title class="d-flex justify-space-between pt-4">
        {{ translate(`dialog_${week ? 'edit' : 'create'}`) }}
        <v-icon icon="mdi-close" size="24" @click="createEditDialog = false" />
      </v-card-title>
      <LoadingScreen :items="!loading" :error="error">
        <template #content>
          <v-form @submit.prevent="submit">
            <v-text-field v-model="weekData.name" :label="translate('name')" autofocus />
            <v-label>{{ translate('from') }}</v-label>
            <LocaleAwareDatepicker v-model="weekData.from" />
            <v-label>{{ translate('until') }}</v-label>
            <LocaleAwareDatepicker v-model="weekData.until" />
            <v-btn class="my-4" variant="tonal" color="info" type="submit"
                   :block="true" :disabled="!submitAllowed()" @click="submit">
              {{ t(`$vuetify.action_${week ? 'edit' : 'create'}`) }}
            </v-btn>
          </v-form>
        </template>
      </LoadingScreen>
    </v-card>
  </v-dialog>
</template>
