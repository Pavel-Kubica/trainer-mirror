<script setup>
import {onMounted, inject, ref} from "vue";
import api from "@/service/quizApi";
import {useUserStore} from "@/plugins/store";
import {useLocale} from "vuetify";
const { t } = useLocale()

const userStore = useUserStore()

const error = ref(null)

const quizName = ref(null)


const tState = inject('tState')
const teacherStates = inject('teacherStates')
const module = inject('moduleData')
const lesson = inject('lesson')
const numOfQuestions = inject('numOfQuestions')
const quizData = inject('quizData')
const quizroomData = inject('quizroomData')
const refreshGuard = inject('refreshGuard')

const emptyQuizAllert = ref(false)



//vytvori na BE novu roomku
const createQuizroom = async () => {

  if(quizData.value.questions.length === 0){
    emptyQuizAllert.value = true
    setTimeout(() => {emptyQuizAllert.value = false}, 5000)

    return
  }

  const quizroom = {
    createdBy: userStore.user.username,
    quiz: quizData.value.id,
    lesson: lesson
  }

  api.createQuizroom(quizroom)
      .then( (q) => {
        quizroomData.value = q

        const teacherInQuizroom = {
          quizroom : quizroomData.value.id,
          student : userStore.user.id
        }

        api.createQuizroomStudent(quizroomData.value.id, teacherInQuizroom)
            .then(() => {
              tState.value = teacherStates[1]
            })
            .catch(() => {
              console.log('error editing state')
            })

      })
      .catch((err) => {
        console.log(err)
        error.value = `Chyba při vytváření: ${err.code}`
        if(err.response.status === 400){
          error.value = 'Aktívna miestnosť s týmto heslom už existuje'
        }

      })

}

onMounted(async () => {

  api.quizDetailByModule(module.id)
      .then((result) => {
        quizData.value = result
        quizName.value = quizData.value.name
        numOfQuestions.value = result.numOfQuestions
        if(!refreshGuard.value){
          refreshGuard.value = true
        }
      })
      .catch((err) => {
        console.log(err)
        error.value = `Chyba při načítání: ${err.code}`
      })
})

</script>

<template>
  <v-card v-if="quizData && quizData.questions.length === 0" variant="tonal" color="blue" class="ma-4">
    <v-row class="align-center justify-center pa-2 ml-4">
      <v-icon icon="mdi-information" />
      <v-card-text>
        {{ t('$vuetify.quiz_module.no_questions_dialog_heading') }}
      </v-card-text>
    </v-row>
  </v-card>

  <v-alert
    v-if="emptyQuizAllert"
    class="ml-3 mt-3 mb-3"
    variant="tonal"
    type="error"
    :title="t('$vuetify.quiz_module.no_questions_dialog_heading')"
    :text="t('$vuetify.quiz_module.no_questions_dialog_text')" />

  <v-card-title class="text-center">
    {{ quizName }}
  </v-card-title>

  <div class="text-center">
    <v-btn class="mt-10" color="green" @click="createQuizroom">
      {{ t('$vuetify.quiz_module.create_room') }}
    </v-btn>
  </div>
</template>

<style scoped>

</style>