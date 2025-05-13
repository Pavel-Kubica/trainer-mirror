<script setup>
import { inject, ref } from 'vue'
import { useLocale } from 'vuetify'
import LoadingScreen from "@/components/custom/LoadingScreen.vue";

const props = defineProps(['method', 'action', 'title', 'text'])
const { t } = useLocale()
const courseSecretDialog = inject('courseSecretDialog')

const dummyItem = ref('item')
const dummyError = ref(null)
const secret = ref('')
const error = ref("")

const performAction = async () => {
  error.value = ""
  dummyItem.value = null
  props.method(secret.value)
      .then((result) => {
        secret.value = ''
        props.action(result)
      })
      .catch((err) => {
        error.value = t([404, 400].includes(err?.response?.status) ? "$vuetify.course_join_invalid_code"
            : "$vuetify.error_network_connection")
      })
      .finally(() => { dummyItem.value = 'item' })
  return false
}
</script>

<template>
  <v-dialog v-model="courseSecretDialog" width="auto">
    <v-card class="dialog-title" :title="title">
      <template #append>
        <v-icon icon="mdi-close" size="24" @click="courseSecretDialog = false" />
      </template>
      <LoadingScreen :items="dummyItem" :error="dummyError">
        <template #content>
          <v-form class="pb-8" @submit.prevent="performAction">
            <v-text-field v-model="secret" :label="t('$vuetify.course_join_code')"
                          :autofocus="true" :error-messages="error" />

            <v-btn class="mt-2" type="button" :block="true" color="green" size="large" variant="tonal" @click="performAction">
              {{ props.text }}
            </v-btn>
          </v-form>
        </template>
      </LoadingScreen>
    </v-card>
  </v-dialog>
</template>

<style scoped>
.dialog-title {
  min-width: 300px
}
</style>
