<script setup>
import {inject, provide, onMounted, ref, computed} from 'vue'
import api from '@/service/quizApi'
import TextEditor from "@/components/custom/TextEditor.vue";
import {useUserStore} from "@/plugins/store";
import draggable from 'vuedraggable';
import {
  MODULE_EDIT_ITEMS,
  MODULE_EDIT_QUIZ_CREATE
} from "@/plugins/constants";
import { useLocale } from 'vuetify'
import QuestionTrueFalse from "@/components/modules/questionTypes/QuestionTrueFalse.vue";
import QuestionMultichoice from "@/components/modules/questionTypes/QuestionMultichoice.vue";
import QuestionFillText from "@/components/modules/questionTypes/QuestionFillText.vue";
import QuestionOpen from "@/components/modules/questionTypes/QuestionOpen.vue";
import QuestionSort from "@/components/modules/questionTypes/QuestionSort.vue";
import QuestionConnect from "@/components/modules/questionTypes/QuestionConnect.vue";
import {onBeforeRouteLeave} from "vue-router";
import QuestionList from "@/components/modules/QuestionList.vue";
import quizApi from "@/service/quizApi";
import DeleteDialog from "@/components/custom/DeleteDialog.vue";
const { t } = useLocale()


const userStore = useUserStore()

const props = defineProps(['moduleId', 'readOnly','reload', 'topics', 'subjects'])

const createEditCallback  = inject('createEditCallback', () => {})

const routerNext = inject('routerNext')
const unsavedChangesDialog = inject('unsavedChangesDialog')

const moduleEditItem = inject('moduleEditItem')
const moduleEditTabList = inject('moduleEditTabList')

const translate = (key) => t(`$vuetify.module_edit_${key}`)

const selectedTopics = ref([])
const selectedSubjects = ref([])
const addQuestionDialog = ref(false)
const qAllert = ref('')
const editQDialog = ref(false)
const createQDialog = ref(false)
const showQType = ref(false)
const error  = ref(null)
//const selected  = ref(null)
const editSelected = ref(null)
const editShow = ref("one")

const quizQuestions = ref([])
const quizName = ref('')
const quizAttempts = ref(1)

//data from api
const questions  = ref([])        //all data of all questions of current user
const questionsData  = ref([])    //questionText of all questions
const questionTopics = ref([])
const questionSubjects = ref([])

//data to api
//createQ
const questionData  = ref('')
const option1  = ref('')
const option2  = ref('')
const option3  = ref('')
const option4  = ref('')
const options = ref([])
const correctAnswers = ref([])
const optionOrder = ref([])
const statements = ref([])
const timeLimit = ref(15)
const explanation = ref('')
const selectedQType = ref('MULTICHOICE')
const questionTypes = ref(
    [ 'TRUEFALSE', 'MULTICHOICE',
      //'OPEN', 'FILLTEXT',
      //'SORT', 'CONNECT'
    ]
)

//editQ
const questionToEdit = ref(null)
//const optionsEdit = ref([])
const questionDataEdit  = ref('')
const option1Edit  = ref('')
const option2Edit  = ref('')
const option3Edit  = ref('')
const option4Edit  = ref('')
const correctAnswersEdit = ref([])
const timeLimitEdit = ref(-1)
const explanationEdit = ref('')
const idEdit = ref(0)


const oldQuizData = ref(null)

const questionListState = ref({
  searchId: '',
  searchText: '',
  searchAuthors: [userStore.user.username],
  searchTypes: [],
  searchTopics: [],
  searchSubjects: []
});

provide('correctAnswers', correctAnswers)
provide('options', options)
provide('optionOrder', optionOrder)
provide('statements', statements)
provide('quizQuestions', quizQuestions)


createEditCallback.value = async (module) => {

  if( !quizQuestions.value || !quizAttempts.value )
    return

  const quiz = {
    name: module.name,
    numOfQuestions : quizQuestions.value.length,
    numOfAttempts: quizAttempts.value,
    questions: quizQuestions.value.map((q) => q.id),
    moduleId: module.id
  }

  if(props.moduleId){
    await api.editQuiz(oldQuizData.value.id, quiz)
        .catch((err) => {
          console.log(err)
          error.value = `Chyba při uprave: ${err.code}`
        })
  }
  else{
    await api.createQuiz(quiz)
        .catch((err) => {
          console.log(err)
          error.value = `Chyba při vytváření: ${err.code}`
        })
  }


}

