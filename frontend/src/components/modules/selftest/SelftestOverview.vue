<script setup>

import {inject, onBeforeMount, onMounted, ref} from "vue";
import api from "@/service/quizApi";
import {useUserStore} from "@/plugins/store";
import {useLocale} from "vuetify";
const userStore = useUserStore()
const { t } = useLocale()


const selftestState = inject('selftestState')
const selftestStateList = inject('selftestStateList')
const selftestData = inject('selftestData')
const currentQuestionIndex = inject('currentQuestionIndex')
const currentSelectedAnswers = inject('currentSelectedAnswers')

const lesson = inject('lesson')


const selftestQuestionList = ref([])

let timer = -1
const leaveSelftest = () => {

  api.editQuizroom(selftestData.value.id, {lesson: lesson, quizState : 'CLOSED', roomState: false})
      .then(() => {
        clearInterval(timer)
        selftestState.value = selftestStateList.value[0]
        selftestData.value = null
        currentSelectedAnswers.value = []
        currentQuestionIndex.value = 1
      })
      .catch(err => {
        console.log(err)
      })
}
const submitSelftest = () => {

  api.editQuizroom(selftestData.value.id, {lesson: lesson, quizState : selftestStateList.value[3]})
      .then(() => {
        clearInterval(timer)
        currentSelectedAnswers.value = []
        currentQuestionIndex.value = 1
        selftestState.value = selftestStateList.value[3]
      })
      .catch(err => {
        console.log(err)
      })
}
const toQuestion = async (qId) => {
  clearInterval(timer)

  currentQuestionIndex.value = selftestData.value.selftestQuestions.indexOf(qId) + 1
  selftestState.value = selftestStateList.value[1]

}

onMounted(async () => {
  await selftestData.value.selftestQuestions.forEach( (qId) => {
    api.questionDetail(qId)
        .then(async (qData) => {

          await api.questionCorrect(qId)
              .then(async (qCorr) => {
                let toInsert = qData
                toInsert.possibleAnswersData = JSON.parse(qData.possibleAnswersData)
                toInsert.correctAnswerData = JSON.parse(qCorr.correctAnswerData)
                toInsert.explanation = qCorr.explanation
                toInsert.orderNum = selftestData.value.selftestQuestions.indexOf(qId)

                await api.studentsAnswerFromQuestionDetail(selftestData.value.id, qId, userStore.user.id)
                    .then((answer) => {
                      if (answer.length === 0)
                        toInsert.studentAnswerData = []
                      else
                        toInsert.studentAnswerData = JSON.parse(answer[0].data)

                      selftestQuestionList.value.push(toInsert)
                    })
                    .catch((error) => {
                      console.log(error)
                    })

                selftestQuestionList.value = selftestQuestionList.value.sort((a,b) => a.orderNum - b.orderNum )

              })
              .catch((error) => {
                console.log(error)
              })

        })
        .catch((error) => {
          console.log(error)
        })
  })

  timer = setInterval(() => {
    if (selftestData.value.timeLeft > 0){

      selftestData.value.timeLeft -= 1
      api.editQuizroom(selftestData.value.id, {timeLeft: selftestData.value.timeLeft, lesson: lesson})
          .catch(err => {
            console.log(err)
          })
    }
    if (selftestData.value.timeLeft === 0){
      clearInterval(timer)
      api.editQuizroom(selftestData.value.id, {timeLeft: 0, quizState: selftestStateList.value[2],lesson: lesson})
          .catch(err => {
            console.log(err)
          })

      selftestState.value = selftestStateList.value[3]
      //toResultsOrOverview(true)
    }

  }, 1000)

})

onBeforeMount(() => {
  clearInterval(timer)
})
</script>

<template>
  <v-row class="mt-4">
    <v-col cols="8">
      <v-table class="ml-4 ml-8 ">
        <thead>
          <tr>
            <th class="text-left">
              <b>{{ t('$vuetify.selftest_module.question') }}</b>
            </th>
            <th class="text-left">
              <b>Status</b>
            </th>
            <th />
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="(q, index) in selftestQuestionList"
            :key="index"
          >
            <td style="width: 40%;">
              <b>{{ t('$vuetify.selftest_module.question') }} {{ index + 1 }}</b>
            </td>
            <td style="width: 40%">
              <b>
                {{ q.studentAnswerData.length === 0 ? t('$vuetify.selftest_module.question_status_negative') : t('$vuetify.selftest_module.question_status_positive') }}
              </b>
            </td>
            <td class="text-right">
              <v-btn color="blue" variant="tonal" @click="toQuestion(q.id)">
                {{ t('$vuetify.selftest_module.to_question') }}
              </v-btn>
            </td>
          </tr>
        </tbody>
      </v-table>

      <v-btn class="ma-8" color="green" @click="submitSelftest()">
        {{ t('$vuetify.selftest_module.hand_in') }}
      </v-btn>
    </v-col>
    <v-col cols="1" />
    <v-col cols="2">
      <v-card style="position: fixed; z-index: 999" variant="outlined">
        <v-row class="mt-4 ml-1 mr-8 pa-4">
          {{ t('$vuetify.selftest_module.number_of_questions') }}: {{ selftestData.selftestQuestions.length }}
        </v-row>
        <v-divider :color="userStore.darkMode ? 'white' : 'black' " class="mb-4" />
        <div class="text-center align-center justify-center">
          <v-card class="ma-4 pa-4 pl-8 pr-8 text-center align-center justify-center" color="blue" min-height="64" style="font-size: 32px;">
            {{ String(Math.floor(selftestData.timeLeft / 3600)).padStart(2,'0') }}:{{ String(Math.floor((selftestData.timeLeft % 3600) / 60)).padStart(2,'0') }}:{{ String(selftestData.timeLeft % 60).padStart(2,'0') }}
          </v-card>

          <v-row class="ma-4 text-center align-center justify-center">
            <v-btn color="red" @click="leaveSelftest()">
              {{ t('$vuetify.selftest_module.leave') }}
            </v-btn>
          </v-row>
        </div>
      </v-card>
    </v-col>
  </v-row>
</template>

<style scoped>

</style>