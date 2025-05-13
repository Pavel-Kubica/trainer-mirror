<script setup>

import {inject, onMounted, ref, provide} from "vue";
import {MODULE_EDIT_ITEMS, MODULE_EDIT_SELFTEST_CREATE} from "@/plugins/constants";
import {useLocale} from "vuetify";
import api from '@/service/quizApi'
import {useUserStore} from "@/plugins/store";
//import QuestionMultichoice from "@/components/modules/questionTypes/QuestionMultichoice.vue";
//import QuestionTrueFalse from "@/components/modules/questionTypes/QuestionTrueFalse.vue";
import TextEditor from "@/components/custom/TextEditor.vue";
import QuestionTrueFalse from "@/components/modules/questionTypes/QuestionTrueFalse.vue";
import QuestionMultichoice from "@/components/modules/questionTypes/QuestionMultichoice.vue";

const { t } = useLocale()
const userStore = useUserStore()


const props = defineProps(['moduleId', 'readOnly'])
const createEditCallback  = inject('createEditCallback', () => {})

const moduleEditTabList = inject('moduleEditTabList')
const moduleEditItem = inject('moduleEditItem')

const addQDialog = ref(false)
const selected = ref(null)

const createQDialog = ref(false)
const questionTypes = ref([ 'TRUEFALSE', 'MULTICHOICE'])
const showQType = ref(false)
const newQuestionData = ref({questionData: '', options: [], correctAnswers: [], timeLimit: 15, explanation: '', type: 'MULTICHOICE', id : null})

const newOptions = ref([])
const newCorrectAnswers = ref([])
provide('options', newOptions)
provide('correctAnswers', newCorrectAnswers)

const editQDialog = ref(false)

const selftestQuestions = ref([])
const oldQuizData = ref(null)

const allQuestions = ref([])
const questionsByAuthor = ref([])

createEditCallback.value = (module) => {

  if (!selftestQuestions.value)
    return

  const selftest = {
    name: module.name,
    numOfQuestions : selftestQuestions.value.length,
    numOfAttempts: 1,
    questions: selftestQuestions.value.map((q) => q.id),
    moduleId: module.id
  }

  if(props.moduleId){
    api.editQuiz(oldQuizData.value.id, selftest)
        .catch((err) => {
          console.log(err)
        })
  }
  else{
    api.createQuiz(selftest)
        .catch((err) => {
          console.log(err)
        })
  }
}

const openEditDialog = (toEdit) => {
  newOptions.value = toEdit.possibleAnswersData
  newCorrectAnswers.value = toEdit.correctAnswerData
  newQuestionData.value.questionData = toEdit.questionData
  newQuestionData.value.type = toEdit.questionType
  newQuestionData.value.timeLimit = toEdit.timeLimit
  newQuestionData.value.explanation = toEdit.explanation
  newQuestionData.value.id = toEdit.id
  editQDialog.value = true
}
const addQuestionToQuiz = () => {
  if(!selected.value)
    return

  let qIndex = questionsByAuthor.value.map(it => it.id).indexOf(selected.value.id)
  if(qIndex !== -1)
    selftestQuestions.value.push(questionsByAuthor.value[qIndex])
}

const createQuestion = async () => {

  newCorrectAnswers.value = newCorrectAnswers.value.filter(
      (a) => { return newOptions.value.includes(a)}
  )

  if(!newQuestionData.value.questionData || !newQuestionData.value.timeLimit ||
      !newQuestionData.value.type || !newOptions.value ||
      newOptions.value.length < 2 || !newCorrectAnswers.value || newCorrectAnswers.value === [])
    return

  const question = {
    questionData: newQuestionData.value.questionData,
    possibleAnswersData : JSON.stringify(newOptions.value),
    correctAnswerData: JSON.stringify(newCorrectAnswers.value),
    timeLimit: newQuestionData.value.timeLimit,
    explanation: newQuestionData.value.explanation,
    author: userStore.user.name,
    singleAnswer: newCorrectAnswers.value.length === 1,
    questionType: newQuestionData.value.type
  }

  api.createQuestion(question)
      .then(q => {
        let toInsert = q
        toInsert.possibleAnswersData = newOptions.value
        toInsert.correctAnswerData = newCorrectAnswers.value
        toInsert.explanation = newQuestionData.value.explanation
        allQuestions.value.push(toInsert)
        questionsByAuthor.value.push(toInsert)
        selftestQuestions.value.push(toInsert)

        newQuestionData.value.questionData = ''
        newOptions.value = []
        newCorrectAnswers.value = []
        newQuestionData.value.timeLimit = 15
        newQuestionData.value.explanation = ''
        newQuestionData.value.type = 'MULTICHOICE'
        createQDialog.value = false
      })
      .catch(e => {
        console.log(e)
      })
}