const reload2 = async () => {
  if (props.moduleId) {
    api.quizDetailByModule(props.moduleId)
        .then((result) => {
          oldQuizData.value = result
          oldQuizData.value.questions.forEach(
              (q) => {
                api.questionDetail(q)
                    .then((qRes) => {
                      api.questionCorrect(q)
                          .then((qCorrect) => {
                            let hasValue = quizQuestions.value.some(que => que.id === qRes.id)
                            if (!hasValue){
                              quizQuestions.value.push({id: qRes.id,
                                questionData: qRes.questionData,
                                possibleAnswersData: qRes.possibleAnswersData,
                                timeLimit : qRes.timeLimit,
                                questionType: qRes.questionType,
                                author: qRes.author,
                                correctAnswerData : qCorrect.correctAnswerData,
                                explanation: qCorrect.explanation,
                                orderNum: oldQuizData.value.questions.indexOf(qRes.id)} )
                            }
                            quizQuestions.value.sort((a,b) => a.orderNum - b.orderNum)
                          })
                          .catch(() => {
                            console.log('Error fetching question data' + q)
                          })
                    })
                    .catch((err) => {
                      console.log(err)
                      error.value = `Chyba při načítání: ${err.code}`
                    })
              }
          )

          quizName.value = oldQuizData.value.name
          quizAttempts.value = oldQuizData.value.numOfAttempts
        })
        .catch((err) => {
          console.log(err)
          error.value = `Chyba při načítání: ${err.code}`
        })
    props.reload()
  }
}

const getQuestionToEdit = () => {
  api.getQuestionTopics(questionToEdit.value.id).then((result) =>
  { questionTopics.value = (result.map((it) => it.id));
    selectedTopics.value = questionTopics.value})
  api.getQuestionSubjects(questionToEdit.value.id).then((result) =>
  { questionSubjects.value = (result.map((it) => it.id));
    selectedSubjects.value = questionSubjects.value})
  if(!editSelected.value)
    return

  correctAnswersEdit.value = JSON.parse(questionToEdit.value.correctAnswerData)
  option1Edit.value = JSON.parse(questionToEdit.value.possibleAnswersData).option1
  option2Edit.value = JSON.parse(questionToEdit.value.possibleAnswersData).option2
  option3Edit.value = JSON.parse(questionToEdit.value.possibleAnswersData).option3
  option4Edit.value = JSON.parse(questionToEdit.value.possibleAnswersData).option4
  editShow.value = true
}

const addTopicToQuestion = async (id) => {
  if (questionTopics.value.indexOf(id) === -1) {
    api.addQuestionTopic(questionToEdit.value.id, id).catch(
        (err) => {
          error.value = err.code
        }
    )
  }
  else {
    questionTopics.value.splice(questionTopics.value.indexOf(id), 1)
  }
}

const addSubjectToQuestion = async (id) => {
  if (questionSubjects.value.indexOf(id) === -1) {
    api.addQuestionSubject(questionToEdit.value.id, id).catch(
        (err) => {
          error.value = err.code
        }
    )
  }
  else {
    questionSubjects.value.splice(questionSubjects.value.indexOf(id), 1)
  }
}

const deleteTopicFromQuestion = async (id) => {
  api.deleteQuestionTopic(questionToEdit.value.id, id).catch(
      (err) => error.value = err.code
  )
}

const deleteSubjectFromQuestion = async (id) => {
  api.deleteQuestionSubject(questionToEdit.value.id, id).catch(
      (err) => error.value = err.code
  )
}

