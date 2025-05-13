<script setup>
import { onMounted, provide, ref, watch, inject} from "vue";

import QuizModuleRoomCreateT from "@/components/modules/quiz/teacherView/QuizModuleRoomCreateT.vue";
import QuizModuleWaitingroomT from "@/components/modules/quiz/teacherView/QuizModuleWaitingroomT.vue";
import QuizModuleQuestionT from "@/components/modules/quiz/teacherView/QuizModuleQuestionT.vue";
import QuizModuleStatsT from "@/components/modules/quiz/teacherView/QuizModuleStatsT.vue";
import QuizModuleResultsT from "@/components/modules/quiz/teacherView/QuizModuleResultsT.vue";
import QuizModuleGateS from "@/components/modules/quiz/studentView/QuizModuleGateS.vue";
import QuizModuleWaitingroomS from "@/components/modules/quiz/studentView/QuizModuleWaitingroomS.vue";
import QuizModuleQuestionS from "@/components/modules/quiz/studentView/QuizModuleQuestionS.vue";
import QuizModuleQuestionEvalS from "@/components/modules/quiz/studentView/QuizModuleQuestionEvalS.vue";
import QuizModuleResultsS from "@/components/modules/quiz/studentView/QuizModuleResultsS.vue";
import {useUserStore} from "@/plugins/store";
import api from "@/service/quizApi";
import {MdPreview} from "md-editor-v3";
import {TAB_MODULE_DETAIL, TAB_MODULE_RATING} from "@/plugins/constants";
import {useLocale} from "vuetify";
import {lessonApi} from "@/service/api";
import TextEditor from "@/components/custom/TextEditor.vue";
const { t } = useLocale()

const props = defineProps(['module', 'lesson', 'teacher', 'course'])

const userStore = useUserStore()

const tabList = inject('tabList')
const selectedTab = inject('selectedTab')
//ws
const stompClient = ref(null)
const connected = ref(false)
const refreshGuard = ref(false)
selectedTab.value = TAB_MODULE_DETAIL
//states
const states = ['entryScreen', 'waitingRoom', 'question', 'stats', 'finalStats']
const tState = ref('')

const sState = ref('')

const quizroomData = ref(null)

const quizData = ref(null)
const numOfQuestions = ref(1)
const currQuestionNum = ref(0)

const currQuestionObj = ref(null)
const questionData = ref( {id: null, possibleAnswersData: null, questionData: ''})
const options = ref([])
const questionText = ref('')
const singleAnswer = ref(false)
const answers = ref([])
const radioAnswers = ref('')

const timeClock = ref(null)
const percentPerSecond = ref(null)


const testAttempt = ref(1)
const totalAttempts = ref()
const roomList = ref([])
const pointList = ref([])
const roomIds = ref([])


const quizQuestionsData = ref([{id: null, order: null, questionData: '',possibleAnswersData: {option1: '', option2: '', option3: '', option4:''}, possibleOptions: [], correctAnswerData: []}])
const possibleOptions = ref([{option1: '', option2: '', option3: '', option4:''}])
const quizQuestionsIds = ref([])

const currStudAnswers = ref([])
const currQuestionShow = ref([])
const currCardColors = ref([])

const editQDialog = ref(false)
const updateQuestionAllert = ref(false)
const pickedQuestion = ref(-1)
const questionToEdit = ref(null)
const optionsToEdit = ref([])
const correctAnswersToEdit = ref([])
const updateQuestionData = ref(null)


provide('moduleData', props.module)
provide('lesson', props.lesson)
provide('teacher', props.teacher)
provide('tState', tState)
provide('teacherStates', states)
provide('quizData', quizData)
provide('numOfQuestions', numOfQuestions)
provide('currQuestionNum', currQuestionNum)
provide('stompClient',stompClient)
provide('connected',connected)
provide('currQuestionObj',currQuestionObj)
provide('questionData',questionData)
provide('options',options)
provide('quizroomData', quizroomData)
provide('teacher', props.teacher)
provide('timeClock', timeClock)
provide('percentPerSecond', percentPerSecond)
provide('refreshGuard', refreshGuard)
provide('questionText', questionText)
provide('singleAnswer',singleAnswer)