const editQuestion = async () => {
  newCorrectAnswers.value = newCorrectAnswers.value.filter(
      (a) => { return newOptions.value.includes(a)}
  )

  if(!newQuestionData.value.questionData || !newQuestionData.value.timeLimit ||
      !newQuestionData.value.type || !newOptions.value ||
      newOptions.value.length < 2 || !newCorrectAnswers.value || newCorrectAnswers.value === [])
    return

  const question = {
    questionData: newQuestionData.value.questionData,
    possibleAnswersData :  JSON.stringify(newOptions.value),
    correctAnswerData: JSON.stringify(newCorrectAnswers.value),
    timeLimit: newQuestionData.value.timeLimit,
    explanation: newQuestionData.value.explanation,
    singleAnswer: newCorrectAnswers.value.length === 1,
    questionType: newQuestionData.value.type,
  }

  api.editQuestion(newQuestionData.value.id, question)
      .then((q) => {

        let toReplace = q
        toReplace.possibleAnswersData = newOptions.value
        toReplace.correctAnswerData = newCorrectAnswers.value
        toReplace.explanation = newQuestionData.value.explanation
        allQuestions.value[allQuestions.value.map( it => it.id).indexOf(q.id)] = toReplace
        questionsByAuthor.value[questionsByAuthor.value.map( it => it.id).indexOf(q.id)] = toReplace
        selftestQuestions.value[selftestQuestions.value.map( it => it.id).indexOf(q.id)] = toReplace

        newQuestionData.value.questionData = ''
        newOptions.value = []
        newCorrectAnswers.value = []
        newQuestionData.value.timeLimit = 15
        newQuestionData.value.explanation = ''
        newQuestionData.value.type = 'MULTICHOICE'
        newQuestionData.value.id = null
        editQDialog.value = false
      })
      .catch(error => {console.log(error)})

}

onMounted(async () => {

  t('$vuetify.selftest_module.create_new_selftest_tab')
  moduleEditTabList.value[MODULE_EDIT_SELFTEST_CREATE] = MODULE_EDIT_ITEMS[MODULE_EDIT_SELFTEST_CREATE]

  if(props.moduleId){
    //console.log(props.moduleId)

    await api.quizDetailByModule(props.moduleId)
        .then((res) => {
          oldQuizData.value = res
          //console.log("Old quiz: ", oldQuizData.value)
        })
        .catch((err) => {
          console.log(err)
        })

    await oldQuizData.value.questions.forEach(
        (q) => {
          api.questionDetail(q)
              .then((qData) => {

                api.questionCorrect(q)
                    .then((qCorr) => {
                      let toInsert = qData //correctAnswerData explanation
                      toInsert.correctAnswerData = JSON.parse(qCorr.correctAnswerData)
                      toInsert.possibleAnswersData = JSON.parse(qData.possibleAnswersData)
                      toInsert.explanation = qCorr.explanation
                      selftestQuestions.value.push(toInsert)
                      //console.log(toInsert)
                    })

              })
        }
    )
  }

  await api.listQuestions()
      .then(async (questions) => {
        allQuestions.value = questions

        for (let i = 0; i < questions.length; i++){
          await api.questionCorrect(allQuestions.value[i].id)
              .then(qCorr => {
                allQuestions.value[i].correctAnswerData = JSON.parse(qCorr.correctAnswerData)
                allQuestions.value[i].explanation = qCorr.explanation
                allQuestions.value[i].possibleAnswersData = JSON.parse(allQuestions.value[i].possibleAnswersData)

              })
        }
        //console.log("All: ", allQuestions.value)
      })

  await api.listQuestionsByUsername(userStore.user.name)
      .then(async (questions) => {
        questionsByAuthor.value = questions

        for (let i = 0; i < questions.length; i++){
          await api.questionCorrect(questionsByAuthor.value[i].id)
              .then(qCorr => {
                questionsByAuthor.value[i].correctAnswerData = JSON.parse(qCorr.correctAnswerData)
                questionsByAuthor.value[i].explanation = qCorr.explanation
                questionsByAuthor.value[i].possibleAnswersData = JSON.parse(questionsByAuthor.value[i].possibleAnswersData)
              })
        }
      })

})
</script>

