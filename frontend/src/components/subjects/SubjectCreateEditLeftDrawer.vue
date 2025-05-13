<script setup>
import { onMounted, ref } from 'vue'
import { getErrorMessage } from '@/plugins/constants'
import * as Nav from '@/service/nav'
import {semesterApi} from '@/service/api'

const props = defineProps(['semesterId'])
const subjects = ref([])
const error = ref(null)

import { useLocale } from 'vuetify'
const { t } = useLocale()

onMounted(async () => {
  console.log("onMounted drawer");
  semesterApi.listSubjectsBySemester(props.semesterId)
      .then((result) => { subjects.value = result })
      .catch((err) => { error.value = err.code })
})
</script>

<template>
  <v-card class="mx-auto">
    <v-list density="comfortable">
      <v-list-item :to="new Nav.SubjectsList(props.semesterId).routerPath()">
        <template #prepend>
          <v-icon size="small" icon="mdi-arrow-left" />
        </template>
        <template #title>
          <strong>{{ t('$vuetify.subject_detail_left_drawer_subject_list') }}</strong>
        </template>
      </v-list-item>
      <v-list-item
        v-for="subject in subjects" :key="subject.id" :active="`${subject.id}` === subjectId"
        :title="`${subject.code} (${subject.name})`"
        :to="new Nav.SubjectEdit(subject).routerPath()" />
      <v-list-item v-if="error" :title="getErrorMessage(t, error)" />
    </v-list>
  </v-card>
</template>
