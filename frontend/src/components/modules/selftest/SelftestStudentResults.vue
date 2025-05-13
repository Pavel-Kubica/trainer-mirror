<script setup>

import {onMounted, ref, watch} from "vue";
import api from "@/service/quizApi";
import {useUserStore} from "@/plugins/store";
import {MdPreview} from "md-editor-v3";
import {useLocale} from "vuetify";
//import {MdPreview} from "md-editor-v3";
const userStore = useUserStore()
const { t } = useLocale()


const props = defineProps(['module', 'lesson', 'teacher'])

const isLoaded = ref(false)
const allAtempts = ref([])
const quizData = ref(null)
const currSelftest = ref(null)
const currAtempt = ref(1)
const points = ref(0)

function resetValues() {
  isLoaded.value = false
  points.value = 0
  allAtempts.value = []
  quizData.value = null
  currSelftest.value = null
}

async function loadSelftest(){
  isLoaded.value = false
  resetValues()

  await api.quizDetailByModule(props.module.id)
      .then((quiz) => {
        quizData.value = quiz
      })
      .catch(err => {console.error(err)})

  await api.listQuizroomsByStudent(props.teacher, quizData.value.id)
      .then(rooms => {
        allAtempts.value = rooms.map(room => room.id)
      })
      .catch(err => {console.error(err)})

  await api.selftestDetail(allAtempts.value[currAtempt.value - 1])
      .then(selftest => {
        currSelftest.value = selftest
        currSelftest.value.questionData = []
        currSelftest.value.selftestQuestions.forEach(q => {
          api.questionDetail(q)
              .then( async question => {
                currSelftest.value.questionData[currSelftest.value.selftestQuestions.indexOf(q)] = question
                currSelftest.value.questionData[currSelftest.value.selftestQuestions.indexOf(q)].options = JSON.parse(currSelftest.value.questionData[currSelftest.value.selftestQuestions.indexOf(q)].possibleAnswersData)
                await api.questionCorrect(q)
                    .then(qCorr => {
                      currSelftest.value.questionData[currSelftest.value.selftestQuestions.indexOf(q)].correctAnswers = JSON.parse(qCorr.correctAnswerData)
                    })
                    .catch(err => {console.error(err)})

                await api.studentsAnswerFromQuestionDetail(allAtempts.value[currAtempt.value - 1], q, props.teacher)
                    .then(a => {
                      if(a.length > 0)
                        currSelftest.value.questionData[currSelftest.value.selftestQuestions.indexOf(q)].studentAnswers = JSON.parse(a[0].data)
                      else
                        currSelftest.value.questionData[currSelftest.value.selftestQuestions.indexOf(q)].studentAnswers = []


                    })
                    .catch(err => {console.error(err)})

              })
              .catch(err => {console.error(err)})

        })

      })
      .catch(err => {console.error(err)})

  await api.listQuizroomStudents(allAtempts.value[currAtempt.value - 1])
      .then((result) => {
        points.value = result[0].points
      })
      .catch((err) => {
        console.log(err)
      })

  isLoaded.value = true
}

watch(currAtempt, async () => {
  await loadSelftest()
})

onMounted( async () => {
  await loadSelftest()
})
</script>

<template>
  <div v-if="isLoaded">
    <div v-if="currSelftest.questionData">
      <v-pagination v-model="currAtempt" :length="allAtempts.length" rounded="circle" total-visible="3" />

      <v-row>
        <v-col cols="9">
          <div>
            <v-card v-for="(q, index) in currSelftest.questionData" :key="index" class="ma-4" :color="userStore.darkMode ? 'black' : 'white'">
              <div v-if="currSelftest.questionData[index]">
                <v-card-title class="pl-10">
                  {{ t('$vuetify.selftest_module.question') }} {{ index + 1 }}
                </v-card-title>
                <MdPreview v-model="currSelftest.questionData[index].questionData" language="en-US" preview-theme="github"
                           :theme="userStore.darkMode ? 'dark' : 'light'" :no-iconfont="true" :read-only="true" class="px-4 pb-2 pl-10 text-left ma-4 ml-8" style="width: 95%; border: 1px solid; border-radius: 10px" :toolbars="[]" />

                <div v-if="Array.isArray(q.studentAnswers) && Array.isArray(q.correctAnswers)">
                  <v-card v-for="(option, oIndex) in currSelftest.questionData[index].options"
                          :key="oIndex"
                          :variant="currSelftest.questionData[index].correctAnswers.includes(option) ||
                            currSelftest.questionData[index].studentAnswers.includes(option) ? 'tonal' : 'outlined'"
                          style="border: 1px solid"
                          rounded="lg"
                          class="ma-4 mt-8 mb-8 pa-4"
                          :color="currSelftest.questionData[index].correctAnswers.indexOf(option) >= 0 ? 'green':
                            currSelftest.questionData[index].studentAnswers.includes(option) ? 'red' :
                            userStore.darkMode ? 'white' : 'black'">
                    <v-row class="mt-4 mb-4 align-center">
                      <div class="ml-4 mr-6">
                        <v-icon v-if="q.studentAnswers.includes(option)" size="25">
                          mdi-radiobox-marked
                        </v-icon>
                        <v-icon v-else size="25">
                          mdi-radiobox-blank
                        </v-icon>
                      </div>
                      <MdPreview v-model="currSelftest.questionData[index].options[oIndex]" language="en-US" preview-theme="github" :theme="userStore.darkMode ? 'dark' : 'light'"
                                 style="max-width: 80%" :no-iconfont="true" :read-only="true" :toolbars="[]" />
                      <div class="ml-6 mr-4">
                        <v-icon v-if="currSelftest.questionData[index].correctAnswers.indexOf(option) >= 0" size="25">
                          mdi-check-circle
                        </v-icon>
                        <v-icon v-else-if="currSelftest.questionData[index].studentAnswers.includes(option)" size="25">
                          mdi-close-circle
                        </v-icon>
                      </div>
                    </v-row>
                  </v-card>
                </div>
              </div>
            </v-card>
          </div>
        </v-col>
        <v-col cols="2">
          <p class="pb-4">
            {{ t('$vuetify.selftest_module.attempt') }}: {{ currAtempt }}, {{ t('$vuetify.selftest_module.quizroom') }}: {{ allAtempts[currAtempt - 1] }}
          </p>
          <v-card style="position: fixed; z-index: 999" variant="outlined">
            <v-row class="mt-4 ml-1 mr-8 pa-2">
              {{ t('$vuetify.selftest_module.number_of_questions') }}: {{ currSelftest.selftestQuestions.length }}
            </v-row>
            <v-row class="ml-1 mr-8 mb-2 pa-2">
              {{ t('$vuetify.selftest_module.correctly_answered') }}: {{ points }}
            </v-row>
            <v-row class="ml-1 mr-8 mb-2 pa-2">
              {{ t('$vuetify.selftest_module.success_rate') }}: {{ Math.floor((100 * points) / currSelftest.selftestQuestions.length) }}%
            </v-row>
          </v-card>
        </v-col>
      </v-row>

      <v-pagination v-model="currAtempt" :length="allAtempts.length" rounded="circle" total-visible="3" />
    </div>
  </div>
  <v-progress-circular v-else indeterminate />
</template>

<style scoped>

</style>