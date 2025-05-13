<script setup>

import {inject, onMounted, ref} from "vue";
import api from "@/service/quizApi";
import {MdPreview} from "md-editor-v3";
import {useUserStore} from "@/plugins/store";
import {useLocale} from "vuetify";
const userStore = useUserStore()
const { t } = useLocale()


const lesson = inject('lesson')

const selftestState = inject('selftestState')
const selftestStateList = inject('selftestStateList')
const selftestData = inject('selftestData')

const selftestQuestionList = ref([])
const studentScore = ref(0)

const toHome = async () => {

  api.editQuizroom(selftestData.value.id, {lesson: lesson, quizState : 'CLOSED', roomState: false})
      .then(() => {
        selftestState.value = selftestStateList.value[0]
        selftestData.value = null
        selftestQuestionList.value = []
        studentScore.value = 0
      })
      .catch(err => {
        console.log(err)
      })

}

onMounted(async () => {

  await selftestData.value.selftestQuestions.forEach( (qId) => {
    api.questionDetail(qId)
        .then(async (qData) => {

          await api.questionCorrect(qId)
              .then(async (qCorr) => {
                let toInsert = qData
                toInsert.possibleAnswersData = JSON.parse(qData.possibleAnswersData)
                toInsert.correctAnswerData = JSON.parse(qCorr.correctAnswerData)
                toInsert.explanation = qCorr.explanation
                toInsert.explanationShown = false
                toInsert.orderNum = selftestData.value.selftestQuestions.indexOf(qId)

                await api.studentsAnswerFromQuestionDetail(selftestData.value.id, qId, userStore.user.id)
                    .then((answer) => {
                      if (answer.length === 0)
                        toInsert.studentAnswerData = []
                      else
                        toInsert.studentAnswerData = JSON.parse(answer[0].data)

                      selftestQuestionList.value.push(toInsert)
                    })
                    .catch((error) => {
                      console.log(error)
                    })

                selftestQuestionList.value = selftestQuestionList.value.sort((a,b) => a.orderNum - b.orderNum )

              })
              .catch((error) => {
                console.log(error)
              })

        })
        .catch((error) => {
          console.log(error)
        })
  })

  await api.listQuizroomStudents(selftestData.value.id)
      .then((stud) => {
        studentScore.value = stud[0].points

      })
      .catch((error) => {
         console.log(error)
      })

})

</script>

<template>
  <v-row>
    <v-col cols="9">
      <div>
        <v-card v-for="(q, index) in selftestQuestionList" :key="index" :color="userStore.darkMode ? 'black' : 'white'" class="mt-2 ml-4 mr-4">
          <v-card-title class="pl-10">
            {{ t('$vuetify.selftest_module.question') }} {{ index + 1 }}
          </v-card-title>
          <MdPreview v-model="selftestQuestionList[index].questionData" language="en-US" preview-theme="github"
                     :theme="userStore.darkMode ? 'dark' : 'light'" :no-iconfont="true" :read-only="true" class="px-4 pb-2 pl-10 text-left ma-4 ml-8" style="width: 95%; border: 1px solid; border-radius: 10px" :toolbars="[]" />
          <!--          {{selftestQuestionList[index]}}-->
          <div v-if="selftestQuestionList[index]">
            <v-card v-for="(option, oIndex) in selftestQuestionList[index].possibleAnswersData"
                    :key="oIndex"
                    :variant=" selftestQuestionList[index].correctAnswerData.includes(option) ||
                      selftestQuestionList[index].studentAnswerData.includes(option) ? 'tonal' : 'outlined'"
                    style="border: 1px solid"
                    rounded="lg"
                    class="ma-4 mt-8 mb-8 pa-4"
                    :color="selftestQuestionList[index].correctAnswerData.indexOf(option) >= 0 ? 'green':
                      selftestQuestionList[index].studentAnswerData.includes(option) ? 'red' :
                      userStore.darkMode ? 'white' : 'black'">
              <v-row class="mt-4 mb-4 align-center ">
                <div class="ml-4 mr-6">
                  <v-icon v-if="selftestQuestionList[index].studentAnswerData.includes(option)" size="25">
                    mdi-radiobox-marked
                  </v-icon>
                  <v-icon v-else size="25">
                    mdi-radiobox-blank
                  </v-icon>
                </div>
                <MdPreview v-model="selftestQuestionList[index].possibleAnswersData[oIndex]" language="en-US" preview-theme="github" :theme="userStore.darkMode ? 'dark' : 'light'"
                           style="max-width: 80%" :no-iconfont="true" :read-only="true" :toolbars="[]" />
                <div class="ml-6 mr-4">
                  <v-icon v-if="selftestQuestionList[index].correctAnswerData.indexOf(option) >= 0" size="25">
                    mdi-check-circle
                  </v-icon>
                  <v-icon v-else-if="selftestQuestionList[index].studentAnswerData.includes(option)" size="25">
                    mdi-close-circle
                  </v-icon>
                </div>
              </v-row>
            </v-card>
          </div>

          <v-card v-if="q.explanation !== ''" class="ma-4 mt-8" :color="userStore.darkMode ? 'black' : 'white'">
            <v-card-actions>
              <v-row @click="q.explanationShown = !q.explanationShown">
                <v-btn color="blue" variant="text" class="ma-1">
                  Vysvětlení
                </v-btn>
                <v-spacer />
                <v-btn class="mr-2"
                       :icon="q.explanationShown ? 'mdi-chevron-up' : 'mdi-chevron-down'"
                />
              </v-row>
            </v-card-actions>
            <v-expand-transition>
              <div v-show="q.explanationShown">
                <v-divider />
                <v-card-item>
                  <MdPreview v-model="q.explanation" language="en-US" preview-theme="github"
                             :theme="userStore.darkMode ? 'dark' : 'light'" :no-iconfont="true" :read-only="true" class="px-4 pb-2 pl-10 text-left" :toolbars="[]" />
                </v-card-item>
              </div>
            </v-expand-transition>
          </v-card>
        </v-card>
      </div>
    </v-col>
    <v-col cols="2">
      <v-card style="position: fixed; z-index: 999" variant="outlined">
        <v-row class="mt-4 ml-1 mr-8 pa-2">
          {{ t('$vuetify.selftest_module.number_of_questions') }}: {{ selftestData.selftestQuestions.length }}
        </v-row>
        <v-row class="ml-1 mr-8 mb-2 pa-2">
          {{ t('$vuetify.selftest_module.correctly_answered') }}: {{ studentScore }}
        </v-row>
        <v-row class="ml-1 mr-8 mb-2 pa-2">
          {{ t('$vuetify.selftest_module.success_rate') }}: {{ Math.floor((100 * studentScore) / selftestData.selftestQuestions.length) }}%
        </v-row>
        <v-divider :color="userStore.darkMode ? 'white' : 'black' " class="mb-4" />
        <div class="text-center align-center justify-center">
          <v-btn color="blue" class="ma-4 mt-2" @click="toHome()">
            {{ t('$vuetify.selftest_module.to_start') }}
          </v-btn>
        </div>
      </v-card>
    </v-col>
  </v-row>
</template>

<style scoped>

</style>