<script setup>

import {inject, onBeforeMount, onMounted, onUnmounted, ref} from "vue";
import api from "@/service/quizApi";
import {useUserStore} from "@/plugins/store";
import Stomp from "webstomp-client";
import SockJS from "sockjs-client";
import axios from "axios";
import {MdPreview} from "md-editor-v3";

const userStore = useUserStore()

const module = inject('moduleData')

const sState = inject('sState')
const studentStates = inject('studentStates')
const numOfQuestions = inject('numOfQuestions')
const quizData = inject('quizData')
const quizroomData = inject('quizroomData')
const timeClock = inject('timeClock')
const percentPerSecond = inject('percentPerSecond')
const refreshGuard = inject('refreshGuard')
const questionText = inject('questionText')
const singleAnswer = inject('singleAnswer')

let stompClient = inject('stompClient')
const connected = inject('connected')
const questionData = inject('questionData')
const options = inject('options')
const answers = inject('answers')
const radioAnswers = inject('radioAnswers')
const lessonId = inject('lesson')

const error = ref(null)

const modelSeconds = ref(100)
const answersSubmitted = ref(false)

let socket = new SockJS(axios.defaults.baseURL + "/broker");

function connect() {
  stompClient.value = Stomp.over(socket)
  stompClient.value.debug = () => {};   //disables debug messages in console
  stompClient.value.connect(
      {},
      async () => {
        connected.value = true

        stompClient.value.subscribe("/messages/questions", q => {
          questionData.value = JSON.parse(q.body)

          if(quizroomData.value.id === questionData.value.quizroomId){

            if(questionData.value.questionType === '' || questionData.value.questionType === 'LEGACY'){
              let tmp = []
              tmp.push( JSON.parse(questionData.value.possibleAnswersData).option1,
                  JSON.parse(questionData.value.possibleAnswersData).option2,
                  JSON.parse(questionData.value.possibleAnswersData).option3,
                  JSON.parse(questionData.value.possibleAnswersData).option4
              )
              options.value = tmp
            }
            else{
              console.log(JSON.parse(questionData.value.possibleAnswersData))
              options.value = JSON.parse(questionData.value.possibleAnswersData)
            }

            questionText.value = questionData.value.questionData
            singleAnswer.value = questionData.value.singleAnswer

            timeClock.value = questionData.value.timeLimit
            percentPerSecond.value = 100 / questionData.value.timeLimit

            sState.value = studentStates[2]

          }
        })
      },
      error => {
        console.log(error);
        connected.value = false;
      }
  );
}

const submitAnswer = () => {
  if(questionData.value.questionType === 'LEGACY' || questionData.value.questionType === ''){
    let answerString = ''

    if (!singleAnswer.value) {
      if (answers.value.length === 0)
        return

      answers.value = [...new Set(answers.value)]

      answerString = '{'
      for (let i = 0; i < answers.value.length; i++) {
        answerString = answerString + "\"answer" + (i + 1) + "\": " + JSON.stringify(answers.value[i])
        if ((i + 1) < answers.value.length)
          answerString = answerString + ','
      }
      answerString = answerString + '}'
    }
    else {

      if (!radioAnswers.value)
        return
      answerString = '{' + "\"answer1\": " + JSON.stringify(radioAnswers.value) + '}'
      answers.value = radioAnswers.value.split()
    }


    const answer = {
      lesson: lessonId,
      quizroom: quizroomData.value.id,
      student: userStore.user.id,
      question: questionData.value.id,
      data: answerString
    }

    api.createAnswer(answer)
        .catch((err) => {
          console.log(err)
          error.value = `Chyba při načítání: ${err.code}`
        })
  }
  else{

    if(questionData.value.singleAnswer){
      if(!radioAnswers.value)
        return

      console.log(questionData.value.questionType)

      const answer = {
        lesson: lessonId,
        quizroom: quizroomData.value.id,
        student: userStore.user.id,
        question: questionData.value.id,
        data: JSON.stringify([radioAnswers.value])
      }

      api.createAnswer(answer)
          .catch((err) => {
            console.log(err)
            error.value = `Chyba při načítání: ${err.code}`
          })
    }else{
      console.log(questionData)

      if (answers.value.length === 0)
        return

      answers.value = [...new Set(answers.value)]

      const answer = {
        lesson: lessonId,
        quizroom: quizroomData.value.id,
        student: userStore.user.id,
        question: questionData.value.id,
        data: JSON.stringify(answers.value)
      }

      api.createAnswer(answer)
          .catch((err) => {
            console.log(err)
            error.value = `Chyba při načítání: ${err.code}`
          })

    }

  }
  answersSubmitted.value = true
}

const selectOrUnselectOption = (option) => {
  if(!answersSubmitted.value){
    if(singleAnswer.value){
      radioAnswers.value = option
    }
    else{
      if(answers.value.includes(option)){
        answers.value = answers.value.filter(e => e !== option)
      }
      else{
        answers.value.push(option)
      }
    }

  }

}

let interval = -1