const answerWasCorrect = (corr, stud, possible) => {

  if(stud === null)
    return false

  corr = corr.filter(a => possible.includes(a))
  corr.sort()
  stud.sort()
  return Array.isArray(corr) && Array.isArray(stud)
      && corr.length === stud.length && corr.every((val, index) => val === stud[index])

}

const showHideQuestion = (index) => {
  currQuestionShow.value[index] = !currQuestionShow.value[index]
}

function pickQuestionToEdit(index) {

  questionToEdit.value = quizQuestionsData.value[index]
  optionsToEdit.value =  quizQuestionsData.value[index].possibleOptions
  correctAnswersToEdit.value = quizQuestionsData.value[index].correctAnswerData

  pickedQuestion.value = index
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
    lesson: props.lesson
  }

  //console.log(question)

  await api.editQuestionAndReasignPoints(questionToEdit.value.id, roomIds.value[testAttempt.value - 1], question)
      .then((res) => {
        updateQuestionData.value = res
        quizQuestionsData.value[pickedQuestion.value].questionData = questionToEdit.value.questionData
        quizQuestionsData.value[pickedQuestion.value].possibleOptions = optionsToEdit.value
        quizQuestionsData.value[pickedQuestion.value].correctAnswerData = correctAnswersToEdit.value


        api.quizDetailByModule(props.module.id)
            .then(quiz => {

              api.listQuizroomsByStudent(Number(props.teacher), quiz.id)
                  .then(list => {
                    pointList.value[testAttempt.value - 1] = list[testAttempt.value - 1].points

                    questionToEdit.value = null
                    optionsToEdit.value = []
                    correctAnswersToEdit.value = []
                    pickedQuestion.value = -1
                    editQDialog.value = false

                    updateQuestionAllert.value = true
                    setTimeout(() => {updateQuestionAllert.value = false}, 15000)
                  })
                  .catch(error => {console.log(error)})
            })

      })
      .catch(err => {
        console.log(err)
      })
  editQDialog.value = false
}

function removeOption(index) {
 optionsToEdit.value.splice(index, 1)
}

function updateQuestionAndPointsWithoutEdit(index) {

  questionToEdit.value = quizQuestionsData.value[index]
  optionsToEdit.value = quizQuestionsData.value[index].possibleOptions
  correctAnswersToEdit.value = quizQuestionsData.value[index].correctAnswerData
  pickedQuestion.value = index
  updateQuestion()
}


watch(testAttempt, () => {


  for(let qOrder = 0; qOrder < quizQuestionsData.value.length; qOrder++){

    api.studentsAnswerFromQuestionDetail(roomIds.value[testAttempt.value - 1],quizQuestionsData.value[qOrder].id,props.teacher)
        .then((a) => {

          if(quizQuestionsData.value[qOrder].questionType === 'LEGACY'){

            if(a[0]){
              let tmp = []
              for(let i in JSON.parse(a[0].data))
                tmp.push(JSON.parse(a[0].data)[i])

              currStudAnswers.value[qOrder] = tmp
            }
            else
              currStudAnswers.value[qOrder] = null

            currQuestionShow.value[qOrder] = false
            currCardColors.value[qOrder] = answerWasCorrect(quizQuestionsData.value[qOrder].correctAnswerData, currStudAnswers.value[qOrder], quizQuestionsData.value[qOrder].possibleOptions)
          }
          else{
            if(a[0]){
              currStudAnswers.value[qOrder] = JSON.parse(a[0].data)
            }
            else{
              currStudAnswers.value[qOrder] = null
            }

            currQuestionShow.value[qOrder] = false
            currCardColors.value[qOrder] = answerWasCorrect(quizQuestionsData.value[qOrder].correctAnswerData, currStudAnswers.value[qOrder], quizQuestionsData.value[qOrder].possibleOptions)

          }

        })
  }
})

