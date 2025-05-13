<script setup>

import {inject, onMounted, ref} from "vue";
import api from "@/service/quizApi";
import Stomp from "webstomp-client";
import SockJS from "sockjs-client";
import axios from "axios";
import {MdPreview} from "md-editor-v3";
import {useUserStore} from "@/plugins/store";
import {useLocale} from "vuetify";
const { t } = useLocale()

const userStore = useUserStore()

const tState = inject('tState')
const teacherStates = inject('teacherStates')

const module = inject('moduleData')
const lesson = inject('lesson')

let stompClient = inject('stompClient')
const connected = inject('connected')

const currQuestionNum = inject('currQuestionNum')
const numOfQuestions = inject('numOfQuestions')
const questionData = inject('questionData')
const options = inject('options')
const quizroomData = inject('quizroomData')
const quizData = inject('quizData')
const timeClock = inject('timeClock')
const percentPerSecond = inject('percentPerSecond')
const refreshGuard = inject('refreshGuard')
const questionText = inject('questionText')

const error = ref(null)
const answersData = ref([])
const answers = ref([])
const correct = ref([])
const currQ = ref(null)
const showExpl = ref(false)
const explanation = ref('')

let socket = new SockJS(axios.defaults.baseURL + "/broker");

const modelValues = ref([])
let occurences = []
let emotes = []

let percentPerAnswer = 0

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

            timeClock.value = questionData.value.timeLimit
            percentPerSecond.value = 100 / questionData.value.timeLimit

            currQuestionNum.value = quizroomData.value.currQuestion + 1

            api.editQuizroom(quizroomData.value.id,{currQuestion : quizroomData.value.currQuestion + 1, quizState : teacherStates[2], lesson: lesson})
                .then((result) => {
                  quizroomData.value = result
                })
                .catch(() => {
                  console.log('error editing state')
                })


            tState.value = teacherStates[2]

          }

        });
      },
      error => {
        console.log(error);
        connected.value = false;
      }
  );
}

function send() {

  if (stompClient && stompClient.value.connected){
    stompClient.value.send("/ws/questions",JSON.stringify({id : quizData.value.questions[quizroomData.value.currQuestion],
      room: quizroomData.value.id}))
  }
}

const countOccurences = () => {

  for (let i = 0; i < options.value.length; i++)
    occurences[i] = 0

  answers.value.forEach( a => {
    for (let i = 0; i < options.value.length; i++) {
      if(a === options.value[i]){
        occurences[i]++
        break
      }
    }
  })

  let sortedOcc = []
  for(let i = 0; i < occurences.length; i++){
    sortedOcc[i] = occurences[i]
  }

  //nejaka 'max' funkce mozno?
  sortedOcc.sort()
  percentPerAnswer = 100 / sortedOcc[sortedOcc.length - 1]
  for(let i = 0; i < occurences.length; i++){
    modelValues.value[i] = occurences[i] * percentPerAnswer
  }
}

//dotiahne otazku
const nextQuestion = () => {
  send()
  tState.value = teacherStates[2]
}

//ukaze celk. vysledky
const showFinalStats = () => {

  api.editQuizroom(quizroomData.value.id, {quizState : teacherStates[4], lesson: lesson})
      .catch(() => {
        console.log('error editing state')
      })

  tState.value = teacherStates[4]
}

const showHideExplanation = () => {
  showExpl.value = !showExpl.value
}

