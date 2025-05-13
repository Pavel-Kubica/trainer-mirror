<script setup>
import {inject,  onMounted, onUnmounted, ref} from "vue";
import api from "@/service/quizApi";
import Stomp from "webstomp-client";
import SockJS from "sockjs-client";
import axios from "axios";
import {MdPreview} from "md-editor-v3";
import {useUserStore} from "@/plugins/store";


const userStore = useUserStore()

const tState = inject('tState')
const teacherStates = inject('teacherStates')

const module = inject('moduleData')
const lesson = inject('lesson')
const quizData = inject('quizData')
const quizroomData = inject('quizroomData')
const questionData = inject('questionData')
const numOfQuestions = inject('numOfQuestions')
const currQuestionNum = inject('currQuestionNum')
const options = inject('options')

let stompClient = inject('stompClient')
const connected = inject('connected')
const timeClock = inject('timeClock')
const percentPerSecond = inject('percentPerSecond')
const refreshGuard = inject('refreshGuard')
const questionText = inject('questionText')

const error = ref(null)
//const timeLeft = ref(null)
const studAnsweredCount = ref(null)
const numberOfStudents = ref(null)
const modelSeconds = ref(100)

let interval = -1
let studCntRefreshInterval = -1
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
          console.log(questionData.value.quizroomId)

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


            timeClock.value = questionData.value.timeLimit
            percentPerSecond.value = 100 / questionData.value.timeLimit

            currQuestionNum.value = quizroomData.value.currQuestion + 1

            api.editQuizroom( quizroomData.value.id,{currQuestion : quizroomData.value.currQuestion + 1,
                              timeLeft: timeClock.value, quizState : teacherStates[2],
                              lesson: lesson})
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

onMounted(async () => {

  api.getLastQuizroomByStudent(userStore.user.id, module.id)
      .then((lastRoom) => {

        let roomId = lastRoom.id
        api.quizroomDetail(roomId)
            .then((room) => {
              quizroomData.value = room
              numberOfStudents.value = quizroomData.value.students.length

              api.quizDetail(room.quiz)
                  .then((quiz) => {
                    quizData.value = quiz
                    numOfQuestions.value = quizData.value.numOfQuestions

                    if(!refreshGuard.value){
                      refreshGuard.value = true

                      api.questionDetail(quiz.questions[room.currQuestion - 1])
                          .then((q) => {
                            questionData.value = q

                            console.log(q)
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
                              console.log(JSON.parse(q.possibleAnswersData))
                              options.value = JSON.parse(q.possibleAnswersData)
                            }


                            timeClock.value = room.timeLeft
                            percentPerSecond.value = 100 / questionData.value.timeLimit
                            modelSeconds.value = percentPerSecond.value * timeClock.value

                            questionText.value = q.questionData

                          })
                          .catch(() => {
                            console.log('failed to load question')
                          })
                    }

                    //nastavenie priebeznej aktualizacie studentov kt. uz otazku zodpovedali
                    studCntRefreshInterval = setInterval(() => {
                      api.listStudentAnswersFromQuestion(room.id, questionData.value.id)
                          .then( (a) => {
                            studAnsweredCount.value = a.length
                          })
                          .catch((err) => {
                            console.log(err)
                            error.value = `Chyba při načítání: ${err.code}`
                          })
                    }, 500)


                    //timer
                    interval = setInterval(() => {
                      if(timeClock.value <= 0){

                        api.editQuizroom(quizroomData.value.id, {quizState : teacherStates[3], lesson: lesson})
                            .then(() => {
                              tState.value = teacherStates[3]
                            })
                            .catch(() => {
                              console.log('error editing state')
                            })

                      }
                      else{
                        timeClock.value -= 1
                        modelSeconds.value = percentPerSecond.value * timeClock.value

                        api.editQuizroom(quizroomData.value.id, {timeLeft: timeClock.value, lesson: lesson})
                            .then((room) => {
                              quizroomData.value = room
                            })
                            .catch(() => {
                              console.log('error editing room')
                            })
                      }

                    },1000)


                  })
                  .catch((err) => {
                    console.log(err)
                    error.value = `Chyba při načítání: ${err.code}`
                  })


            })
            .catch((err) => {
              console.log(err)
              error.value = `Chyba při načítání: ${err.code}`
            })
      })

  if(!stompClient.value){
    connect()
  }

})

onUnmounted(() => {
  clearInterval(interval)
  clearInterval(studCntRefreshInterval)
})


</script>

<template>
  <v-progress-circular
    :rotate="360"
    :size="50"
    :width="5"
    :model-value="modelSeconds"
    color="blue"
    style="position: fixed; z-index: 999; top: 128px; right: 8px; font-size: 20px"
  >
    {{ timeClock }}
  </v-progress-circular>

  <v-card-subtitle>{{ studAnsweredCount }}/{{ numberOfStudents - 1 }}</v-card-subtitle>

  <v-card-title class="mt-5">
    <MdPreview v-model="questionText" language="en-US" preview-theme="github"
               :theme="userStore.darkMode ? 'dark' : 'light'" :no-iconfont="true" :read-only="true" class="px-4 pb-2 pl-5 text-left" :toolbars="[]" />
  </v-card-title>

  <v-card
    v-for="(option, index) in options"
    :key="option"
    :style="[userStore.darkMode ? {'background-color': 'black'} : {'background-color': 'white'}]"
    class="mt-5" variant="outlined">
    <v-row>
      <v-checkbox class="ma-5" style="width: 10px" :value="option" disabled indeterminate />
      <MdPreview v-model="options[index]" language="en-US" preview-theme="github"
                 :theme="userStore.darkMode ? 'dark' : 'light'" :no-iconfont="true" :read-only="true" class="px-4 pb-2 pt-3 text-left" style="width: 85%" />
    </v-row>
  </v-card>
</template>

<style scoped>

</style>