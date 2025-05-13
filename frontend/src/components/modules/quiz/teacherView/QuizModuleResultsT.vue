<script setup>
import {inject, onMounted, onUnmounted, ref} from "vue";
import {studentModuleApi} from "@/service/api";
import api from "@/service/quizApi";
import Stomp from "webstomp-client";
import SockJS from "sockjs-client";
import axios from "axios";
import {useUserStore} from "@/plugins/store";
import {anonEmoji} from "@/plugins/constants"
import {useLocale} from "vuetify";
import TextEditor from "@/components/custom/TextEditor.vue";
import {MdPreview} from "md-editor-v3";
const { t } = useLocale()

const userStore = useUserStore()

const module = inject('moduleData')
const lesson = inject('lesson')
const tState = inject('tState')
const teacherStates = inject('teacherStates')
const currQuestionNum = inject('currQuestionNum')
const quizroomData = inject('quizroomData')
const quizData = inject('quizData')

let stompClient = inject('stompClient')
const connected = inject('connected')
const questionData = inject('questionData')
const options = inject('options')

const error = ref(null)
const leaderboard = ref([])
const tableLeaderboard = ref([])

const editQDialog = ref(false)
const allQuizQuestionsData = ref([])
const pickedQuestion = ref(-1)
const questionToEdit = ref(null)
const optionsToEdit = ref([])
const correctAnswersToEdit = ref([])
const updateQuestionAllert = ref(false)
const updateQuestionData = ref(null)

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

function pickQuestionToEdit(index) {

  questionToEdit.value = allQuizQuestionsData.value[index]
  optionsToEdit.value = allQuizQuestionsData.value[index].possibleAnswersData
  correctAnswersToEdit.value = allQuizQuestionsData.value[index].correctAnswerData

  pickedQuestion.value = index
}
function removeOption(index) {
  optionsToEdit.value.splice(index, 1)
}

function updateQuestionAndPointsWithoutEdit(index) {

  questionToEdit.value = allQuizQuestionsData.value[index]
  optionsToEdit.value = allQuizQuestionsData.value[index].possibleAnswersData
  correctAnswersToEdit.value = allQuizQuestionsData.value[index].correctAnswerData
  pickedQuestion.value = index
  updateQuestion()
}
const updateQuestion = async () => {

  let optionsString = ''

  if(questionToEdit.value.type === 'LEGACY'){
    optionsString = JSON.stringify({"option1": optionsToEdit.value[0],
      "option2": optionsToEdit.value[1],
      "option3": optionsToEdit.value[2],
      "option4": optionsToEdit.value[3] })
  }
  else{
    optionsString = JSON.stringify(optionsToEdit.value)
  }

  const question = {
    questionData: questionToEdit.value.questionData,
    possibleAnswersData: optionsString,
    correctAnswerData: JSON.stringify(correctAnswersToEdit.value),
    questionType : questionToEdit.value.questionType,
    singleAnswer : correctAnswersToEdit.value.filter((o) => {return optionsToEdit.value.indexOf(o) !== -1}).length === 1,
    lesson: lesson
  }


  api.editQuestionAndReasignPoints(questionToEdit.value.id, quizroomData.value.id, question)
      .then((res) => {
        updateQuestionData.value = res
        allQuizQuestionsData.value[pickedQuestion.value].questionData = questionToEdit.value.questionData
        allQuizQuestionsData.value[pickedQuestion.value].possibleAnswersData = optionsToEdit.value
        allQuizQuestionsData.value[pickedQuestion.value].correctAnswerData = correctAnswersToEdit.value
        questionToEdit.value = null
        optionsToEdit.value = []
        correctAnswersToEdit.value = []
        pickedQuestion.value = -1

        api.getLastQuizroomByStudent(userStore.user.id, module.id)
            .then((lastRoom) => {
              let roomId = lastRoom.id

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
                  })
                  .catch(() => {
                    console.log('error loading room')
                  })

            })

         editQDialog.value = false

        updateQuestionAllert.value = true
        setTimeout(() => {updateQuestionAllert.value = false}, 15000)
      })
      .catch(err => {
        console.log(err)
      })
  editQDialog.value = false
}

//ukonci kviz, hodi state do povodneho stavu, teda sa vratime na obrazovku s tvorenim roomky
const endQuiz = () => {

  const quizroom = {
    roomState: false,
    lesson: lesson
  }

  api.editQuizroom(quizroomData.value.id,quizroom)


  currQuestionNum.value = 0
  tState.value = teacherStates[0]
}

onMounted(async () => {

  if(!stompClient.value)
    connect()

  api.getLastQuizroomByStudent(userStore.user.id, module.id)
      .then((lastRoom) => {
        let roomId = lastRoom.id

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
            })
            .catch(() => {
              console.log('error loading room')
            })

      })


  for(let i =0; i < quizData.value.questions.length; i++) {
    api.questionDetail(quizData.value.questions[i])
        .then(async (q) => {

          await api.questionCorrect(q.id)
              .then(qCorr => {

                allQuizQuestionsData.value[i] = q

                if(Array.isArray(JSON.parse(q.possibleAnswersData))){
                  allQuizQuestionsData.value[i].possibleAnswersData = JSON.parse(q.possibleAnswersData)
                }
                else{
                  allQuizQuestionsData.value[i].possibleAnswersData = [
                    JSON.parse(q.possibleAnswersData).option1, JSON.parse(q.possibleAnswersData).option2,
                    JSON.parse(q.possibleAnswersData).option3, JSON.parse(q.possibleAnswersData).option4 ]
                }
                allQuizQuestionsData.value[i].type = q.questionType
                allQuizQuestionsData.value[i].id = q.id
                allQuizQuestionsData.value[i].questionData = q.questionData

                allQuizQuestionsData.value[i].correctAnswerData = JSON.parse(qCorr.correctAnswerData)

              })
        })
        .catch((err) => {
          console.log(err)
        })
  }

})

