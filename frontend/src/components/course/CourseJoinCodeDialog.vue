<script setup>

import {courseApi} from "@/service/api";
import {inject, onMounted, ref, watch} from "vue";
import {useLocale} from "vuetify";
import LoadingScreen from "@/components/custom/LoadingScreen.vue";

const { t } = useLocale()

const props = defineProps(['courseId']);

const joinCodeDialog = inject('joinCodeDialog')
const appState = inject('appState')

const currentCode = ref(null)
const newCode = ref(null)
const error = ref(null)

const changeCode = () => {
  courseApi.putCourseSecret(props.courseId, newCode.value)
      .then(() => {
        currentCode.value = newCode.value;
        success();
      })
      .catch((err) => {
        error.value = t([404, 400].includes(err?.response?.status) ? "$vuetify.course_join_invalid_code"
            : "$vuetify.error_network_connection")
      })
}

const success = () => {
  joinCodeDialog.value = false
  appState.value.notifications.push({
    type: "success", title: t(`$vuetify.course_secret_changed_title`),
    text: t(`$vuetify.course_secret_changed_text`),
  })
}

const reload = () => {
  courseApi.courseDetail(props.courseId).then((result) => {
    currentCode.value = result.secret;
  })
  error.value = null;
}

watch(props, reload);
onMounted(reload);

</script>

<template>
  <v-dialog v-model="joinCodeDialog" width="15%">
    <v-card>
      <v-card-title class="d-flex justify-end pt-4">
        <v-icon icon="mdi-close" size="24" @click="joinCodeDialog = false" />
      </v-card-title>
      <LoadingScreen :items="courseId" :error="error">
        <template #content>
          <h3 class="mb-6">
            {{ currentCode ? `${t("$vuetify.course_current_code")}: ${currentCode}` : t('$vuetify.course_no_code_set') }}
          </h3>
          <v-form class="pb-8" @submit.prevent="changeCode">
            <v-text-field v-model="newCode" :label="t('$vuetify.course_new_join_code')"
                          :placeholder="currentCode" :autofocus="true" :error-messages="error" />

            <v-btn class="mt-2" type="button" :block="true" color="green" size="large" variant="tonal" @click="changeCode">
              {{ t('$vuetify.course_save_join_code') }}
            </v-btn>
          </v-form>
        </template>
      </LoadingScreen>
    </v-card>
  </v-dialog>
</template>

<style scoped>

</style>