const editQuestion = async () => {
  if(questionToEdit.value.questionType === 'LEGACY'){

    //console.log("options: ", option1Edit.value, option2Edit.value, option3Edit.value, option4Edit.value)
    //console.log("correct: ", correctAnswersEdit.value)


    if (!questionToEdit.value.questionData || !option1Edit.value || !option2Edit.value ||
        !option3Edit.value || !option4Edit.value || correctAnswersEdit.value.length === 0 || !questionToEdit.value.timeLimit)
      return

    if(correctAnswersEdit.value.filter((a) => {
      return a === option1Edit.value ||
          a === option2Edit.value ||
          a === option3Edit.value ||
          a === option4Edit.value }).length === 0)
      return

    let jsonCorrectAnswers = '['

    for(let i = 0; i < correctAnswersEdit.value.length; i++){
      jsonCorrectAnswers = jsonCorrectAnswers + JSON.stringify(correctAnswersEdit.value[i])
      if((i + 1) !== correctAnswersEdit.value.length)
        jsonCorrectAnswers = jsonCorrectAnswers + ','
    }

    jsonCorrectAnswers = jsonCorrectAnswers + ']'

    let jsonOptions = JSON.stringify({"option1": option1Edit.value,
      "option2": option2Edit.value,
      "option3": option3Edit.value,
      "option4": option4Edit.value })

    let singleAnswer = (correctAnswersEdit.value.filter((a) => {
      return a === option1Edit.value ||
          a === option2Edit.value ||
          a === option3Edit.value ||
          a === option4Edit.value }).length  === 1)


    const question = {
      questionData: questionToEdit.value.questionData,
      possibleAnswersData :  jsonOptions,
      correctAnswerData: jsonCorrectAnswers,
      timeLimit: questionToEdit.value.timeLimit,
      explanation: questionToEdit.value.explanation,
      author: questionToEdit.value.author,
      singleAnswer: singleAnswer,
      questionType: "LEGACY"
    }

    api.editQuestion(questionToEdit.value.id, question)
        .then((editedQ) => {

          api.listQuestionsByUsername(userStore.user.username)
              .then(() => {
                api.questionCorrect(editedQ.id)
                    .then((result) => {
                      let oldQIndex = questionsData.value.indexOf(editSelected.value)
                      //console.log(oldQIndex)
                      questions.value[oldQIndex] = editedQ
                      questionsData.value[oldQIndex] = editedQ.questionData

                      questions.value[oldQIndex].correctAnswerData = result.correctAnswerData
                      questions.value[oldQIndex].explanation = result.explanation

                      let quizQIndex = quizQuestions.value.map(q => q.id).indexOf(questionToEdit.value.id)
                      if(quizQIndex > -1)
                        quizQuestions.value[quizQIndex] = questions.value[oldQIndex]
                      else
                        quizQuestions.value.push(questions.value[oldQIndex])

                      //quizQuestions.value.push(questions.value[qIndex])

                      questionDataEdit.value  = ''
                      option1Edit.value  = ''
                      option2Edit.value  = ''
                      option3Edit.value  = ''
                      option4Edit.value  = ''
                      correctAnswersEdit.value = []
                      correctAnswers.value = []
                      options.value = []
                      timeLimitEdit.value = -1
                      explanationEdit.value = ''
                      idEdit.value = 0
                      editSelected.value = null

                      editShow.value = false

                      editQDialog.value = false;

                      qAllert.value = 'EDITED'
                      setTimeout(() => {qAllert.value = ''}, 3000)
                    })
                    .catch(() => {
                      console.log('Error fetching question')
                    })

              })


        })
        .catch(() => {
          console.log('Error editing question')
        })

  }
  else{


    if(!questionToEdit.value.questionData || !options.value || options.value.length === 0 ||
        correctAnswers.value.length === 0 || !questionToEdit.value.timeLimit)
      return

    if(correctAnswers.value.filter((a) => {return options.value.indexOf(a) !== -1}).length === 0)
      return

    let singleAnswer = (correctAnswers.value.filter((a) => {return options.value.indexOf(a) !== -1}).length === 1)


    const question = {
      questionData: questionToEdit.value.questionData,
      possibleAnswersData :  JSON.stringify(options.value),
      correctAnswerData: JSON.stringify(correctAnswers.value),
      timeLimit: questionToEdit.value.timeLimit,
      explanation: questionToEdit.value.explanation,
      author: questionToEdit.value.author,
      singleAnswer: singleAnswer,
      questionType: questionToEdit.value.questionType
    }

    // console.log(question)

    api.editQuestion(questionToEdit.value.id, question)
        .then((editedQ) => {

          api.listQuestionsByUsername(userStore.user.username)
              .then(() => {
                api.questionCorrect(editedQ.id)
                    .then((result) => {
                      let oldQIndex = questionsData.value.indexOf(editSelected.value)
                      //console.log(oldQIndex)
                      questions.value[oldQIndex] = editedQ
                      questionsData.value[oldQIndex] = editedQ.questionData

                      questions.value[oldQIndex].correctAnswerData = result.correctAnswerData
                      questions.value[oldQIndex].explanation = result.explanation

                      let quizQIndex = quizQuestions.value.map(q => q.id).indexOf(questionToEdit.value.id)
                      if(quizQIndex > -1)
                        quizQuestions.value[quizQIndex] = questions.value[oldQIndex]
                      else
                        quizQuestions.value.push(questions.value[oldQIndex])


                      questionDataEdit.value  = ''
                      option1Edit.value  = ''
                      option2Edit.value  = ''
                      option3Edit.value  = ''
                      option4Edit.value  = ''
                      correctAnswersEdit.value = []
                      correctAnswers.value = []

                      options.value = []
                      timeLimitEdit.value = -1
                      explanationEdit.value = ''
                      idEdit.value = 0
                      editSelected.value = null

                      editShow.value = false

                      editQDialog.value = false;

                      qAllert.value = 'EDITED'
                      setTimeout(() => {qAllert.value = ''}, 3000)
                    })
                    .catch(() => {
                      console.log('Error fetching question')
                    })

              })


        })
        .catch(() => {
          console.log('Error editing question')
        })

  }
  selectedTopics.value.forEach(addTopicToQuestion)
  selectedSubjects.value.forEach(addSubjectToQuestion)
  questionTopics.value.forEach(deleteTopicFromQuestion)
  questionSubjects.value.forEach(deleteSubjectFromQuestion)
}

