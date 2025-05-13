<script setup>

import {inject, onMounted, onUnmounted, ref} from "vue";
import { studentModuleApi } from '@/service/api'
import api from "@/service/quizApi";
import Stomp from "webstomp-client";
import SockJS from "sockjs-client";
import axios from "axios";
import {useUserStore} from "@/plugins/store";
import {MdPreview} from "md-editor-v3";
import {useLocale} from "vuetify";
const { t } = useLocale()

const userStore = useUserStore()

const sState = inject('sState')
const studentStates = inject('studentStates')
const currQuestionNum = inject('currQuestionNum')
const quizroomData = inject('quizroomData')
const quizData = inject('quizData')

const module = inject('moduleData')
const lesson = inject('lesson')
let stompClient = inject('stompClient')
const connected = inject('connected')
const questionData = inject('questionData')
const options = inject('options')

const error = ref(null)
const leaderboard = ref([])
const tableLeaderboard = ref([])

const studAnswers = ref([])
const questionList = ref([])
const possibleAnswers = ref([])
const questionShow = ref([])
const cardColors = ref([])


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

          options.value = questionData.value.possibleAnswersData.split(',')

        });
      },
      error => {
        console.log(error);
        connected.value = false;
      }
  );
}
function disconnect() {
  if (stompClient.value) {
    stompClient.value.disconnect();
  }
  connected.value = false;
}

function sortAndCutoff(){
  leaderboard.value.sort((a,b) => b.score - a.score)

  if(leaderboard.value.length > 5){
    for (let i = 0; i < 5; i++)
      tableLeaderboard.value[i] = leaderboard.value[i]
  }
  else{
    tableLeaderboard.value = leaderboard.value
  }
}
const leaveQuiz = () => {
  const studentModule = {
    allowedShow: module.allowedShow ?? false,
    data : ""
  }
  studentModuleApi.putStudentModule(lesson,module.id, studentModule)
      .then(() => {
        module.studentData = ""
      })

  sState.value = studentStates[0]
  currQuestionNum.value = 0
}

const showHideQuestion = (index) => {
  questionShow.value[index] = !questionShow.value[index]
}

const answerWasCorrect = (a, b, c) => {

  if(b == null)
    return false

  a = a.filter(a => c.includes(a))
  a.sort()
  b.sort()

//  let x = ['a','b']
  return Array.isArray(a) && Array.isArray(b)
    && a.length === b.length && a.every((val, index) => val === b[index])
}

onMounted(async () => {

  api.getLastQuizroomByStudent(userStore.user.id, module.id)
      .then((lastRoom) => {
        let roomId = lastRoom.id
        sState.value = studentStates[4]

        api.quizroomDetail(roomId)
            .then((room) => {
              quizroomData.value = room

              api.listQuizroomStudents(quizroomData.value.id)
                  .then((result) => {
                    for(let i = 0; i < result.length; i++){
                      leaderboard.value[i] = {
                        student: result[i].student,
                        score: result[i].points
                      }
                    }

                    sortAndCutoff()

                  })
                  .catch((err) => {
                    console.log(err)
                    error.value = `Chyba při načítání: ${err.code}`
                  })

              api.quizDetail(room.quiz)
                  .then((quiz) => {
                    quizData.value = quiz

                    for(let i = 0; i < quiz.questions.length; i++){
                      let q = quiz.questions[i]
                      api.questionDetail(q)
                          .then((qData) => {
                            studAnswers.value[i] = qData
                            studAnswers.value[i].order = i

                            api.questionCorrect(q)
                                .then((qCorrect) => {
                                  studAnswers.value[i].correctAnswerData = JSON.parse(qCorrect.correctAnswerData)

                                  api.studentsAnswerFromQuestionDetail(room.id,q,userStore.user.id)
                                      .then((a) => {

                                        if(a[0]){
                                          let tmp = []
                                          for(let i in JSON.parse(a[0].data))
                                            tmp.push(JSON.parse(a[0].data)[i])

                                          studAnswers.value[i].answerData = tmp
                                        }
                                        else
                                          studAnswers.value[i].answerData = null

                                        if(qData.questionType ===  'LEGACY'){
                                           studAnswers.value[i].possibleAnswersData = [
                                             JSON.parse(studAnswers.value[i].possibleAnswersData).option1, JSON.parse(studAnswers.value[i].possibleAnswersData).option2,
                                             JSON.parse(studAnswers.value[i].possibleAnswersData).option3, JSON.parse(studAnswers.value[i].possibleAnswersData).option4]
                                        }
                                        else{
                                          studAnswers.value[i].possibleAnswersData = JSON.parse(studAnswers.value[i].possibleAnswersData)
                                        }

                                        studAnswers.value.sort((a,b) => a.order - b.order)

                                        cardColors.value[i] = answerWasCorrect(studAnswers.value[i].correctAnswerData, studAnswers.value[i].answerData, studAnswers.value[i].possibleAnswersData)

                                        questionList.value[i] = studAnswers.value[i].questionData
                                        possibleAnswers.value[i] = studAnswers.value[i].possibleAnswersData
                                        questionShow.value[i] = false
                                      })
                                })
                          })
                    }
                  })
            })
            .catch(() => {
              console.log('error loading room')
            })
      })


  if(!stompClient.value)
    connect()

})


onUnmounted(() => {

  disconnect()
})
</script>