onUnmounted(() => {
  const teacherModule = {
    allowedShow: module.allowedShow ?? false,
    data : ""
  }
  studentModuleApi.putStudentModule(lesson,module.id, teacherModule)
      .then(() => {
        module.studentData = ""
      })
  disconnect()
})

</script>


<template>
  <v-dialog v-model="editQDialog">
    <v-card v-if="pickedQuestion === -1">
      <div>
        <v-card v-for="(q, index) in allQuizQuestionsData"
                :key="index"
                variant="outlined"
                class="ma-8" rounded="lg">
          <v-row class="ma-4">
            <v-card-title>
              {{ t('$vuetify.quiz_module.question') }} {{ index + 1 }}
            </v-card-title>
            <v-spacer />
            <v-btn color="green" @click="pickQuestionToEdit(index)">
              {{ t('$vuetify.action_edit') }}
            </v-btn>
          </v-row>
          <v-row class="ma-4">
            <v-card-title>
              {{ q.questionData }}
            </v-card-title>
            <v-spacer />
            <v-btn color="blue" @click="updateQuestionAndPointsWithoutEdit(index)">
              {{ t('$vuetify.quiz_module.reasign_points') }}
            </v-btn>
          </v-row>
        </v-card>
      </div>
    </v-card>
    <v-card v-else>
      <v-row class="mt-1 mr-1 justify-end">
        <v-card-title class="ml-4 mb-8 mt-4">
          {{ t('$vuetify.quiz_module.edit_question_dialog_heading') }}
        </v-card-title>
        <v-spacer />
        <v-btn elevation="0" icon="mdi-close" class="mr-4 mb-8 mt-4" @click="pickedQuestion = -1">
          <v-icon>mdi-close</v-icon>
          <v-tooltip
            activator="parent"
            location="bottom"
          >
            {{ t('$vuetify.quiz_module.go_back') }}
          </v-tooltip>
        </v-btn>
      </v-row>

      <div v-if="questionToEdit">
        <MdPreview v-model="questionToEdit.questionData" language="en-US" preview-theme="github"
                   :theme="userStore.darkMode ? 'dark' : 'light'" :no-iconfont="true" :read-only="true" class="px-4 pb-2 pl-10 text-left" :toolbars="[]" />

        <v-row v-for="(option, index) in optionsToEdit"
               :key="index"
               class="mt-15 ml-1 mb-8 align-center">
          <TextEditor v-model="optionsToEdit[index]" class=" max-height-tab mb-4 assignment-editor" :placeholder="t('$vuetify.quiz_module.option')" style="width: 80%" :editable="!(optionsToEdit.length === 2 && optionsToEdit[0] === 'True' && optionsToEdit[1] === 'False')" />
          <v-checkbox v-model="correctAnswersToEdit" :disabled="optionsToEdit[index] === '' || !optionsToEdit[index]" class="ml-5" :label="t('$vuetify.quiz_module.correct_option')" :value="option" />
          <v-btn color="red" variant="text" class="mr-8" icon="mdi-delete" @click="removeOption(index)">
            <v-icon>mdi-delete</v-icon>
            <v-tooltip
              activator="parent"
              location="bottom"
            >
              {{ t('$vuetify.action_delete') }}
            </v-tooltip>
          </v-btn>
        </v-row>

        <v-btn class="ma-4 ml-8 mr-8" color="blue" @click="updateQuestion()">
          {{ t('$vuetify.dialog_save') }}
        </v-btn>
      </div>
    </v-card>
  </v-dialog>


  <v-row class="justify-end mr-4 mb-4">
    <v-btn color="blue" @click="editQDialog = true">
      {{ t('$vuetify.quiz_module.fix_eval') }}
    </v-btn>
  </v-row>


  <v-alert
    v-if="updateQuestionAllert"
    class="ml-3 mt-3 mb-3"
    variant="tonal"
    type="success"
    :title="t('$vuetify.quiz_module.question_edited_allert_heading')">
    <div v-if="updateQuestionData">
      {{ t('$vuetify.quiz_module.question_edited_allert_text') }}
      <div v-for="s in updateQuestionData.studentsAdd" :key="s">
        {{ s }} (+)
      </div>
      <div v-for="s in updateQuestionData.studentsSub" :key="s">
        {{ s }} (-)
      </div>
    </div>
  </v-alert>

  <v-card-title>{{ t('$vuetify.quiz_module.results') }}</v-card-title>


  <v-table class="mt-10">
    <tbody>
      <tr v-for="stud in tableLeaderboard"
          :key="stud"
          class="text-left">
        <td v-if="userStore.user.username !== stud.student" class="font-weight-bold">
          {{ userStore.anonymous ? anonEmoji(stud.student) : stud.student }}
        </td>
        <td v-if="userStore.user.username !== stud.student" class="font-weight-bold">
          {{ stud.score }}
        </td>
      </tr>
    </tbody>
  </v-table>

  <!--<v-card-subtitle class="font-weight-bold">Materiály</v-card-subtitle>-->

  <v-btn class="mt-10" color="red" @click="endQuiz">
    {{ t('$vuetify.quiz_module.end_quiz') }}
  </v-btn>
</template>

<style scoped>

</style>