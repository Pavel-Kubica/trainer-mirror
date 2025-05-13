<script setup>
import {inject} from 'vue'
import quizApi from '@/service/quizApi'
import { useUserStore } from '@/plugins/store'
import TooltipIconButton from '@/components/custom/TooltipIconButton.vue'

const props = defineProps(['deleteQuestion','customSearch', 'dismiss', 'reloadDialog', 'reloadSource', 'topicChipColor', 'subjectChipColor', 'addQuestion'])
const userStore = useUserStore()

const questions = inject('questions')
//const quiz = inject('quiz')
const error = inject('error')
const quizQuestions = inject('quizQuestions')


const deleteQuestion = async (question) => {
  questions.value = null
  await props.deleteQuestion(question.id)
  quizApi.deleteQuestion(question.id)
      .then(() => {
        props.reloadSource()
        props.reloadDialog()
      })
      .catch((err) => { error.value = err.code })
}

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
                           @click="deleteQuestion(question)" />
        <TooltipIconButton v-if="!quizQuestions.some((que) => que.id === question.id)" icon="mdi-plus" tooltip="$vuetify.action_add"
                           color="rgb(var(--v-theme-anchor))" @click="props.addQuestion(question)" />
        <v-btn v-else variant="text" style="visibility: hidden" icon />
      </div>
    </td>
  </tr>
</template>
