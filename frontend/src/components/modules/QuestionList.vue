<script setup>
import {ref, onMounted, provide, toRefs, inject} from 'vue'
import { useLocale } from 'vuetify'
import {subjectApi, topicApi} from '@/service/api'
import quizApi from '@/service/quizApi'
import LoadingScreen from '@/components/custom/LoadingScreen.vue'
import QuestionListDialogRow from "@/components/modules/quiz/QuestionListDialogRow.vue";
import DeleteDialog from "@/components/custom/DeleteDialog.vue";
import {diacriticInsensitiveStringIncludes} from "@/plugins/constants";

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

const customSearch = (question) =>
       (!searchId.value || `${question.id}`.includes(searchId.value))
    && (!searchText.value || diacriticInsensitiveStringIncludes(question.questionData.toLowerCase(), searchText.value.toLowerCase()))
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

const questionDeleteDialog = ref(false)
const questionToDelete = ref(null)
provide('deleteDialog', questionDeleteDialog)
provide('questionToDelete', questionToDelete)

const reloadDialog = () => {
  Promise.all([
    props.quizId ? quizApi.quizDetail(props.quizId) : null,
    quizApi.listQuestions(),
    topicApi.listTopics(),
    subjectApi.listSubjects()
  ])
      .then((result) => {
        console.log(result)
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

const deleteQuestion = async (question) => {
  console.log(question)
  questions.value = null
  await props.deleteQuestion(question.id)
  quizApi.deleteQuestion(question.id)
      .then(() => {
        props.reload()
        reloadDialog()
      })
      .catch((err) => { error.value = err.code })
  questionDeleteDialog.value = false
}

onMounted(async () => {
  reloadDialog()
})
</script>

<template>
  <DeleteDialog title="$vuetify.quiz_module.question_list_delete_question_title"
                text-start="$vuetify.quiz_module.question_list_delete_question_text_p1"
                :item-name="questionToDelete?.questionData"
                text-before-line-break="$vuetify.quiz_module.question_list_delete_question_text_p2"
                text-second-line="$vuetify.irreversible_action"
                :on-cancel="() => questionDeleteDialog = false"
                :on-confirm="() => deleteQuestion(questionToDelete)"
                :text-confirm-button="'$vuetify.action_delete'" />
  <v-card :title="t('$vuetify.quiz_module.question_list_add_title')">
    <LoadingScreen :items="questions" :error="error">
      <template #content>
        <div class="d-flex mt-4" style="gap: 16px">
          <v-text-field v-model="searchId" style="flex: 0 0 15.5%" type="number" :label="t('$vuetify.module_list_header_id')" />
          <v-text-field v-model="searchText" style="flex: 0 0 15.5%" :label="t('$vuetify.module_list_header_name')" />
          <v-select v-model="searchAuthors" style="flex: 0 0 15.5%" :label="t('$vuetify.module_list_header_author')"
                    :items="[...new Set(questions.map((q) => q.author))].sort()"
                    multiple>
            <template #selection="{ index, item }">
              <v-chip v-if="searchAuthors.length && searchAuthors.length <= 2" size="small">
                <span>{{ item.title }}</span>
              </v-chip>
              <span v-else-if="searchAuthors.length && index === 0" class="text-caption align-self-center">
                {{ t('$vuetify.module_list_header_authors_selected', searchAuthors.length) }}
              </span>
            </template>
          </v-select>
          <v-select v-model="searchTypes" style="flex: 0 0 15.5%" :label="t('$vuetify.module_list_header_type')"
                    :items="[...new Set(questions.map((q) => q.questionType))].sort()" multiple />
          <v-autocomplete v-model="searchTopics" style="flex: 0 0 15.5%" :items="topics" item-title="name" item-value="id"
                          :label="t('$vuetify.module_list_header_topics')"
                          :no-data-text="t('$vuetify.module_list_topics_empty')"
                          multiple>
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
                                   :add-question="addQuestion" />
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