<template>
  <div v-if="moduleEditItem.id === MODULE_EDIT_SELFTEST_CREATE">
    <v-dialog v-model="addQDialog">
      <v-card>
        <v-row class="mt-1 mr-1 justify-end">
          <v-btn elevation="0" icon="mdi-close" @click="addQDialog = false">
            <v-icon>mdi-close</v-icon>
            <v-tooltip
              activator="parent"
              location="bottom"
            >
              {{ t('$vuetify.dialog_close') }}
            </v-tooltip>
          </v-btn>
        </v-row>

        <v-table v-if="questionsByAuthor" class="mt-5 ml-3">
          <thead>
            <tr>
              <th class="text-left">
                <b>{{ t('$vuetify.quiz_module.question') }}</b>
              </th>
              <th class="text-left" />
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="item in questionsByAuthor"
              :key="item"
            >
              <td style="max-width: 50%">
                {{ item.questionData }}
              </td>
              <td class="d-flex justify-end">
                <v-btn variant="text" icon="mdi-plus" @click="selected = item; addQuestionToQuiz(); selected = null">
                  <v-icon>mdi-plus</v-icon>
                  <v-tooltip
                    activator="parent"
                    location="bottom"
                  >
                    {{ t('$vuetify.action_add') }}
                  </v-tooltip>
                </v-btn>

                <v-btn color="red" variant="text" icon="mdi-delete" @click="true">
                  <v-icon>mdi-delete</v-icon>
                  <v-tooltip
                    activator="parent"
                    location="bottom"
                  >
                    {{ t('$vuetify.action_delete') }}
                  </v-tooltip>
                </v-btn>
              </td>
            </tr>
          </tbody>
        </v-table>
      </v-card>
    </v-dialog>

    <v-dialog v-model="createQDialog">
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
                {{ t('$vuetify.quiz_module.chosen_question_type') }} : {{ newQuestionData.type }}
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
                      @click="newQuestionData.type = qType">
                      <div v-if="qType === 'TRUEFALSE'">
                        <v-img src="./questionTypes/icons/trueFalseIcon.png" max-width="256" max-height="256" class="mx-auto mb-2" />
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
                        <v-img src="./questionTypes/icons/multichoiceIcon.png" max-width="256" max-height="256" class="mx-auto mb-2" />
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

                      <v-img v-else src="./questionTypes/icons/placeholder.png" max-width="256" max-height="256" class="mx-auto mb-2" />
                    </v-card>
                  </v-col>
                </v-row>
              </div>
            </v-expand-transition>
          </v-card>

          <TextEditor v-model="newQuestionData.questionData" class="ml-1 max-height-tab mb-4 assignment-editor" :placeholder="t('$vuetify.quiz_module.question')" :editable="true" />

          <QuestionTrueFalse v-if="newQuestionData.type === 'TRUEFALSE'" />
          <QuestionMultichoice v-else-if="newQuestionData.type === 'MULTICHOICE'" />

          <v-text-field v-model="newQuestionData.timeLimit" class="ml-1 mt-8" type="number" :label="t('$vuetify.quiz_module.time_limit')" />

          <TextEditor v-model="newQuestionData.explanation" class="ml-1" :placeholder="t('$vuetify.quiz_module.explanation')" style="height: 200px" editable="true" />

          <v-btn class="ml-1 mt-5" type="button" color="green" variant="tonal" @click="createQuestion()">
            {{ t('$vuetify.quiz_module.create_question') }}
          </v-btn>
        </v-card-item>
      </v-card>
    </v-dialog>

    <v-dialog v-model="editQDialog">
      <v-card>
        <v-card-item>
          <v-card-title class="mt-3 mb-2">
            {{ t('$vuetify.quiz_module.edit_question_dialog_heading') }}
          </v-card-title>

          <TextEditor v-model="newQuestionData.questionData" class="ml-1 max-height-tab mb-4 assignment-editor" :placeholder="t('$vuetify.quiz_module.question')" :editable="true" />

          <QuestionTrueFalse v-if="newQuestionData.type === 'TRUEFALSE'" :correct-answers="newCorrectAnswers" />
          <QuestionMultichoice v-else-if="newQuestionData.type === 'MULTICHOICE'" :options="newOptions" :correct-answers="newCorrectAnswers" />
          <div v-else>
            {{ newQuestionData.type }}
          </div>

          <v-text-field v-model="newQuestionData.timeLimit" class="ml-1 mt-8" type="number" :label="t('$vuetify.quiz_module.time_limit')" />

          <TextEditor v-model="newQuestionData.explanation" class="ml-1" :placeholder="t('$vuetify.quiz_module.explanation')" style="height: 200px" editable="true" />

          <v-btn class="ml-1 mt-5" type="button" color="green" variant="tonal" @click="editQuestion()">
            {{ t('$vuetify.quiz_module.edit_question') }}
          </v-btn>
        </v-card-item>
      </v-card>
    </v-dialog>

    <v-card v-if="!props.readOnly" variant="tonal" color="blue" class="ma-4">
      <v-row class="align-center justify-center pa-2 ml-4">
        <v-icon icon="mdi-information" />
        <v-card-text>
          {{ t('$vuetify.selftest_module.selftest_heading_info') }}
        </v-card-text>
      </v-row>
    </v-card>

    <div>
      <v-card v-for="(q, index) in selftestQuestions"
              :key="index"
              variant="outlined"
              class="ma-4">
        <v-row class="mt-2 mb-2 pl-4">
          <v-card-title class="pl-4" style="max-width: 80%">
            {{ q.questionData }}
          </v-card-title>

          <v-spacer />

          <v-btn :disabled="props.readOnly" color="blue" variant="text" icon="mdi-pencil" class="mr-2" @click="openEditDialog(q)">
            <v-icon>mdi-pencil</v-icon>
            <v-tooltip
              activator="parent"
              location="bottom"
            >
              {{ t('$vuetify.action_edit') }}
            </v-tooltip>
          </v-btn>
          <v-btn :disabled="props.readOnly" color="red" variant="text" icon="mdi-delete" class="mr-4" @click="selftestQuestions.splice(index, 1)">
            <v-icon>mdi-delete</v-icon>
            <v-tooltip
              activator="parent"
              location="bottom"
            >
              {{ t('$vuetify.action_remove') }}
            </v-tooltip>
          </v-btn>
        </v-row>

        <v-card-subtitle v-for="(option, oIndex) in q.possibleAnswersData" :key="oIndex"
                         class="pl-4">
          <b v-if="q.correctAnswerData.includes(option)">
            {{ q.possibleAnswersData[oIndex] }}
          </b>
          <i v-else>{{ q.possibleAnswersData[oIndex] }}</i>
        </v-card-subtitle>
      </v-card>
    </div>

    <v-card-actions style="justify-content: space-between" class="mb-5">
      <v-btn :disabled="props.readOnly" variant="tonal" class="mt-3" color="green" size="large" @click="addQDialog = true;">
        {{ t('$vuetify.quiz_module.question_list') }}
      </v-btn>
      <v-btn :disabled="props.readOnly" variant="tonal" class="mt-3" color="cyan" size="large" @click="createQDialog = true;">
        {{ t('$vuetify.quiz_module.create_new_question') }}
      </v-btn>
    </v-card-actions>
  </div>
</template>

<style scoped>

</style>