function disconnect() {
  if (stompClient.value) {
    stompClient.value.disconnect();
  }
  connected.value = false;
}

onMounted( async () => {

  tabList.value[TAB_MODULE_DETAIL] = t('$vuetify.quiz_module.quiz')

  selectedTab.value = TAB_MODULE_DETAIL
  if(userStore.isAnyTeacher)
  {
     console.log("lesson quiz - ", props.lesson)
     await lessonApi.lessonDetail(props.lesson)
        .then((lessonData) => {
          if (userStore.isTeacher(lessonData.week.course))
            tabList.value[TAB_MODULE_RATING] = t('$vuetify.tab_rating')
        })
  }
  // props.teacher je defined ak sa ma nacitat nahlad studentovych odpovedi
  if(props.teacher){
      await api.quizDetailByModule(props.module.id)
          .then((quiz) => {
             api.listQuizroomsByStudent(Number(props.teacher), quiz.id)
                .then((list) => {
                  totalAttempts.value = list.length
                  roomList.value = list
                  pointList.value = list.map((it) => it.points)
                  roomList.value.sort((a,b) => a.id - b.id)
                  for(let i = 0; i < totalAttempts.value; i++){
                    roomIds.value[i] = roomList.value[i].id
                  }

                  for (let q = 0; q < quiz.questions.length; q++){
                     api.questionDetail(quiz.questions[q])
                        .then((qData) => {
                          quizQuestionsData.value[q] = qData
                          quizQuestionsData.value[q].order = q
                          quizQuestionsIds.value[q] = qData.id

                           api.questionCorrect(quiz.questions[q])
                              .then((qCorrect) => {
                                quizQuestionsData.value[q].correctAnswerData = JSON.parse(qCorrect.correctAnswerData)
                                quizQuestionsData.value[q].possibleAnswersData = JSON.parse(qData.possibleAnswersData)

                                if (quizQuestionsData.value[q].questionType === 'LEGACY'){
                                  quizQuestionsData.value[q].possibleOptions = [
                                      quizQuestionsData.value[q].possibleAnswersData.option1,
                                      quizQuestionsData.value[q].possibleAnswersData.option2,
                                      quizQuestionsData.value[q].possibleAnswersData.option3,
                                      quizQuestionsData.value[q].possibleAnswersData.option4
                                  ]
                                }
                                else{
                                  quizQuestionsData.value[q].possibleOptions = qData.possibleAnswersData
                                }
                                possibleOptions.value.push(quizQuestionsData.value[q].possibleAnswersData)
                                quizQuestionsData.value.sort((a,b) => a.order - b.order)

                                if(list.length > 0){
                                  if(q === quiz.questions.length - 1){

                                    for(let qOrder = 0; qOrder < quizQuestionsData.value.length; qOrder++){

                                      api.studentsAnswerFromQuestionDetail(list[0].id,quizQuestionsData.value[qOrder].id,props.teacher)
                                          .then((a) => {
                                            if(quizQuestionsData.value[qOrder].questionType === 'LEGACY'){

                                              if(a[0]){
                                                let tmp = []
                                                for(let i in JSON.parse(a[0].data))
                                                  tmp.push(JSON.parse(a[0].data)[i])

                                                currStudAnswers.value[qOrder] = tmp
                                              }
                                              else
                                                currStudAnswers.value[qOrder] = null

                                              currQuestionShow.value[qOrder] = false
                                              currCardColors.value[qOrder] = answerWasCorrect(quizQuestionsData.value[qOrder].correctAnswerData, currStudAnswers.value[qOrder], quizQuestionsData.value[qOrder].possibleOptions)
                                            }
                                            else{
                                              if(a[0]){
                                                currStudAnswers.value[qOrder] = JSON.parse(a[0].data)
                                              }
                                              else{
                                                currStudAnswers.value[qOrder] = null
                                              }

                                              currQuestionShow.value[qOrder] = false
                                              currCardColors.value[qOrder] = answerWasCorrect(quizQuestionsData.value[qOrder].correctAnswerData, currStudAnswers.value[qOrder], quizQuestionsData.value[qOrder].possibleOptions)

                                            }
                                          })
                                    }

                                  }
                                }

                              })
                        })
                  }

                })
                .catch((err) => {
                  console.log(err)
                })
          })


    }
  else{
    if(userStore.isAnyTeacher){

      api.getLastQuizroomByStudent(userStore.user.id, props.module.id)
          .then((lastRoom)=> {

            if(lastRoom.id !== -1){
              let roomId = lastRoom.id
              api.quizroomDetail(roomId)
                  .then((room) => {

                    if(room.roomState){
                      api.quizDetailByModule(props.module.id)
                          .then((quiz) => {

                            if(room.quiz === quiz.id){
                              api.quizroomStudentDetail(roomId,userStore.user.id)
                                  .then(() => {
                                    quizData.value = quiz
                                    tState.value = room.quizState
                                  })
                                  .catch(() => {
                                    console.log('not found')
                                  })
                            }
                            else{
                              tState.value = states[0]
                            }
                          })
                          .catch(() => {
                            console.log('quiz not found')
                          })
                    }
                    else{
                      disconnect()
                      tState.value = states[0]
                    }

                  })
                  .catch(() => {
                    console.log('error fetching quizroom')
                  })
            }
            else{

              tState.value = states[0]
            }
          })

      // ak je ucastnik prebiehajuceho kvizu


    }
    else{

      api.getLastQuizroomByStudent(userStore.user.id, props.module.id)
          .then((lastRoom) => {

            if(lastRoom.id !== -1){
              let roomId = lastRoom.id
              api.quizroomDetail(roomId)
                  .then((room) => {

                    if(room.roomState){
                      api.quizDetailByModule(props.module.id)
                          .then((quiz) => {
                            if(room.quiz === quiz.id){
                              api.quizroomStudentDetail(roomId,userStore.user.id)
                                  .then(() => {
                                    sState.value = room.quizState
                                  })
                                  .catch(() => {
                                    console.log('not found')
                                  })
                            }
                            else{
                              sState.value = states[0]
                            }
                          })
                          .catch(() => {
                            console.log('quiz not found')
                          })
                    }
                    else{
                      disconnect()
                      sState.value = states[0]
                    }


                  })
                  .catch(() => {
                    console.log('error fetching quizroom')
                  })
            }
            else{
              sState.value = states[0]
            }
          })


      provide('module', props.module)
      provide('lesson', props.lesson)
      provide('studentStates', states)
      provide('sState', sState)
      provide('currQuestionNum', currQuestionNum)
      provide('numOfQuestions', numOfQuestions)
      provide('quizroomData', quizroomData)
      provide('quizData', quizData)
      provide('user',props.teacher)
      provide('stompClient',stompClient)
      provide('connected',connected)
      provide('currQuestionObj',currQuestionObj)
      provide('questionData',questionData)
      provide('options',options)
      provide('answers',answers)
      provide('radioAnswers',radioAnswers)
      provide('lesson', props.lesson)
      provide('timeClock', timeClock)
      provide('percentPerSecond', percentPerSecond)
      provide('refreshGuard', refreshGuard)
      provide('questionText', questionText)
      provide('singleAnswer',singleAnswer)


    }
  }


})

