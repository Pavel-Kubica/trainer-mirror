<script setup>

import {TAB_MODULE_DETAIL} from "@/plugins/constants";
import {inject, onMounted, ref, provide, watch} from "vue";
import api from "@/service/quizApi";
import {useLocale} from "vuetify";
import SelftestQuestion from "@/components/modules/selftest/SelftestQuestion.vue";
import SelftestGate from "@/components/modules/selftest/SelftestGate.vue";
import SelftestReview from "@/components/modules/selftest/SelftestReview.vue";
import SelftestError from "@/components/modules/selftest/SelftestError.vue";
import {useUserStore} from "@/plugins/store";
import SelftestOverview from "@/components/modules/selftest/SelftestOverview.vue";
import SelftestStudentResults from "@/components/modules/selftest/SelftestStudentResults.vue";
const { t } = useLocale()
const userStore = useUserStore()


const props = defineProps(['module', 'lesson', 'teacher'])

const tabList = inject('tabList')
const selectedTab = inject('selectedTab')

const selftestState = ref('CREATING')
const selftestStateList = ref(['CREATING', 'ANSWERING', 'CHECKING' ,'REVIEWING']) // add another for selftest overview maybe? (moodle-like)
const selftestData = ref(null)

const currentQuestion = ref(null)
const currentQuestionIndex = ref(1)
const currentSelectedAnswers = ref([])

const isLoaded = ref(false)

provide('lesson', props.lesson)

provide('selftestState', selftestState)
provide('selftestStateList', selftestStateList)
provide('selftestData', selftestData)

provide('currentQuestion', currentQuestion)
provide('currentQuestionIndex', currentQuestionIndex)
provide('currentSelectedAnswers', currentSelectedAnswers)


watch(currentQuestionIndex, async (_, oldVal) => {

  api.editQuizroom(selftestData.value.id, {lesson: props.lesson, currQuestion: currentQuestionIndex.value - 1})
      .catch(err => console.log(err))

  if(selftestState.value !== selftestStateList.value[1])
    return

  let answerString = JSON.stringify(currentSelectedAnswers.value)
  const answer = {
    lesson: props.lesson,
    quizroom: selftestData.value.id,
    student: userStore.user.id,
    question: currentQuestion.value.id,
    data: answerString
  }

  if(!selftestData.value.answered[oldVal - 1]) {

    await api.createAnswer(answer)
        .then((response) => {

          selftestData.value.answered[oldVal - 1] = response.id
        })
        .catch((err) => {
          console.log(err)
        })
  }
  else{
    await api.updateAnswer(selftestData.value.answered[oldVal - 1], answer)
     .catch((err) => {
          console.log(err)
        })
  }

  currentSelectedAnswers.value = []

  await api.questionDetail(selftestData.value.selftestQuestions[currentQuestionIndex.value - 1])
      .then((q) => {
        currentQuestion.value = q
        currentQuestion.value.possibleAnswersData = JSON.parse(q.possibleAnswersData)

        api.studentsAnswerFromQuestionDetail(selftestData.value.id, q.id, userStore.user.id)
            .then(response => {
              if (response.length > 0)
                currentSelectedAnswers.value = JSON.parse(response[0].data)
              else
                currentSelectedAnswers.value = []
            })
            .catch(err => {
              console.log(err)
            })
      })
      .catch((e) => {
        console.log(e)
      })
})

onMounted(async () => {

  tabList.value[TAB_MODULE_DETAIL] = t('$vuetify.quiz_module.quiz')
  selectedTab.value = TAB_MODULE_DETAIL

  await api.getLastQuizroomByStudent(userStore.user.id, props.module.id)
      .then(async res => {
        if (res.roomState){
          await api.selftestDetail(res.id)
              .then(test => {
                selftestData.value = test
                selftestData.value.answered = Array(test.selftestQuestions.length).fill(null);
                selftestState.value = test.quizState

                selftestData.value.selftestQuestions.forEach( (qId) => {
                  api.studentsAnswerFromQuestionDetail(selftestData.value.id, qId, userStore.user.id)
                      .then( a => {
                        if (a.length > 0){
                          selftestData.value.answered[selftestData.value.selftestQuestions.indexOf(qId)] = a[0].id
                        }
                      })
                })

              })
              .then(error => {
                console.log(error)
              })

          await api.questionDetail(selftestData.value.selftestQuestions[selftestData.value.currQuestion])
              .then(async (q) => {
                currentQuestion.value = q
                currentQuestion.value.possibleAnswersData = JSON.parse(q.possibleAnswersData)
                currentQuestionIndex.value = selftestData.value.currQuestion + 1

                await api.studentsAnswerFromQuestionDetail(selftestData.value.id, q.id, userStore.user.id)
                    .then(a => {
                      currentSelectedAnswers.value = JSON.parse(a[0].data)
                    })
                    .catch(error => {
                      console.log(error)
                    })
              })


          isLoaded.value = true
        }
        else{
          selftestData.value = null
          selftestState.value = 'CREATING'
          isLoaded.value = true
        }
      })
      .catch(err => {
        console.log(err)
      })

})


</script>

<template>
  <v-window-item :key="TAB_MODULE_DETAIL" :transition="false" :reverse-transition="false" :value="TAB_MODULE_DETAIL" class="mt-4">
    <div v-if="props.teacher">
      <SelftestStudentResults :module="props.module" :lesson="props.lesson" :teacher="props.teacher" />
    </div>
    <div v-else>
      <div v-if="isLoaded">
        <SelftestGate v-if="selftestState === selftestStateList[0]" :module="props.module" :lesson="props.lesson" />
        <SelftestQuestion v-else-if="selftestState === selftestStateList[1]" />
        <SelftestOverview v-else-if="selftestState === selftestStateList[2]" />
        <SelftestReview v-else-if="selftestState === selftestStateList[3]" />
        <SelftestError v-else class="text-center" />
      </div>
      <div v-else>
        <v-progress-circular indeterminate />
      </div>
    </div>
  </v-window-item>
</template>

<style scoped>

</style>