onMounted(async () => {


  await api.getLastQuizroomByStudent(userStore.user.id, module.id)
      .then(async (lastRoom) => {
        let roomId = lastRoom.id

        setTimeout(async () => {
          await api.quizroomDetail(roomId)
              .then(async (room) => {
                quizroomData.value = room
                currQ.value = quizroomData.value.currQuestion

                await api.quizDetail(room.quiz)
                    .then(async (quiz) => {
                      quizData.value = quiz
                      numOfQuestions.value = quizData.value.numOfQuestions

                      if(!refreshGuard.value) {
                        refreshGuard.value = true
                      }

                      await api.questionDetail(quiz.questions[room.currQuestion - 1])
                          .then(async (q) => {
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

                            await api.questionCorrect(q.id)
                                .then((result) => {
                                  correct.value = JSON.parse(result.correctAnswerData)
                                  for(let i = 0; i < options.value.length; i++){
                                    if(correct.value.indexOf(options.value[i]) > -1){
                                      emotes[i] = '✔'
                                    }
                                    else{
                                      emotes[i] = '✖'
                                    }
                                  }
                                  explanation.value = result.explanation
                                })
                                .catch((err) => {
                                  console.log(err)
                                  error.value = `Chyba při načítání: ${err.code}`
                                })

                            setTimeout(async () => {
                              await api.listStudentAnswersFromQuestion(quizroomData.value.id,questionData.value.id)
                                  .then((result) => {
                                    answersData.value = result


                                    answersData.value.forEach( a => {
                                      let jsonAnswer = JSON.parse(a.data)

                                      for(let i in jsonAnswer){
                                        answers.value.push(jsonAnswer[i])
                                      }

                                    })

                                    countOccurences()
                                  })
                                  .catch((err) => {
                                    console.log(err)
                                    error.value = `Chyba při načítání: ${err.code}`
                                  })
                            },500)

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
        }, 1000)

      })


  if(!stompClient.value){
    connect()
  }
})

</script>

<template>
  <v-card-subtitle>{{ currQ }}/{{ numOfQuestions }}</v-card-subtitle>

  <v-card-title>
    <MdPreview v-model="questionText" language="en-US" preview-theme="github"
               :theme="userStore.darkMode ? 'dark' : 'light'" :no-iconfont="true" :read-only="true" class="px-4 pb-2 pl-10 text-left" :toolbars="[]" />
  </v-card-title>

  <div v-for="(option, index) in options" :key="index">
    <v-card class="mt-8" style="min-height: 75px" :color="userStore.darkMode ? 'black' : 'white'">
      <v-row>
        <MdPreview v-model="options[index]" language="en-US" preview-theme="github"
                   :theme="userStore.darkMode ? 'dark' : 'light'" :no-iconfont="true" :read-only="true" class="px-4 pb-2 pt-3 ml-8 text-left" style="width: 85%" />
        <v-icon v-if="correct.indexOf(options[index]) > -1" size="25" class="mr-8 mt-8">
          mdi-checkbox-marked-circle
        </v-icon>
      </v-row>
    </v-card>
    <v-progress-linear class="mt-2"
                       height="16"
                       :bg-color="correct.indexOf(options[index]) > -1 ? 'green-lighten-2' : 'red-lighten-2' "
                       :color="correct.indexOf(options[index]) > -1 ? 'green' : 'red'"
                       :model-value="modelValues[index]">
      <b>{{ emotes[index] }} {{ occurences[index] }}</b>
    </v-progress-linear>
  </div>

  <v-card-item>
    <v-btn v-if="currQ !== numOfQuestions" class="mt-10" color="green" @click="nextQuestion">
      {{ t('$vuetify.quiz_module.next_question') }}
    </v-btn>
  </v-card-item>
  <v-card-item>
    <v-btn class="mb-5" color="red" @click="showFinalStats">
      {{ t('$vuetify.quiz_module.end_quiz') }}
    </v-btn>
  </v-card-item>

  <v-card v-if="explanation && explanation !== ''" class="mt-5" :color="userStore.darkMode ? 'black' : 'white'">
    <v-card-actions>
      <v-row @click="showHideExplanation()">
        <v-btn color="blue" variant="text" class="ma-1">
          Vysvětlení
        </v-btn>
        <v-spacer />
        <v-btn :icon="showExpl ? 'mdi-chevron-up' : 'mdi-chevron-down'"
        />
      </v-row>
    </v-card-actions>
    <v-expand-transition>
      <div v-show="showExpl">
        <v-card-item>
          <MdPreview v-model="explanation" language="en-US" preview-theme="github"
                     :theme="userStore.darkMode ? 'dark' : 'light'" :no-iconfont="true" :read-only="true"
                     class="px-4 pb-2 pl-10 text-left" :toolbars="[]" />
        </v-card-item>
      </div>
    </v-expand-transition>
  </v-card>
</template>

<style scoped>

</style>