onMounted( () => {

  api.getLastQuizroomByStudent(userStore.user.id, module.id)
      .then((lastRoom) => {
        let roomId = lastRoom.id

        sState.value = studentStates[2]

        radioAnswers.value = ''
        answers.value = []

        api.quizroomDetail(roomId)
            .then( (room) => {
              quizroomData.value = room

              api.quizDetail(quizroomData.value.quiz)
                  .then((quiz) => {
                    quizData.value = quiz
                    numOfQuestions.value = quizData.value.numOfQuestions

                    if(!refreshGuard.value){
                      refreshGuard.value = true

                      api.questionDetail(quiz.questions[room.currQuestion - 1])
                          .then((q) => {
                            questionData.value = q

                            if(q.questionType === '' || q.questionType === 'LEGACY'){
                              let tmp = []
                              tmp.push( JSON.parse(questionData.value.possibleAnswersData).option1,
                                  JSON.parse(questionData.value.possibleAnswersData).option2,
                                  JSON.parse(questionData.value.possibleAnswersData).option3,
                                  JSON.parse(questionData.value.possibleAnswersData).option4
                              )
                              options.value = tmp
                            }
                            else{
                              console.log(JSON.parse(questionData.value.possibleAnswersData))
                              options.value = JSON.parse(questionData.value.possibleAnswersData)
                            }

                            timeClock.value = room.timeLeft
                            percentPerSecond.value = 100 / questionData.value.timeLimit
                            modelSeconds.value = percentPerSecond.value * timeClock.value

                            questionText.value = q.questionData
                            singleAnswer.value = questionData.value.singleAnswer

                            api.studentsAnswerFromQuestionDetail(quizroomData.value.id, questionData.value.id, userStore.user.id)
                                .then((a) => {
                                  answersSubmitted.value = a.length === 1
                                  let tmp = []
                                  for(let i in JSON.parse(a[0].data))
                                    tmp.push(JSON.parse(a[0].data)[i])
                                  answers.value = tmp
                                  radioAnswers.value = JSON.parse(a[0].data).answer1

                                })
                                .catch(() => {
                                  console.log('err')
                                })
                          })
                          .catch(() => {
                            console.log('failed to load question')
                          })
                    }

                    interval = setInterval(() => {
                      if(timeClock.value <= 0){

                        if(answersSubmitted.value === false)
                          submitAnswer()

                        sState.value = studentStates[3]
                      }
                      timeClock.value -= 1
                      modelSeconds.value = percentPerSecond.value * timeClock.value

                    },1000)

                  })
                  .catch((err) => {
                    console.log(err)
                    error.value = `Chyba při načítání: ${err.code}`
                  })
            })
            .catch(() => {
              console.log('error while loading data')
            })
      })

  if(!stompClient.value){
    connect()
  }

})

onBeforeMount(() => {
  clearInterval(interval)
})

onUnmounted(() => {
  clearInterval(interval)
})
</script>

<template>
  <v-progress-circular
    :rotate="360"
    :size="50"
    :width="5"
    :model-value="modelSeconds"
    :color="timeClock > 5 ? 'teal' : 'red'"
    style="position: fixed; z-index: 999; top: 96px; right: 8px; font-size: 20px"
  >
    {{ timeClock }}
  </v-progress-circular>

  <v-card-title style="top: 200px">
    <MdPreview v-model="questionText" language="en-US" preview-theme="github"
               :theme="userStore.darkMode ? 'dark' : 'light'" :no-iconfont="true" :read-only="true" class="px-4 pb-2 text-left" :toolbars="[]" />
  </v-card-title>

  <div v-if="options.length === 0">
    <v-text-field v-model="answers[0]" class="mt-8" label="Odpoveď..." />
  </div>

  <div v-if="!singleAnswer">
    <v-card
      v-for="(option, index) in options"
      :key="option"
      :style="[userStore.darkMode ? {'background-color': 'black'} : {'background-color': 'white'}]"
      class="mt-5"
      :variant="answers.indexOf(options[index]) > -1 ? 'tonal' : 'outlined'"
      @click="selectOrUnselectOption(options[index])">
      <v-row>
        <v-checkbox v-model="answers" class="ma-5" style="width: 10px" :value="options[index]" :disabled="answersSubmitted" />
        <MdPreview v-model="options[index]" language="en-US" preview-theme="github"
                   :theme="userStore.darkMode ? 'dark' : 'light'" :no-iconfont="true" :read-only="true" class="px-4 pb-2 pt-3 text-left" style="width: 85%" />
      </v-row>
    </v-card>
  </div>
  <div v-else>
    <v-radio-group v-model="radioAnswers">
      <v-card
        v-for="(option, index) in options"
        :key="option"
        :style="[userStore.darkMode ? {'background-color': 'black'} : {'background-color': 'white'}]"
        class="mt-5"
        :variant="radioAnswers === options[index] ? 'tonal' : 'outlined'"
        @click="selectOrUnselectOption(options[index])">
        <v-row>
          <v-radio class="ma-5" :value="options[index]" style="width: 10px; height: 64px" :disabled="answersSubmitted" />
          <MdPreview v-model="options[index]" language="en-US" preview-theme="github"
                     :theme="userStore.darkMode ? 'dark' : 'light'" :no-iconfont="true" :read-only="true" class="px-4 pb-2 pt-3 text-left" style="width: 85%" />
        </v-row>
      </v-card>
    </v-radio-group>
  </div>

  <v-btn v-if="!answersSubmitted" class="mr-5; mt-5" color="blue" @click="submitAnswer">
    Odeslat
  </v-btn>
</template>

<style scoped>

</style>