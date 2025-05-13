<script setup>
import {inject, onMounted, onUnmounted, ref} from "vue";
import api from "@/service/quizApi";
import Stomp from "webstomp-client";
import SockJS from "sockjs-client";
import axios from "axios";
import {MdPreview} from "md-editor-v3";
import {useUserStore} from "@/plugins/store";
import {useLocale} from "vuetify";
const { t } = useLocale()

const userStore = useUserStore()

const module = inject('moduleData')

const sState = inject('sState')
const studentStates = inject('studentStates')
const numOfQuestions = inject('numOfQuestions')
const quizroomData = inject('quizroomData')
const refreshGuard = inject('refreshGuard')
const questionText = inject('questionText')
const singleAnswer = inject('singleAnswer')

const questionData = inject('questionData')
const options = inject('options')
const answers = inject('answers')
const quizData = inject('quizData')
const timeClock = inject('timeClock')
const percentPerSecond = inject('percentPerSecond')

let stompClient = inject('stompClient')
const connected = inject('connected')

const correct = ref([])
const error = ref(null)
const modelValues = ref([])
const checkboxValues = ref([])
const currQ = ref(null)


const tmpJumpToEnd = () => {
  sState.value = studentStates[4]
}

let socket = new SockJS(axios.defaults.baseURL + "/broker");

function connect() {
  stompClient.value = Stomp.over(socket);
  stompClient.value.debug = () => {};   //disables debug messages in console
  stompClient.value.connect(
      {},
      () => {
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

const setModels = () => {
  if(answers.value){
    for (let i = 0; i < options.value.length; i++){
      modelValues.value[i] = answers.value.indexOf(options.value[i]) > -1
    }
  }
  else{
    for (let i = 0; i < options.value.length; i++){
      modelValues.value[i] = false
    }
  }

}

const setCheckboxColors = () => {
  for (let i = 0; i < 4; i++){
    checkboxValues.value[i] = (modelValues.value[i] && (correct.value.indexOf(options[i]) === -1))
  }

}

onMounted(() => {


  api.getLastQuizroomByStudent(userStore.user.id, module.id)
      .then((lastRoom) => {
        let roomId = lastRoom.id

        sState.value = studentStates[3]

        api.quizroomDetail(roomId)
            .then((room) => {
              quizroomData.value = room
              currQ.value = quizroomData.value.currQuestion

              api.quizDetail(quizroomData.value.quiz)
                  .then((quiz) => {
                    quizData.value = quiz
                    numOfQuestions.value = quizData.value.numOfQuestions

                    if(!refreshGuard.value) {
                      refreshGuard.value = true

                    }

                    api.questionDetail(quizData.value.questions[quizroomData.value.currQuestion - 1])
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
                            options.value = JSON.parse(questionData.value.possibleAnswersData)
                          }

                          timeClock.value = questionData.value.timeLimit
                          percentPerSecond.value = 100 / questionData.value.timeLimit

                          questionText.value = q.questionData

                          api.studentsAnswerFromQuestionDetail(quizroomData.value.id, questionData.value.id, userStore.user.id)
                              .then((a) => {
                                let tmp = []
                                for(let i in JSON.parse(a[0].data))
                                  tmp.push(JSON.parse(a[0].data)[i])
                                answers.value = tmp

                                setModels()

                                api.questionCorrect(q.id)
                                    .then((result) => {
                                      correct.value = JSON.parse(result.correctAnswerData)
                                      setCheckboxColors()
                                    })
                                    .catch((err) => {
                                      console.log(err)
                                      error.value = `Chyba při načítání: ${err.code}`
                                    })

                              })
                              .catch(() => {
                                console.log('err')
                              })

                        })
                        .catch(() => {
                          console.log('failed to load question')
                        })
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

onUnmounted(() => {
  answers.value = []
})
</script>

<template>
  <v-card-subtitle>{{ currQ }}/{{ numOfQuestions }}</v-card-subtitle>
  <v-card-title class="mb-8">
    <MdPreview v-model="questionText" language="en-US" preview-theme="github"
               :theme="userStore.darkMode ? 'dark' : 'light'" :no-iconfont="true" :read-only="true" class="px-4 pb-2 text-left" :toolbars="[]" />
  </v-card-title>

  <v-card
    v-for="(option, index) in options"
    :key="option"
    style="min-height: 75px"
    class="mt-5"
    :color="correct.indexOf(options[index]) > -1 ? '#4CAF50' : (checkboxValues[index] ? '#F44336' : (userStore.darkMode ? 'black' : 'default'))">
    <v-row>
      <v-checkbox v-model="modelValues[index]" class="ma-5" style="width: 10px" disabled />
      <MdPreview v-if="correct.indexOf(options[index]) > -1" v-model="options[index]" language="en-US" preview-theme="github"
                 :theme="userStore.darkMode ? 'dark' : 'light'" :no-iconfont="true" :read-only="true" class="px-4 pb-2 pt-3 text-left" style="width: 85%; background-color: #4CAF50" />
      <MdPreview v-else-if="checkboxValues[index]" v-model="options[index]" language="en-US" preview-theme="github"
                 :theme="userStore.darkMode ? 'dark' : 'light'" :no-iconfont="true" :read-only="true" class="px-4 pb-2 pt-3 text-left" style="width: 85%; background-color:#F44336" />
      <MdPreview v-else v-model="options[index]" language="en-US" preview-theme="github"
                 :theme="userStore.darkMode ? 'dark' : 'light'" :no-iconfont="true" :read-only="true" class="px-4 pb-2 pt-3 text-left" style="width: 85%" />
    </v-row>
  </v-card>

  <v-card-item>
    <v-btn v-if="currQ === numOfQuestions" class="mb-5" color="red" @click="tmpJumpToEnd">
      {{ t('$vuetify.quiz_module.end_quiz') }}
    </v-btn>
  </v-card-item>
</template>

  <style scoped>

  </style>