/*const deleteQuestion = async (questionToDelete) => {
  console.log("questionToDelete - ", questionToDelete)
  let qId = questions.value[questionsData.value.indexOf(questionToDelete)].id
  api.deleteQuestion(qId)
      .then(()  => {
        for (let i = 0; i < quizQuestions.value.length; i++) {
          if(quizQuestions.value[i].id === qId) {
            quizQuestions.value.splice(i, 1)
          }
        }
        questions.value.splice(questionsData.value.indexOf(questionToDelete),1)
        questionsData.value.splice(questionsData.value.indexOf(questionToDelete),1)
      })
      .catch((error) => {
        console.log('err' + error)
      })

}*/



const removeQuestionFromQuiz =  (questionId) => {
  let delQ = quizQuestions.value.find((it) => {return it.id === questionId })
  let qIndex = quizQuestions.value.indexOf(delQ)
  quizQuestions.value.splice(qIndex,1)
  questionRemoveDialog.value = false
}


/*const deleteQuestionFromQuiz =  async (q) => {
  oldQuizData.value = null
  if (props.moduleId){
    quizApi.quizDetailByModule(props.moduleId)
        .then(async (quiz) => {
          quizApi.removeQuizQuestion(quiz.id, q)
              .then(()  => {
                    let delQ = quizQuestions.value.find((it) => {return it.id === q })
                    let qIndex = quizQuestions.value.indexOf(delQ)
                    quizQuestions.value.splice(qIndex,1)
                    reload2()

                  }
              )
              .catch((err) => {
                error.value = err.code
              })
        })

  }
}*/

const addQuestion = (question) => {
  api.questionCorrect(question.id)
    .then((qCorr) => {
      question.correctAnswerData = qCorr.correctAnswerData
      question.explanation = qCorr.explanation

      //pridane novo vytvorenej otazky do kvizu
      quizQuestions.value.push(question)
      questionData.value = ''
      option1.value = ''
      option2.value = ''
      option3.value = ''
      option4.value = ''
      correctAnswers.value = []

      options.value = []
      explanation.value = ''
      timeLimit.value = 15

      createQDialog.value = false
      showQType.value = false
      selectedQType.value = 'MULTICHOICE'

      qAllert.value = 'CREATED'
      setTimeout(() => {
        qAllert.value = ''
      }, 3000)
    })
}

const createQuestion = () => {

  correctAnswers.value = correctAnswers.value.filter(
      (a) => { return options.value.indexOf(a) !== -1 }
  )

  if(!questionData.value || !options.value || options.value.length === 0 ||
      correctAnswers.value.length === 0 || !timeLimit.value)
    return


  let jsonCorrectAnswers = JSON.stringify(correctAnswers.value)

  let jsonOptions = JSON.stringify(options.value)

  let singleAnswer = (correctAnswers.value.length === 1)

  const question = {
    questionData: questionData.value,
    possibleAnswersData :  jsonOptions,
    correctAnswerData: jsonCorrectAnswers,
    timeLimit: timeLimit.value,
    explanation: explanation.value,
    author: userStore.user.username,
    singleAnswer: singleAnswer,
    questionType: selectedQType.value
  }

  api.createQuestion(question)
      .then((createdQ) => {
        selectedTopics.value.forEach((topicId) => quizApi.addQuestionTopic(createdQ.id, topicId))
        selectedSubjects.value.forEach((subjectId) => quizApi.addQuestionSubject(createdQ.id, subjectId))
        api.listQuestionsByUsername(userStore.user.username)
            .then((result) => {
              questions.value = result
              questions.value.forEach(
                  (q) => {
                    api.questionCorrect(q.id)
                        .then((qCorr) => {

                          q.correctAnswerData = qCorr.correctAnswerData
                          q.explanation = qCorr.explanation

                          if(questionsData.value.indexOf(q.questionData) === -1)
                            questionsData.value.push(q.questionData)

                          //pridane novo vytvorenej otazky do kvizu
                          if(createdQ.id === q.id){
                            quizQuestions.value.push(q)
                          }

                          questionData.value = ''
                          option1.value = ''
                          option2.value = ''
                          option3.value = ''
                          option4.value = ''
                          correctAnswers.value = []

                          options.value = []
                          explanation.value = ''
                          timeLimit.value = 15
                          createQDialog.value = false
                          showQType.value = false
                          selectedQType.value = 'MULTICHOICE'

                          qAllert.value = 'CREATED'
                          setTimeout(() => {qAllert.value = ''}, 3000)

                          quizApi.putQuizQuestion(oldQuizData.value.id, q.id, {
                            orderNum: (oldQuizData.value.questions.slice(-1)[0]?.order ?? 0) + 1
                          })
                              .catch((err) => { error.value = err.code })
                        })
                  }
              )

            })
            .catch((err) => {
              error.value = `Chyba při načítání: ${err.code}`
            })
      })
      .catch((err) => {
        error.value = `Chyba při vytváření: ${err.code}`
      })
}

