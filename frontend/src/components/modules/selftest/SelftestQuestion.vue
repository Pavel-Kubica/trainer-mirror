<script setup>

import {MdPreview} from "md-editor-v3";
import {inject, onBeforeMount, onMounted} from "vue";
import {useUserStore} from "@/plugins/store";
import api from "@/service/quizApi";
import {useLocale} from "vuetify";

const userStore = useUserStore()
const { t } = useLocale()


const selftestState = inject('selftestState')
const selftestStateList = inject('selftestStateList')
const selftestData = inject('selftestData')

const currentQuestion = inject('currentQuestion')
const currentQuestionIndex = inject('currentQuestionIndex')
const currentSelectedAnswers = inject('currentSelectedAnswers')
const lesson = inject('lesson')

let timer = -1


const selectOrUnselectOption = (option) => {
  if(currentQuestion.value.questionType === 'TRUEFALSE'){
    if(!currentSelectedAnswers.value.includes(option))
      currentSelectedAnswers.value = [option]
    else
      currentSelectedAnswers.value = []
  }
  else{
    if(currentSelectedAnswers.value.includes(option))
      currentSelectedAnswers.value = currentSelectedAnswers.value.filter(e => e !== option)
    else
      currentSelectedAnswers.value.push(option)

  }

}

const leaveSelftest = async ()  => {

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

const toResultsOrOverview = async (forceQuit)  => {
  clearInterval(timer)

  let answerString = JSON.stringify(currentSelectedAnswers.value)
  const answer = {
    lesson: lesson,
    quizroom: selftestData.value.id,
    student: userStore.user.id,
    question: currentQuestion.value.id,
    data: answerString
  }

  if(!selftestData.value.answered[currentQuestionIndex.value - 1]) {

    await api.createAnswer(answer)
        .then((response) => {
          selftestData.value.answered[currentQuestionIndex.value - 1] = response.id
        })
        .catch((err) => {
          console.log(err)
        })
  }
  else{
    await api.updateAnswer(selftestData.value.answered[currentQuestionIndex.value - 1], answer)
        .catch((err) => {
          console.log(err)
        })
  }

  if (forceQuit === true){
    api.editQuizroom(selftestData.value.id, {lesson: lesson, quizState : selftestStateList.value[3]})
        .then(() => {
          selftestState.value = selftestStateList.value[3];
        })
        .catch(err => {
          console.log(err)
        })
  }
  else{
    api.editQuizroom(selftestData.value.id, {lesson: lesson, quizState : selftestStateList.value[2]})
        .then(() => {
          selftestState.value = selftestStateList.value[2];
        })
        .catch(err => {
          console.log(err)
        })
  }


}

onMounted( () => {
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
      toResultsOrOverview(true)
    }

  }, 1000)
})

onBeforeMount(() => {
  clearInterval(timer)
})


</script>

<template>
  <div v-if="selftestData && currentQuestion">
    <v-pagination v-model="currentQuestionIndex" :length="selftestData.selftestQuestions.length" rounded="circle" total-visible="3" />

    <v-row class="mt-4">
      <v-col cols="8">
        <div class="mx-16">
          <v-card class="mb-16">
            <MdPreview v-model="currentQuestion.questionData" language="en-US" preview-theme="github"
                       :theme="userStore.darkMode ? 'dark' : 'light'" :no-iconfont="true" :read-only="true" class="px-4 pb-2 text-left" :toolbars="[]" />
          </v-card>

          <v-card
            v-for="(option, index) in currentQuestion.possibleAnswersData"
            :key="option"
            :style="[userStore.darkMode ? {'background-color': 'black'} : {'background-color': 'white'}]"
            class="mt-5"
            variant="outlined"
            @click="selectOrUnselectOption(option)">
            <v-row>
              <v-checkbox v-model="currentSelectedAnswers" class="ma-5" style="width: 10px" :value="option" />
              <MdPreview v-model="currentQuestion.possibleAnswersData[index]" language="en-US" preview-theme="github"
                         :theme="userStore.darkMode ? 'dark' : 'light'" :no-iconfont="true" :read-only="true" class="px-4 pb-2 pt-3 text-left" style="width: 85%" />
            </v-row>
          </v-card>
        </div>
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

            <v-row class="mb-4 mt-4 text-center align-center justify-center">
              <v-btn color="green" @click="toResultsOrOverview(false)">
                {{ t('$vuetify.selftest_module.to_review') }}
              </v-btn>
            </v-row>
            <v-row class="mb-4 text-center align-center justify-center">
              <v-btn color="red" @click="leaveSelftest()">
                {{ t('$vuetify.selftest_module.leave') }}
              </v-btn>
            </v-row>
          </div>
        </v-card>
      </v-col>
    </v-row>

    <v-pagination v-model="currentQuestionIndex" class="mt-4" :length="selftestData.selftestQuestions.length" rounded="circle" total-visible="3" />
  </div>
</template>

<style scoped>

</style>