</script>


<template>
  <v-window-item :key="TAB_MODULE_DETAIL" :transition="false" :reverse-transition="false" :value="TAB_MODULE_DETAIL" class="mt-4">
    <div v-if="props.teacher">
      <div v-if="Number(userStore.user.id) === Number(props.teacher)">
        <v-label style="font-style: italic">
          Učitel na otázky neodpovídá, jen je klade.
        </v-label>
      </div>
      <div v-else-if="roomList && roomList.length > 0">
        <template v-if="quizQuestionsData">
          <v-card-title class="text-center">
            {{ props.module.name }}
          </v-card-title>

          <v-dialog v-model="editQDialog">
            <v-card v-if="pickedQuestion === -1">
              <v-card-title>{{ t('$vuetify.quiz_module.room_id') }}: {{ roomIds[testAttempt - 1] }}</v-card-title>
              <div>
                <v-card v-for="(q, index) in quizQuestionsData"
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

          <v-row v-if="pointList">
            <h4 class="pa-4">
              {{ t('$vuetify.quiz_module.points') }}: {{ pointList[testAttempt - 1] }}
            </h4>
            <v-spacer />
            <h4 class="pa-4">
              {{ ((pointList[testAttempt - 1] * 100 / quizQuestionsData.length) >= props.module.minPercent) ? t('$vuetify.quiz_module.student_passed') : t('$vuetify.quiz_module.student_didnt_pass') }}
            </h4>
          </v-row>
          <v-card v-for="(q, index) in quizQuestionsData"
                  :key="q"
                  class="mt-3 text-left"
                  variant="tonal"
                  :color="currCardColors[index] ? 'green' : 'red' ">
            <v-card-actions>
              <v-row class="align-center" @click="showHideQuestion(index)">
                <v-btn color="blue" variant="text" class="ma-1">
                  {{ index + 1 }}. {{ t('$vuetify.quiz_module.question') }}
                </v-btn>
                <v-icon v-if="currCardColors[index]">
                  mdi-checkbox-marked-circle
                </v-icon>
                <v-icon v-else>
                  mdi-close-circle
                </v-icon>
                <v-spacer />
                <v-btn
                  :icon="currQuestionShow[index] ? 'mdi-chevron-up' : 'mdi-chevron-down'"
                />
              </v-row>
            </v-card-actions>
            <v-expand-transition>
              <div v-show="currQuestionShow[index]">
                <div v-if="quizQuestionsData[index]">
                  <v-card-title class="pl-1">
                    <MdPreview v-model="quizQuestionsData[index].questionData" language="en-US" preview-theme="github"
                               :theme="userStore.darkMode ? 'dark' : 'light'" :no-iconfont="true" :read-only="true" class="px-4 pb-2 pl-10 text-left" :toolbars="[]" />
                  </v-card-title>

                  <div v-if="quizQuestionsData[index].correctAnswerData && quizQuestionsData[index].possibleAnswersData && quizQuestionsData[index].possibleOptions">
                    <div v-for="(option, oIndex) in quizQuestionsData[index].possibleOptions" :key="option" class="mb-4">
                      <v-row v-if="quizQuestionsData[index].correctAnswerData.includes(option)" class="mt-16 align-center" style="background-color: green">
                        <v-icon v-if="currStudAnswers[index] && currStudAnswers[index].includes(option)" size="25" class="ml-8" color="black">
                          mdi-radiobox-marked
                        </v-icon>
                        <v-icon v-else size="25" class="ml-8" color="black">
                          mdi-radiobox-blank
                        </v-icon>
                        <MdPreview v-model="quizQuestionsData[index].possibleOptions[oIndex]" language="en-US" preview-theme="github"
                                   :theme="userStore.darkMode ? 'dark' : 'light'" :no-iconfont="true" :read-only="true" class="px-4 pb-2 pt-3 ml-8 text-left" style="background-color: green; width: 85%" />
                        <v-icon size="25" class="ml-8" color="black">
                          mdi-checkbox-marked-circle
                        </v-icon>
                      </v-row>

                      <v-row v-else-if="currStudAnswers[index] && currStudAnswers[index].includes(option)" class="mt-16 align-center " style="background-color: red">
                        <v-icon v-if="currStudAnswers[index] && currStudAnswers[index].includes(option)" size="25" class="ml-8" color="black">
                          mdi-radiobox-marked
                        </v-icon>
                        <v-icon v-else size="25" class="ml-8" color="black">
                          mdi-radiobox-blank
                        </v-icon>
                        <MdPreview v-model="quizQuestionsData[index].possibleOptions[oIndex]" language="en-US" preview-theme="github"
                                   :theme="userStore.darkMode ? 'dark' : 'light'" :no-iconfont="true" :read-only="true" class="px-4 pb-2 pt-3 ml-8 text-left" style="background-color: red; width: 85%" />
                      </v-row>

                      <v-row v-else-if="userStore.darkMode" class="mt-16 align-center " style="background-color: black">
                        <v-icon v-if="currStudAnswers[index] && currStudAnswers[index].includes(option)" size="25" class="ml-8" color="black">
                          mdi-radiobox-marked
                        </v-icon>
                        <v-icon v-else size="25" class="ml-8" color="black">
                          mdi-radiobox-blank
                        </v-icon>
                        <MdPreview v-model="quizQuestionsData[index].possibleOptions[oIndex]" language="en-US" preview-theme="github"
                                   :theme="userStore.darkMode ? 'dark' : 'light'" :no-iconfont="true" :read-only="true" class="px-4 pb-2 pt-3 ml-8 text-left" style="background-color: black; width: 85%" />
                      </v-row>

                      <v-row v-else class="mt-16 align-center " style="background-color: white">
                        <v-icon v-if="currStudAnswers[index] && currStudAnswers[index].includes(option)" size="25" class="ml-8" color="black">
                          mdi-radiobox-marked
                        </v-icon>
                        <v-icon v-else size="25" class="ml-8" color="black">
                          mdi-radiobox-blank
                        </v-icon>
                        <MdPreview v-model="quizQuestionsData[index].possibleOptions[oIndex]" language="en-US" preview-theme="github"
                                   :theme="userStore.darkMode ? 'dark' : 'light'" :no-iconfont="true" :read-only="true" class="px-4 pb-2 pt-3 ml-8 text-left" style="background-color: white; width: 85%" />
                      </v-row>
                    </div>
                  </div>
                </div>
              </div>
            </v-expand-transition>
          </v-card>

          <v-pagination v-model="testAttempt" :length="totalAttempts" rounded="circle" total-visible="3" />
        </template>
      </div>
      <div v-else class="align-center text-center">
        <v-card-title>Student tento kvíz nevyplňoval</v-card-title>
      </div>
    </div>
    <div v-else>
      <template v-if="userStore.isTeacher(props.course)">
        <div v-if="tState === states[0]">
          <QuizModuleRoomCreateT />
        </div>
        <div v-else-if="tState === states[1]" class="text-center">
          <QuizModuleWaitingroomT />
        </div>
        <div v-else-if="tState === states[2]" class="text-center">
          <h3 v-if="quizroomData" class="mb-10 mt-8">
            {{ t('$vuetify.quiz_module.room_password') }} : {{ quizroomData.roomPassword }}
          </h3>
          <QuizModuleQuestionT />
        </div>
        <div v-else-if="tState === states[3]" class="text-center">
          <h3 v-if="quizroomData" class="mb-10 mt-8">
            {{ t('$vuetify.quiz_module.room_password') }} : {{ quizroomData.roomPassword }}
          </h3>
          <QuizModuleStatsT />
        </div>
        <div v-else-if="tState === states[4]" class="text-center">
          <QuizModuleResultsT />
        </div>
      </template>
      <template v-else>
        <div v-if="sState === states[0]" class="text-center">
          <QuizModuleGateS />
        </div>
        <div v-else-if="sState === states[1]" class="text-center">
          <QuizModuleWaitingroomS />
        </div>
        <div v-else-if="sState === states[2]" class="text-center" @copy.prevent>
          <QuizModuleQuestionS />
        </div>
        <div v-else-if="sState === states[3]" class="text-center">
          <QuizModuleQuestionEvalS />
        </div>
        <div v-else-if="sState === states[4]" class="text-center">
          <QuizModuleResultsS />
        </div>
      </template>
    </div>
  </v-window-item>
</template>

<style scoped>

</style>