onMounted(async () => {
  onBeforeRouteLeave((to, from, next) => {
    if (JSON.stringify(oldQuizData.value?.questions) === JSON.stringify(quizQuestions.value.map( (it) => it.id))){
      next()
      return
    }
    routerNext.value = next
    unsavedChangesDialog.value = true
  })

  t('$vuetify.quiz_module.create_new_quiz_tab')

  moduleEditTabList.value[MODULE_EDIT_QUIZ_CREATE] = MODULE_EDIT_ITEMS[MODULE_EDIT_QUIZ_CREATE]

  questionData.value = ''
  option1.value = ''
  option2.value = ''
  option3.value = ''
  option4.value = ''
  correctAnswers.value = []
  timeLimit.value = 15

  if(props.moduleId){
    await api.quizDetailByModule(props.moduleId)
        .then(async (result) => {
          oldQuizData.value = result

          for (const q of oldQuizData.value.questions) {
            await api.questionDetail(q)
                .then( async (qRes) => {

                  await api.questionCorrect(q)
                      .then((qCorrect) => {
                        quizQuestions.value.push({id: qRes.id,
                          questionData: qRes.questionData,
                          possibleAnswersData: qRes.possibleAnswersData,
                          timeLimit : qRes.timeLimit,
                          questionType: qRes.questionType,
                          author: qRes.author,
                          correctAnswerData : qCorrect.correctAnswerData,
                          explanation: qCorrect.explanation,
                          orderNum: oldQuizData.value.questions.indexOf(qRes.id)} )
                        quizQuestions.value.sort((a,b) => a.orderNum - b.orderNum)

                      })
                      .catch(() => {
                        console.log('Error fetching question data' + q)
                      })
                })
                .catch((err) => {
                  console.log(err)
                  error.value = `Chyba při načítání: ${err.code}`
                })
          }

          quizName.value = oldQuizData.value.name
          quizAttempts.value = oldQuizData.value.numOfAttempts
        })
        .catch((err) => {
          console.log(err)
          error.value = `Chyba při načítání: ${err.code}`
        })
  }
})

const addQuestionDialogDismiss = (shouldReload) => {
  addQuestionDialog.value = false
  if (shouldReload) props.reload()
}

const questionRemoveDialog = ref(false)
const questionToRemove = ref(null)
provide('deleteDialog', questionRemoveDialog)

const deleteDialogTexts = computed(() => {
  if (questionToRemove.value)
    return {
      title: '$vuetify.quiz_module.remove_question',
      start: '$vuetify.quiz_module.remove_question_dialog_start',
      end: '$vuetify.quiz_module.remove_question_dialog_end',
    }
  else
    return {
      itemName: '',
      title: '',
      start: '',
      middle: '',
      end: '',
    }
})

</script>