<template>
  <v-card-title>{{ t('$vuetify.quiz_module.results') }}</v-card-title>

  <v-table class="mt-10">
    <tbody>
      <tr v-for="stud in tableLeaderboard"
          :key="stud"
          class="text-left"
      >
        <td v-if="userStore.user.username === stud.student" class="font-weight-bold">
          {{ stud.student }}
        </td>
        <td v-if="userStore.user.username === stud.student" class="font-weight-bold">
          {{ stud.score }}
        </td>
      </tr>
    </tbody>
  </v-table>


  <v-card-subtitle class="mt-5">
    {{ t('$vuetify.quiz_module.review') }}
  </v-card-subtitle>

  <div v-if="questionList && studAnswers">
    <v-card v-for="(q, index) in questionList"
            :key="q"
            class="mt-3 text-left"
            variant="tonal"
            :color="cardColors[index] ? 'green' : 'red' "
    >
      <v-card-actions>
        <v-row class="align-center" @click="showHideQuestion(index)">
          <v-btn color="blue" variant="text" class="ma-1">
            {{ index + 1 }}. {{ t('$vuetify.quiz_module.question') }}
          </v-btn>
          <v-icon v-if="cardColors[index]">
            mdi-checkbox-marked-circle
          </v-icon>
          <v-icon v-else>
            mdi-close-circle
          </v-icon>
          <v-spacer />
          <v-btn
            :icon="questionShow[index] ? 'mdi-chevron-up' : 'mdi-chevron-down'"
          />
        </v-row>
      </v-card-actions>
      <v-expand-transition>
        <div v-show="questionShow[index]">
          <v-card-title class="pl-1">
            <MdPreview v-model="studAnswers[index].questionData" language="en-US" preview-theme="github"
                       :theme="userStore.darkMode ? 'dark' : 'light'" :no-iconfont="true" :read-only="true" class="px-4 pb-2 pl-10 text-left" :toolbars="[]" />
          </v-card-title>

          <div v-for="(option, oIndex) in studAnswers[index].possibleAnswersData" :key="option" class="mb-8">
            <v-row v-if="studAnswers[index].correctAnswerData.includes(option)" class="mt-16 align-center " style="background-color: green">
              <v-icon v-if="studAnswers[index].answerData && studAnswers[index].answerData.includes(option)" size="25" class="ml-8">
                mdi-radiobox-marked
              </v-icon>
              <v-icon v-else size="25" class="ml-8">
                mdi-radiobox-blank
              </v-icon>
              <MdPreview v-model="studAnswers[index].possibleAnswersData[oIndex]" language="en-US" preview-theme="github"
                         :theme="userStore.darkMode ? 'dark' : 'light'" :no-iconfont="true" :read-only="true" class="px-4 pb-2 pt-3 ml-8 text-left" style="background-color: green; width: 85%" />
              <v-icon size="25" class="ml-8">
                mdi-checkbox-marked-circle
              </v-icon>
            </v-row>

            <v-row v-else-if="studAnswers[index].answerData && studAnswers[index].answerData.includes(option)" class="mt-16 align-center " style="background-color: red">
              <v-icon v-if="studAnswers[index].answerData && studAnswers[index].answerData.includes(option)" size="25" class="ml-8">
                mdi-radiobox-marked
              </v-icon>
              <v-icon v-else size="25" class="ml-8">
                mdi-radiobox-blank
              </v-icon>
              <MdPreview v-model="studAnswers[index].possibleAnswersData[oIndex]" language="en-US" preview-theme="github"
                         :theme="userStore.darkMode ? 'dark' : 'light'" :no-iconfont="true" :read-only="true" class="px-4 pb-2 pt-3 ml-8 text-left" style="background-color: red; width: 85%" />
            </v-row>

            <v-row v-else-if="userStore.darkMode" class="mt-16 align-center " style="background-color: black">
              <v-icon v-if="studAnswers[index].answerData && studAnswers[index].answerData.includes(option)" size="25" class="ml-8">
                mdi-radiobox-marked
              </v-icon>
              <v-icon v-else size="25" class="ml-8">
                mdi-radiobox-blank
              </v-icon>
              <MdPreview v-model="studAnswers[index].possibleAnswersData[oIndex]" language="en-US" preview-theme="github"
                         :theme="userStore.darkMode ? 'dark' : 'light'" :no-iconfont="true" :read-only="true" class="px-4 pb-2 pt-3 ml-8 text-left" style="background-color: black; width: 85%" />
            </v-row>

            <v-row v-else class="mt-16 align-center " style="background-color: white">
              <v-icon v-if="studAnswers[index].answerData && studAnswers[index].answerData.includes(option)" size="25" class="ml-8">
                mdi-radiobox-marked
              </v-icon>
              <v-icon v-else size="25" class="ml-8">
                mdi-radiobox-blank
              </v-icon>
              <MdPreview v-model="studAnswers[index].possibleAnswersData[oIndex]" language="en-US" preview-theme="github"
                         :theme="userStore.darkMode ? 'dark' : 'light'" :no-iconfont="true" :read-only="true" class="px-4 pb-2 pt-3 ml-8 text-left" style="background-color: white; width: 85%" />
            </v-row>
          </div>
        </div>
      </v-expand-transition>
    </v-card>
  </div>

  <v-btn class="mt-10" color="red" @click="leaveQuiz">
    {{ t('$vuetify.quiz_module.leave') }}
  </v-btn>
</template>

<style scoped>

</style>