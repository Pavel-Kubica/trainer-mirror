<script setup>
import {ref, onMounted, provide, toRefs, inject} from 'vue'
import { useLocale } from 'vuetify'
import {subjectApi, topicApi} from '@/service/api'
import quizApi from '@/service/quizApi'
import LoadingScreen from '@/components/custom/LoadingScreen.vue'
import QuestionListDialogRow from "@/components/modules/quiz/QuestionListDialogRow.vue";

const props = defineProps(['quizId', 'dismiss', 'state', 'reload', 'addQuestion', 'deleteQuestion'])
const { t } = useLocale()
const { searchId, searchText, searchAuthors, searchTypes, searchTopics, searchSubjects } = toRefs(props.state)

const questions = ref(null)
const lesson = ref(null)
const quiz = ref(null)
const error = ref(null)
const topics = ref(null)
const subjects = ref(null)
const quizQuestions = inject('quizQuestions')
provide('questions', questions)
provide('lesson', lesson)
//provide('quiz',quiz)
provide('error', error)
provide('topics', topics)
provide('subjects', subjects)
provide('quizQuestions', quizQuestions)

const customSearch = (question) => (!searchId.value || `${question.id}`.includes(searchId.value))
    && (!searchText.value || question.questionData.toLowerCase().includes(searchText.value.toLowerCase()))
    && (!searchAuthors.value.length || searchAuthors.value.includes(question.author))
    && (!searchTypes.value.length || searchTypes.value.includes(question.questionType))
    && (!searchTopics.value.length || question.topics.map((t) => t.id).some((id) => searchTopics.value.includes(id)))
    && (!searchSubjects.value.length || question.subjects.map((s) => s.id).some((id) => searchSubjects.value.includes(id)))

const topicChipColor = (topic) => {
  if (searchTopics.value.includes(topic.id) || searchTopics.value.length === 0) return 'purple'
  return 'grey'
}

const subjectChipColor = (subject) => {
  if (searchSubjects.value.includes(subject.id) || searchSubjects.value.length === 0) return 'indigo'
  return 'grey'
}

const clearSearch = () => {
  searchId.value = ''
  searchText.value = ''
  searchAuthors.value = [searchAuthors.value[0]]
  searchTypes.value = []
  searchTopics.value = []
  searchSubjects.value = []
}

const reloadDialog = () => {
  Promise.all([
    props.quizId ? quizApi.quizDetail(props.quizId) : null,
    quizApi.listQuestions(),
    topicApi.listTopics(),
    subjectApi.listSubjects()
  ])
      .then((result) => {
        let [qz, que, top, sub] = result
        quiz.value = qz
        questions.value = que
        topics.value = top.sort((a, b) => a.name.localeCompare(b.name))
        subjects.value = sub.sort((a, b) => a.code.localeCompare(b.code))
      })
      .catch((err) => {
        error.value = err.code
      })
}
onMounted(async () => {
  reloadDialog()
})
</script>

<template>
  <v-card :title="t('$vuetify.quiz_module.question_list_add_title')">
    <LoadingScreen :items="questions" :error="error">
      <template #content>
        <div class="d-flex mt-4" style="gap: 16px">
          <v-text-field v-model="searchId" class="flex-grow-0" type="number"
                        :label="t('$vuetify.quiz_module.question_list_header_id')" />
          <v-text-field v-model="searchText" :label="t('$vuetify.quiz_module.question_list_header_text')" />
          <v-select v-model="searchAuthors[0]" :label="t('$vuetify.quiz_module.question_list_header_author')"
                    :items="[...new Set(questions.map((que) => que.author))].sort()" />
          <v-select v-model="searchTypes" :label="t('$vuetify.quiz_module.question_list_header_type')"
                    :items="['','TRUEFALSE', 'MULTICHOICE']" />
          <v-autocomplete v-model="searchTopics" :items="topics" item-title="name" item-value="id"
                          :label="t('$vuetify.quiz_module.question_list_header_topics')" multiple
                          :no-data-text="t('$vuetify.quiz_module.question_list_topics_empty')">
            <template #selection="{ index, item }">
              <v-chip v-if="searchTopics.length && searchTopics.length <= 2" size="small">
                <span>{{ item.title }}</span>
              </v-chip>
              <span v-else-if="searchTopics.length && index === 0" class="text-caption align-self-center">
                {{ t('$vuetify.quiz_module.question_list_header_topics_selected', searchTopics.length) }}
              </span>
            </template>
          </v-autocomplete>
          <v-autocomplete v-model="searchSubjects" :items="subjects" item-title="code" item-value="id"
                          :label="t('$vuetify.quiz_module.question_list_header_subjects')" multiple
                          :no-data-text="t('$vuetify.quiz_module.question_list_subjects_empty')">
            <template #selection="{ index, item }">
              <v-chip v-if="searchSubjects.length && searchSubjects.length <= 2" size="small">
                <span>{{ item.title }}</span>
              </v-chip>
              <span v-else-if="searchSubjects.length && index === 0" class="text-caption align-self-center">
                {{ t('$vuetify.module_list_header_subjects_selected', searchSubjects.length) }}
              </span>
            </template>
          </v-autocomplete>
        </div>
        <v-table v-if="questions.length" class="mb-4">
          <thead>
            <tr>
              <th v-for="title in ['id', 'text', 'author', 'type', 'topics', 'subjects', 'action']" :key="title"
                  :class="title === 'action' ? 'text-end' : ''">
                {{ t(`$vuetify.quiz_module.question_list_header_${title}`) }}
              </th>
            </tr>
          </thead>
          <tbody>
            <QuestionListDialogRow :custom-search="customSearch" :dismiss="dismiss" :reload-dialog="reloadDialog"
                                   :reload-source="reload"
                                   :topic-chip-color="topicChipColor" :subject-chip-color="subjectChipColor"
                                   :add-question="addQuestion" :delete-question="deleteQuestion" />
            <tr v-if="!questions.filter(customSearch).length">
              <td colspan="7">
                {{ t('$vuetify.quiz_module.question_list_search_empty') }}
              </td>
            </tr>
          </tbody>
        </v-table>
        <span v-else class="mb-4">{{ t('$vuetify.quiz_module.question_list_empty') }}</span>
      </template>
    </LoadingScreen>
    <template #append>
      <v-btn class="ms-4" variant="tonal" @click="clearSearch">
        {{ t('$vuetify.quiz_module.question_list_clear_filters') }}
      </v-btn>
      <v-btn class="ms-4" variant="tonal" @click="dismiss(false)">
        {{ t('$vuetify.close') }}
      </v-btn>
    </template>
  </v-card>
</template>