<template>
  <DeleteDialog item-name=""
                :title="deleteDialogTexts.title"
                :text-start="deleteDialogTexts.start"
                text-before-line-break=""
                :text-second-line="deleteDialogTexts.end"
                :on-cancel="() => questionRemoveDialog = false"
                :on-confirm="() => removeQuestionFromQuiz(questionToRemove.id)"
                :text-confirm-button="'$vuetify.action_remove'" />
  <v-dialog v-model="addQuestionDialog" width="80%" height="100%">
    <QuestionList :dismiss="addQuestionDialogDismiss" :reload="reload2" :quiz-id="oldQuizData?.id"
                  :state="questionListState" :delete-question="removeQuestionFromQuiz" :add-question="addQuestion" />
    <!-- height 100% fixes the dialog to the top, but creates an invisible area below that otherwise wouldn't dismiss it -->
    <div style="flex: 1" @click="() => addQuestionDialog = false" />
  </v-dialog>
  <v-dialog v-model="editQDialog" width="80%">
    <v-card class="ma-5">
      <v-card-title class="ml-3 mt-3">
        {{ t('$vuetify.quiz_module.edit_question_dialog_heading') }}
      </v-card-title>
      <v-card-item>
        <v-select v-model="selectedTopics" :items="topics" :label="translate('topic')" item-title="name" item-value="id"
                  multiple />
        <v-select v-model="selectedSubjects" :items="subjects" :label="translate('subject')" item-title="name"
                  item-value="id" multiple />
        <TextEditor v-model="questionToEdit.questionData" class="ml-1 max-height-tab mb-4 assignment-editor"
                    style="width: 99%" :placeholder="t('$vuetify.quiz_module.question')" :editable="true" />

        <v-divider class="mt-5 mb-5 ml-1" thickness="5" />

        <QuestionTrueFalse v-if="questionToEdit.questionType === 'TRUEFALSE'"
                           :correct-answers="JSON.parse(questionToEdit.correctAnswerData)" />
        <QuestionMultichoice v-else-if="questionToEdit.questionType === 'MULTICHOICE'"
                             :correct-answers="JSON.parse(questionToEdit.correctAnswerData)"
                             :options="JSON.parse(questionToEdit.possibleAnswersData)" />
        <div v-else>
          <v-row v-if="editShow" class="mt-5 ml-1 align-center">
            <TextEditor v-model="option1Edit" class="ml-1 max-height-tab mb-4 assignment-editor"
                        :placeholder="t('$vuetify.quiz_module.option')" style="width: 80%" editable="true" />
            <v-checkbox v-model="correctAnswersEdit" :disabled="option1Edit === ''" class="ml-5"
                        :label="t('$vuetify.quiz_module.correct_option')" :value="option1Edit" />
          </v-row>

          <v-row v-if="editShow" class="mt-5 ml-1 align-center">
            <TextEditor v-model="option2Edit" class="ml-1 max-height-tab mb-4 assignment-editor"
                        :placeholder="t('$vuetify.quiz_module.option')" style="width: 80%" editable="true" />
            <v-checkbox v-model="correctAnswersEdit" :disabled="option2Edit === ''" class="ml-5"
                        :label="t('$vuetify.quiz_module.correct_option')" :value="option2Edit" />
          </v-row>

          <v-row v-if="editShow" class="mt-5 ml-1 align-center">
            <TextEditor v-model="option3Edit" class="ml-1 max-height-tab mb-4 assignment-editor"
                        :placeholder="t('$vuetify.quiz_module.option')" style="width: 80%" editable="true" />
            <v-checkbox v-model="correctAnswersEdit" :disabled="option3Edit === ''" class="ml-5"
                        :label="t('$vuetify.quiz_module.correct_option')" :value="option3Edit" />
          </v-row>

          <v-row v-if="editShow" class="mt-5 ml-1 align-center">
            <TextEditor v-model="option4Edit" class="ml-1 max-height-tab mb-4 assignment-editor"
                        :placeholder="t('$vuetify.quiz_module.option')" style="width: 80%" editable="true" />
            <v-checkbox v-model="correctAnswersEdit" :disabled="option4Edit === ''" class="ml-5"
                        :label="t('$vuetify.quiz_module.correct_option')" :value="option4Edit" />
          </v-row>
        </div>

        <v-text-field v-model="questionToEdit.timeLimit" class="mt-8" type="number" style="width: 99%"
                      :label="t('$vuetify.quiz_module.time_limit')" />

        <TextEditor v-model="questionToEdit.explanation" class="ml-1 max-height-tab mb-4 assignment-editor"
                    style="width: 99%" :editable="true" :placeholder="t('$vuetify.quiz_module.explanation')" />
        <v-btn class="mb-5 mt-5" type="button" color="green" variant="tonal" @click="editQuestion()">
          {{ t('$vuetify.quiz_module.edit_question') }}
        </v-btn>
      </v-card-item>
    </v-card>
  </v-dialog>
  <v-dialog v-model="createQDialog" width="80%">
    <v-card>
      <v-card-item>
        <v-card-title class="mt-3 mb-2">
          {{ t('$vuetify.quiz_module.new_question_dialog_heading') }}
        </v-card-title>

        <v-card
          class="ma-1 mb-4"
          style="width: 99%"
          @click="showQType = !showQType">
          <v-row class="ma-2 ml-4 mr-4">
            <v-card-title class="ma-4">
              {{ t('$vuetify.quiz_module.chosen_question_type') }} : {{ selectedQType }}
            </v-card-title>

            <v-spacer />

            <v-card-actions>
              <v-btn
                :icon="showQType ? 'mdi-chevron-up' : 'mdi-chevron-down'"
              />
            </v-card-actions>
          </v-row>


          <v-expand-transition>
            <div v-show="showQType">
              <v-divider />

              <v-row class="mb-4 ml-2 mt-4">
                <v-col v-for="qType in questionTypes" :key="qType" sm="9"
                       md="6"
                       lg="4"
                       class="d-flex">
                  <v-card
                    class="text-center align-center"
                    style="border-radius: 20px"
                    variant="tonal"
                    width="300"
                    height="300"
                    @click="selectedQType = qType">
                    <div v-if="qType === 'TRUEFALSE'">
                      <v-img src="./questionTypes/icons/trueFalseIcon.png" max-width="256" max-height="256"
                             class="mx-auto mb-2" />
                      <p style="font-size: 16px; font-weight: bold">
                        {{ qType }}
                      </p>
                      <v-tooltip
                        activator="parent"
                        location="bottom"
                      >
                        {{ t('$vuetify.quiz_module.tooltip_truefalse') }}
                      </v-tooltip>
                    </div>

                    <div v-else-if="qType === 'MULTICHOICE'">
                      <v-img src="./questionTypes/icons/multichoiceIcon.png" max-width="256" max-height="256"
                             class="mx-auto mb-2" />
                      <p style="font-size: 16px; font-weight: bold">
                        {{ qType }}
                      </p>
                      <v-tooltip
                        activator="parent"
                        location="bottom"
                      >
                        {{ t('$vuetify.quiz_module.tooltip_multichoice') }}
                      </v-tooltip>
                    </div>

                    <v-img v-else src="./questionTypes/icons/placeholder.png" max-width="256" max-height="256"
                           class="mx-auto mb-2" />
                  </v-card>
                </v-col>
              </v-row>
            </div>
          </v-expand-transition>
        </v-card>

        <v-select v-model="selectedTopics" :items="topics" :label="translate('topic')" item-title="name" item-value="id"
                  multiple />
        <v-select v-model="selectedSubjects" :items="subjects" :label="translate('subject')" item-title="name"
                  item-value="id" multiple />

        <TextEditor v-model="questionData" class="ml-1 max-height-tab mb-4 assignment-editor" style="width: 99%"
                    :placeholder="t('$vuetify.quiz_module.question')" :editable="true" />

        <v-divider class="mt-5 mb-5 ml-1" thickness="5" />

        <QuestionTrueFalse v-if="selectedQType === 'TRUEFALSE'" />
        <QuestionMultichoice v-else-if="selectedQType === 'MULTICHOICE'" />
        <QuestionOpen v-else-if="selectedQType === 'OPEN'" />
        <QuestionFillText v-else-if="selectedQType === 'FILLTEXT'" />
        <QuestionSort v-else-if="selectedQType === 'SORT'" />
        <QuestionConnect v-else-if="selectedQType === 'CONNECT'" />

        <v-text-field v-model="timeLimit" class="ml-1 mt-8" type="number" style="width: 99%"
                      :label="t('$vuetify.quiz_module.time_limit')" />

        <TextEditor v-model="explanation" class="ml-1 max-height-tab mb-4 assignment-editor" style="width: 99%"
                    :placeholder="t('$vuetify.quiz_module.explanation')" :editable="true" />

        <v-btn class="ml-1 mt-5" type="button" color="green" variant="tonal" @click="createQuestion()">
          {{ t('$vuetify.quiz_module.create_question') }}
        </v-btn>
      </v-card-item>
    </v-card>
  </v-dialog>

  <div v-if="moduleEditItem.id === MODULE_EDIT_QUIZ_CREATE">
    <v-card-title class="ml-3 mt-5 mb-5">
      {{ t('$vuetify.quiz_module.create_new_quiz_heading') }}
    </v-card-title>

    <draggable v-if="quizQuestions" v-model="quizQuestions" tag="ul" item-key="id">
      <template #item="{ element: question }">
        <div v-if="question.questionType === '' || question.questionType === 'LEGACY'">
          <v-card variant="outlined" class="ml-3 mt-2">
            <v-card-item>
              <v-row>
                <v-card-item
                  style="max-width: 80%"
                  :title="quizQuestions.indexOf(question) + 1 + '. ' + question.questionData"
                />
                <v-spacer />

                <v-btn v-if="question.author === userStore.user.username" color="blue" variant="text" icon="mdi-pencil"
                       @click="editQDialog = true; editSelected = question.questionData; questionToEdit = question; getQuestionToEdit();">
                  <v-icon>mdi-pencil</v-icon>
                  <v-tooltip
                    activator="parent"
                    location="bottom"
                  >
                    {{ t('$vuetify.action_edit') }}
                  </v-tooltip>
                </v-btn>

                <v-btn v-else disabled variant="text" icon="mdi-pencil"
                       @click="editQDialog = true; editSelected = question.questionData; questionToEdit = question; getQuestionToEdit();">
                  <v-icon>mdi-pencil</v-icon>
                </v-btn>

                <v-btn color="blue" variant="text" icon="mdi-delete" @click="removeQuestionFromQuiz(question.id)">
                  <v-icon>mdi-delete</v-icon>
                  <v-tooltip
                    activator="parent"
                    location="bottom"
                  >
                    {{ t('$vuetify.action_remove') }}
                  </v-tooltip>
                </v-btn>
              </v-row>
            </v-card-item>

            <v-card-subtitle class="pl-4">
              <b v-if="JSON.parse(question.correctAnswerData).indexOf(JSON.parse(question.possibleAnswersData).option1) > -1">
                {{ JSON.parse(question.possibleAnswersData).option1 }}
              </b>
              <i v-else>{{ JSON.parse(question.possibleAnswersData).option1 }}</i>
            </v-card-subtitle>
            <v-card-subtitle class="pl-4">
              <b v-if="JSON.parse(question.correctAnswerData).indexOf(JSON.parse(question.possibleAnswersData).option2) > -1">
                {{ JSON.parse(question.possibleAnswersData).option2 }}
              </b>
              <i v-else>{{ JSON.parse(question.possibleAnswersData).option2 }}</i>
            </v-card-subtitle>
            <v-card-subtitle class="pl-4">
              <b v-if="JSON.parse(question.correctAnswerData).indexOf(JSON.parse(question.possibleAnswersData).option3) > -1">
                {{ JSON.parse(question.possibleAnswersData).option3 }}
              </b>
              <i v-else>{{ JSON.parse(question.possibleAnswersData).option3 }}</i>
            </v-card-subtitle>
            <v-card-subtitle class="pl-4">
              <b v-if="JSON.parse(question.correctAnswerData).indexOf(JSON.parse(question.possibleAnswersData).option4) > -1">
                {{ JSON.parse(question.possibleAnswersData).option4 }}
              </b>
              <i v-else>{{ JSON.parse(question.possibleAnswersData).option4 }}</i>
            </v-card-subtitle>

            <v-card-subtitle class="pl-4">
              Časový limit: {{ question.timeLimit }}
            </v-card-subtitle>
          </v-card>
        </div>

        <div v-else>
          <v-card variant="outlined" class="ml-3 mt-2">
            <v-card-item>
              <v-row>
                <v-card-item
                  style="max-width: 80%"
                  :title="quizQuestions.indexOf(question) + 1 + '. ' + question.questionData"
                />
                <v-spacer />

                <v-btn v-if="question.author === userStore.user.username && !props.readOnly" color="blue" variant="text"
                       icon="mdi-pencil" @click=" questionToEdit = question; editQDialog = true; getQuestionToEdit()">
                  <v-icon>mdi-pencil</v-icon>
                  <v-tooltip
                    activator="parent"
                    location="bottom"
                  >
                    {{ t('$vuetify.action_edit') }}
                  </v-tooltip>
                </v-btn>
                <v-btn v-else disabled variant="text" icon="mdi-pencil"
                       @click="questionToEdit = question; editQDialog = true; getQuestionToEdit()">
                  <v-icon>mdi-pencil</v-icon>
                </v-btn>
                <v-btn color="red" :disabled="props.readOnly" variant="text" icon="mdi-delete"
                       @click="() => { questionToRemove = question; questionRemoveDialog = true }">
                  <v-icon>mdi-delete</v-icon>
                  <v-tooltip
                    activator="parent"
                    location="bottom"
                  >
                    {{ t('$vuetify.action_remove') }}
                  </v-tooltip>
                </v-btn>
              </v-row>
            </v-card-item>

            <v-card-subtitle
              v-for="(option, index) in JSON.parse(question.possibleAnswersData)"
              :key="option"
              class="pl-4">
              <b v-if="JSON.parse(question.correctAnswerData).indexOf(JSON.parse(question.possibleAnswersData)[index]) > -1">
                {{ JSON.parse(question.possibleAnswersData)[index] }}
              </b>
              <i v-else>{{ JSON.parse(question.possibleAnswersData)[index] }}</i>
            </v-card-subtitle>

            <v-card-subtitle class="pl-4">
              Časový limit: {{ question.timeLimit }}
            </v-card-subtitle>
          </v-card>
        </div>
      </template>
    </draggable>
    <v-card-actions style="justify-content: space-between" class="mb-5">
      <v-btn :disabled="props.readOnly" variant="tonal" class="mt-3" color="purple" size="large"
             @click="addQuestionDialog = true;">
        {{ t('$vuetify.quiz_module.add_existing_question') }}
      </v-btn>
      <v-btn :disabled="props.readOnly" variant="tonal" class="mt-3" color="cyan" size="large"
             @click="createQDialog = true; showQType = false; selectedQType = 'MULTICHOICE'; correctAnswers=[]; selectedSubjects = []; selectedTopics = []">
        {{ t('$vuetify.quiz_module.create_new_question') }}
      </v-btn>
    </v-card-actions>

    <v-alert
      v-if="qAllert !== ''"
      class="ml-3 mt-3 mb-3"
      variant="tonal"
      type="success"
      :title="qAllert === 'CREATED' ? 'Otázka vytvořena' : 'Otázka upravena' " />
  </div>
</template>

<style scoped>

</style>
