<script setup>
import {inject, onMounted, onUnmounted, ref} from "vue";
import api from "@/service/quizApi";
import {useUserStore} from "@/plugins/store";
import {useLocale} from "vuetify";
const { t } = useLocale()

const sState = inject('sState')
const studentStates = inject('studentStates')
const quizroomData = inject('quizroomData')
//const user = inject('user')
const quizData = inject('quizData')
const module = inject('moduleData')
const refreshGuard = inject('refreshGuard')
const userStore = useUserStore()

const error = ref(null)
const studEntryCode = ref(null)
const wrongCode = ref(false)
const quizName = ref('')

const enterQuiz = () => {
  if(!studEntryCode.value)
    return

  api.quizroomDetailByPassword(studEntryCode.value)
      .then((result) => {

        if(result.length !== 0){
          quizroomData.value = result[0]
          if(result[0].quiz === quizData.value.id){

            let tmp = quizroomData.value.students

            tmp.push(userStore.user.id)

            const studInQuizroom = {
              quizroom : quizroomData.value.id,
              student : userStore.user.id,
            }

            api.createQuizroomStudent(quizroomData.value.id, studInQuizroom)
                .then( () => {
                  sState.value = studentStates[1]
                })
                .catch((err) => {
                  console.log(err)
                  error.value = `Chyba při načítání: ${err.code}`
                })

          }
          else{
            wrongCode.value = true
            setTimeout(() => {wrongCode.value = false}, 10000)
          }
        }
        else{
          console.log('wrong code')
          wrongCode.value = true
          setTimeout(() => {wrongCode.value = false}, 10000)

        }
      })
      .catch((err) => {
        wrongCode.value = true
        setTimeout(() => {wrongCode.value = false}, 10000)
        console.log(err)
        error.value = `Chyba při načítání: ${err.code}`
      })
}

onMounted(() => {
  sState.value = studentStates[0]

  api.quizDetailByModule(module.id)
      .then((quiz) => {

        if(!refreshGuard.value){
          refreshGuard.value = true
        }

        quizData.value = quiz
        quizName.value = quiz.name
      })
      .catch(() => {
        console.log('Quiz not found')
      })

  wrongCode.value = false
})

onUnmounted(() => {
})
</script>

<template>
  <v-card>
    <v-card-title>{{ t('$vuetify.quiz_module.quiz') }} : {{ quizName }}</v-card-title>
    <v-alert
      v-if="wrongCode"
      icon="mdi-alert-circle"
      class="mb-3 mt-3 text-left"
      variant="tonal"
      type="error"
      :title="t('$vuetify.quiz_module.wrong_code_dialog')"
      :text="t('$vuetify.quiz_module.wrong_code_dialog_text')" />
    <v-text-field v-model="studEntryCode"
                  :placeholder="t('$vuetify.quiz_module.enter_code_placeholder')"
    />
    <v-btn class="mt-3 mb-4" color="blue" @click="enterQuiz">
      {{ t('$vuetify.quiz_module.enter_room') }}
    </v-btn>
  </v-card>
</template>

<style scoped>

</style>