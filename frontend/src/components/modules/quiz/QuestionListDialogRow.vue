<script setup>
import {inject} from 'vue'
import { useUserStore } from '@/plugins/store'
import TooltipIconButton from '@/components/custom/TooltipIconButton.vue'
import {useLocale} from "vuetify";

const props = defineProps(['customSearch', 'dismiss', 'topicChipColor', 'subjectChipColor', 'addQuestion'])
const userStore = useUserStore()
const {t} = useLocale()

const questions = inject('questions')
//const quiz = inject('quiz')
const quizQuestions = inject('quizQuestions')
const questionDeleteDialog = inject('deleteDialog')
const questionToDelete = inject('questionToDelete')

/*const addQuestion = async (question) => {
  questions.value = null
  quizApi.putQuizQuestion(quiz.value.id, question.id, {
    orderNum: (quiz.value.questions.slice(-1)[0]?.order ?? 0) + 1
  })
   .then(() => {
    props.reloadSource()
    props.reloadDialog()
  })
      .catch((err) => { error.value = err.code })

}*/
</script>

<template>
  <tr v-for="question in questions.filter(customSearch)" :key="question.id">
    <td>#{{ question.id }}</td>
    <td>
      {{ question.questionData }}
    </td>
    <td>{{ question.author }}</td>
    <td>{{ question.questionType }}</td>
    <td>
      <div class="d-flex flex-wrap" style="gap: 3px; padding: 5px">
        <v-chip v-for="topic in question.topics" :key="topic.id" class="mr-2" :color="topicChipColor(topic)">
          {{ topic.name }}
        </v-chip>
      </div>
    </td>
    <td>
      <div class="d-flex flex-wrap" style="gap: 3px; padding: 5px">
        <v-chip v-for="subject in question.subjects" :key="subject.id" class="mr-2" :color="subjectChipColor(subject)">
          {{ subject.code }}
        </v-chip>
      </div>
    </td>
    <td>
      <div class="d-flex justify-end">
        <TooltipIconButton v-if="question.author === userStore.user.username" icon="mdi-delete" color="red"
                           @click="questionToDelete = question; questionDeleteDialog = true" />
        <div v-else style="display: flex; align-items: center; justify-content: center; width: 48px; height: 48px">
          <v-icon icon="mdi-delete" color="grey" size="24" style="align-self: center" />
          <v-tooltip activator="parent" location="top">
            {{ t('$vuetify.quiz_module.question_list_cannot_delete') }}
          </v-tooltip>
        </div>
        <TooltipIconButton v-if="!quizQuestions.some((que) => que.id === question.id)" icon="mdi-plus" tooltip="$vuetify.action_add"
                           color="rgb(var(--v-theme-anchor))" @click="() => props.addQuestion(question)" />
        <div v-else style="display: flex; align-items: center; justify-content: center; width: 48px; height: 48px">
          <v-icon icon="mdi-plus" color="grey" size="24" style="align-self: center" />
          <v-tooltip activator="parent" location="top">
            {{ t('$vuetify.quiz_module.question_list_cannot_add') }}
          </v-tooltip>
        </div>
      </div>
    </td>
  </tr>
</template>
