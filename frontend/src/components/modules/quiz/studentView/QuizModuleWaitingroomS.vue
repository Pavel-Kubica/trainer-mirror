<script setup>
import {inject, onMounted, ref} from "vue";
import api from "@/service/quizApi";
import SockJS from "sockjs-client";
import Stomp from "webstomp-client";
import axios from "axios";
import {useUserStore} from "@/plugins/store";
import {useLocale} from "vuetify";
const { t } = useLocale()

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

let socket = new SockJS(axios.defaults.baseURL + "/broker");

const error = ref(null)
//const quizroom = ref(null)


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
              console.log(JSON.parse(questionData.value.possibleAnswersData))
              options.value = JSON.parse(questionData.value.possibleAnswersData)
            }

            questionText.value = questionData.value.questionData
            singleAnswer.value = questionData.value.singleAnswer

            timeClock.value = questionData.value.timeLimit
            percentPerSecond.value = 100 / questionData.value.timeLimit


            sState.value = studentStates[2]

          }
          else{
            console.log('false')
          }
        });
      },
      error => {
        console.log(error);
        connected.value = false;
      }
  );
}

//dotiahne konkretny kviz z BE
const loadQuiz = async (id) => {
  api.quizDetail(id)
      .then((result) => {
        quizData.value = result
        numOfQuestions.value = quizData.value.numOfQuestions
      })
      .catch((err) => {
        console.log(err)
        error.value = `Chyba při načítání: ${err.code}`
      })
}

onMounted(async () => {


  api.getLastQuizroomByStudent(userStore.user.id, module.id)
      .then((lastRoom) => {
        let roomId = lastRoom.id

        api.quizroomDetail(roomId)
            .then( (room) => {
              quizroomData.value = room
              loadQuiz(quizroomData.value.quiz)
              if(!refreshGuard.value){
                refreshGuard.value = true
              }
            })
            .catch(() => {
              console.log('error while loading data')
            })
      })



  connect()

})
</script>

<template>
  <v-card-title>{{ t('$vuetify.quiz_module.waiting_room') }}</v-card-title>

  <v-card-subtitle class="mt-5 mb-3" style="color: crimson; font-size: 16px">
    {{ t('$vuetify.quiz_module.waiting_room_joke') }}
  </v-card-subtitle>
</template>

<style scoped>

</style>