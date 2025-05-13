<script setup>
import { ref, inject } from 'vue'
import { useLocale } from 'vuetify'
import { lessonApi } from '@/service/api'

const lesson = inject('lesson')
const module = inject('module')
const props = defineProps(['reload', 'teacher'])
const { t } = useLocale()

const unlockCode = ref('')
const unlockCodeErrors = ref([])

const unlockModules = () => {
  lessonApi.unlockLesson(lesson.value.id, unlockCode.value)
      .then(() => props.reload())
      .catch((err) => {
        console.log(err)
        unlockCode.value = ''
        unlockCodeErrors.value = [t('$vuetify.module_locked_invalid_code')]
      })
}
</script>

<template>
  <v-card class="mb-2 pa-2 overflow-y-auto">
    <v-card-item v-if="teacher" :title="module.name">
      <v-label class="my-2">
        {{ t('$vuetify.module_student_locked') }}
      </v-label>
    </v-card-item>
    <v-card-item v-else-if="module.locked" :title="t('$vuetify.module_title_locked') + ' 🔑'">
      <v-form class="pt-2 d-flex flex-column" @submit.prevent="unlockModules">
        <v-label>{{ t('$vuetify.module_locked_prompt') }}</v-label>
        <v-text-field v-model="unlockCode" class="pt-2" :autofocus="true"
                      :placeholder="t('$vuetify.module_locked_code')"
                      :error-messages="unlockCodeErrors" />
        <v-btn class="mt-2" :block="true" color="blue" variant="tonal" @click="unlockModules">
          {{ t('$vuetify.module_locked_unlock') }}
        </v-btn>
      </v-form>
    </v-card-item>
    <v-card-item v-else :title="module.name">
      <v-label class="my-2">
        {{ t('$vuetify.module_locked_depends', lesson.modules.find((mod) => mod.id === module.depends)?.name) }}
      </v-label>
    </v-card-item>
  </v-card>
</template>
