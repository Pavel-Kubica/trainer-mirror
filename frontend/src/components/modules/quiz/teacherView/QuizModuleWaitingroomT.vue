<script setup>
import {inject, onMounted, onUnmounted, ref} from "vue";
import SockJS from "sockjs-client";
import Stomp from "webstomp-client";
import {studentModuleApi} from "@/service/api";
import api from "@/service/quizApi";
import axios from "axios";
import {useUserStore} from "@/plugins/store";
import {anonEmoji} from "@/plugins/constants"
import {useLocale} from "vuetify";
const { t } = useLocale()


const tState = inject('tState')
const teacherStates = inject('teacherStates')
const userStore = useUserStore()

const module = inject('moduleData')
const lesson = inject('lesson')
let stompClient = inject('stompClient')
const connected = inject('connected')
const questionData = inject('questionData')
const currQuestionNum = inject('currQuestionNum')
const options = inject('options')
const quizData = inject('quizData')
const quizroomData = inject('quizroomData')
const timeClock = inject('timeClock')
const percentPerSecond = inject('percentPerSecond')
const refreshGuard = inject('refreshGuard')
const questionText = inject('questionText')


const error = ref(null)
const password = ref('')
const numOfStudents = ref(0)

let socket = new SockJS(axios.defaults.baseURL + "/broker");

const studentList = ref([])
let interval = -1
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
  if (stompClient && stompClient.value.connected) {
    stompClient.value.send("/ws/questions",JSON.stringify({id : quizData.value.questions[quizroomData.value.currQuestion],
                                                                          room: quizroomData.value.id}))
  }
}

function disconnect() {
  if (stompClient) {
    stompClient.value.disconnect();
  }
  connected.value = false;
}

const endQuiz = () => {

  const quizroom = {
    roomState: false,
    lesson: lesson
  }

  api.editQuizroom(quizroomData.value.id,quizroom)


  currQuestionNum.value = 0
  tState.value = teacherStates[0]
}
const leaveRoom = () => {
  endQuiz()
  disconnect()

  const teacherModule = {
    allowedShow: module.allowedShow ?? false,
    data : ""
  }
  studentModuleApi.putStudentModule(lesson,module.id, teacherModule)
      .then(() => {
        module.studentData = ""
      })
}
//dotiahne otazku
const nextQuestion = () => {
  send()
}

onMounted(async () => {

  api.getLastQuizroomByStudent(userStore.user.id, module.id)
      .then((lastRoom) => {
        let roomId = lastRoom.id

        api.quizroomDetail(roomId)
            .then((room) => {
              quizroomData.value = room
              password.value = room.roomPassword
              api.quizDetail(room.quiz)
                  .then((quiz) => {
                    quizData.value = quiz

                    if(!refreshGuard.value){
                      refreshGuard.value = true
                    }

                  })
                  .catch(() => {
                    console.log('error loading quiz')
                  })

              interval = setInterval(() => {
                api.listQuizroomStudents(room.id)
                    .then((result) => {
                      numOfStudents.value = result.length
                      let tmp = result

                      tmp = tmp.map(s => s.student).filter( (n) => n !== userStore.user.username)

                      for(let i = 0; i < tmp.length; i++){
                        if(!studentList.value.map(s => s.name).includes(tmp[i])){
                          const stud = {
                            name: tmp[i],
                            emoji: anonEmoji(tmp[i])
                          }
                          studentList.value[i] = stud
                        }

                      }

                    })
                    .catch((err) => {
                      console.log(err)
                      error.value = `Chyba při načítání: ${err.code}`
                    })
              },1000)

            })
            .catch(() => {
              console.log('error loading quizroom')
            })
      })

  connect()
})

onUnmounted(() => {
  clearInterval(interval)
})

</script>

<template>
  <v-row class="mt-1 mr-1 justify-end" style="height: 24px">
    <v-btn elevation="0" icon="mdi-close" @click="leaveRoom()">
      <v-icon>mdi-close</v-icon>
      <v-tooltip
        activator="parent"
        location="bottom"
      >
        Odejít
      </v-tooltip>
    </v-btn>
  </v-row>

  <v-card-title>{{ t('$vuetify.quiz_module.waiting_room') }}</v-card-title>
  <h5 class="mt-10">
    {{ t('$vuetify.quiz_module.room_password') }}
  </h5>
  <h1 v-if="password" style="color: red">
    {{ password }}
  </h1>

  <v-card-subtitle class="mt-5">
    {{ t('$vuetify.quiz_module.number_of_students') }} {{ numOfStudents === 0 ? 0 : numOfStudents - 1 }}
  </v-card-subtitle>
  <v-card-subtitle v-if="userStore.darkMode" class="mt-5 font-weight-bold" style="color: white">
    {{ t('$vuetify.quiz_module.students') }}
  </v-card-subtitle>
  <v-card-subtitle v-else class="mt-5 font-weight-bold" style="color: black">
    {{ t('$vuetify.quiz_module.students') }}
  </v-card-subtitle>

  <v-divider v-if="numOfStudents - 1 > 0" class="mt-3 mb-3" thickness="2" />

  <div class="mt-5 text-center">
    <v-row>
      <v-col v-for="stud in studentList" :key="stud" sm="12"
             md="6"
             lg="3"
             class="d-flex">
        <b style="font-size: 32px">
          {{ userStore.anonymous ? stud.emoji : stud.name }}
        </b>
      </v-col>
    </v-row>
  </div>


  <v-btn class="mt-10" color="green" @click="nextQuestion">
    {{ t('$vuetify.quiz_module.launch_quiz') }}
  </v-btn>
</template>

<style scoped>

</style>