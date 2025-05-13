<script setup>
import {inject, onMounted, ref} from "vue";
import api from "@/service/quizApi";
import {useUserStore} from "@/plugins/store";
import {useLocale} from "vuetify";

const { t } = useLocale()


const userStore = useUserStore()
const props = defineProps(['module', 'lesson'])

const quizModuleData = ref(null)
const selftestLength = ref(1)
const possibleTimeControls = ref(['EASY','MEDIUM',/*'REAL',*/ 'HARD', 'HELL'])
const timeControl = ref('EASY')

const selftestState = inject('selftestState')
const selftestStateList = inject('selftestStateList')
const selftestData = inject('selftestData')

const currentQuestionIndex = inject('currentQuestionIndex')
const currentQuestion = inject('currentQuestion')

const startSelftest = async () => {

  if(!selftestLength.value || selftestLength.value < 1 || !timeControl.value)
    return

  const selftest = {
    createdBy: userStore.user.username,
    quiz: quizModuleData.value.id,
    lesson: props.lesson,
    selftestLength: selftestLength.value,
    timeControl: timeControl.value
  }

  api.createSelftest(selftest)
      .then(async (res) => {

        selftestData.value = res

        selftestData.value.answered = Array(res.selftestQuestions.length).fill(null);

        api.questionDetail(res.selftestQuestions[0])
            .then((q) => {
              currentQuestion.value = q
              currentQuestion.value.possibleAnswersData = JSON.parse(q.possibleAnswersData)
              selftestLength.value = 1
              timeControl.value = 'EASY'
              selftestState.value = selftestStateList.value[1]
              currentQuestionIndex.value = 1
            })
      })
      .catch((err) => {
        console.log(err)
      })


}

onMounted(() => {

  api.quizDetailByModule(props.module.id)
      .then((res) => {
        quizModuleData.value = res
      })
      .catch((err) => {
        console.log(err)
      })

})
</script>

<template>
  <v-card-title v-if="quizModuleData" class="text-center">
    {{ t('$vuetify.selftest_module.selftest') }} : {{ quizModuleData.name }}
  </v-card-title>

  <v-row>
    <v-col />
    <v-col cols="4">
      <v-text-field v-model="selftestLength" class="ma-2 mt-8" type="number" :label="t('$vuetify.selftest_module.number_of_questions')" />
      <div>
        <v-card v-if="quizModuleData && selftestLength > quizModuleData.questions.length" variant="tonal" color="blue" class="ma-2">
          <v-row class="align-center justify-center pa-2 ml-4">
            <v-icon icon="mdi-information" />
            <v-card-text>
              {{ t('$vuetify.selftest_module.too_many_questions') }}
            </v-card-text>
          </v-row>
        </v-card>
        <v-card v-else-if="selftestLength < 1 || !selftestLength" variant="tonal" color="blue" class="ma-2">
          <v-row class="align-center justify-center pa-2 ml-4">
            <v-icon icon="mdi-information" />
            <v-card-text>
              {{ t('$vuetify.selftest_module.invalid_number_of_questions') }}
            </v-card-text>
          </v-row>
        </v-card>
      </div>
    </v-col>
    <v-col cols="2" />
    <v-col cols="4">
      <v-select v-model="timeControl" class="ma-2 mt-8" :items="possibleTimeControls" :label="t('$vuetify.selftest_module.time_difficulty')" />
    </v-col>
    <v-col />
  </v-row>



  <div class="text-center">
    <v-btn class="ma-2 mt-4 text-center" color="green" @click="startSelftest()">
      {{ t('$vuetify.selftest_module.launch') }}
    </v-btn>
  </div>
</template>

<style scoped>

</style>