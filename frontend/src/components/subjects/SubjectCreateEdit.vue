<script setup>
import {ref, inject, onMounted, provide, watch} from 'vue'
import {useLocale} from 'vuetify'
import {onBeforeRouteLeave} from 'vue-router'
import router from '@/router'
import {subjectApi, userApi} from '@/service/api'
import * as Nav from '@/service/nav'


import LoadingScreen from '@/components/custom/LoadingScreen.vue'

import UnsavedChangesDialog from '@/components/lesson/UnsavedChangesDialog.vue'
import SubjectCreateEditInformation from "@/components/subjects/SubjectCreateEditInformation.vue";
import {useUserStore} from "@/plugins/store";

const appState = inject('appState')
const props = defineProps(['subjectId'])
const {t} = useLocale()
const error = ref(null)
const userStore = useUserStore()
const subject = ref(null)
const guarantors = ref([])
const subjectGuarantors = ref([])
const alreadySelectedGuarantors = ref([])
const subjectData = ref(null)
const createEditCallback = ref((subject))
const routerNext = ref(() => {
})
const unsavedChangesDialog = ref(false)

provide('subjectGuarantors', subjectGuarantors)
provide('subjectData', subjectData)
provide('unsavedChangesDialog', unsavedChangesDialog)
provide('createEditCallback', createEditCallback)



const reload = async () => {
  subjectApi.subjectDetail(subject.value.id)
      .then((result) => {
        subject.value = subjectData.value = result
        alreadySelectedGuarantors.value = subjectGuarantors.value
      })
      .catch((err) => {
        error.value = err.code
      })
}

const submitAllowed = () => {
  return subjectData.value.name && (subject.value || subjectData.value)
}

const submit = () => {
  if (!submitAllowed())
    return false

  const editing = subject.value !== null
  const subjectDataVal = subjectData.value;
  const promise = editing ? subjectApi.editSubject(props.subjectId, subjectDataVal) : subjectApi.createSubject(subjectDataVal)
  promise
      .then(async (result) => {
        const key = editing ? 'edit' : 'create'
        appState.value.notifications.push({
          type: "success", title: t(`$vuetify.subject_${key}_notification_title`),
          text: t(`$vuetify.subject_${key}_notification_text`),
        })
        subjectApi.listMeGuarantor().then((result) => {
          userStore.realUser.isGuarantor = result
        })
        if (userStore.realUser.username === userStore.user.username) {
          userStore.user = userStore.realUser
        }
        if (subject.value) await reload()
        else {
          await router.push(new Nav.SubjectEdit(result).routerPath())
        }

      })
      .catch((err) => {
        error.value = err.code
      })
}


const loadSubjectEdit = async () => {
  if (props.subjectId) {
    subjectApi.subjectDetail(props.subjectId)
        .then(async (result) => {
          guarantors.value = await userApi.getAllUsers();
          appState.value.navigation = [new Nav.SubjectEdit(result)]
          subject.value = result
          subjectData.value = Object.assign({}, result)
        })
        .catch((err) => {
          console.log("error")
          error.value = err.code
        })
    alreadySelectedGuarantors.value = await subjectApi.subjectGuarantors(props.subjectId)
    subjectGuarantors.value = alreadySelectedGuarantors.value
  } else {
    appState.value.navigation = [new Nav.SubjectCreate()]
    subject.value = null
    subjectData.value = {
      code: "",
      name: "",
    }
  }
  onBeforeRouteLeave((to, from, next) => {
    if (JSON.stringify(subject.value) === JSON.stringify(subjectData.value) || to.name === 'subject-edit') { // ok
      next()
      return
    }
    appState.value.leftDrawer = true
    routerNext.value = next
    unsavedChangesDialog.value = true
  })
}


onMounted(async () => {
  await loadSubjectEdit()
})

watch(props, async () => {
  await loadSubjectEdit()
})
</script>

<template>
  <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0">
  <UnsavedChangesDialog :router-callback="routerNext" :save-callback="submit" />
  <v-card :title="t('$vuetify.subject_create_edit_title')">
    <LoadingScreen :items="subjectData" :error="error">
      <template #items>
        <v-card-item>
          <v-form @submit.prevent="submit">
            <SubjectCreateEditInformation :guarantors="guarantors" />
            <v-btn class="mb-2 mx-2" type="submit" color="blue" size="large" variant="tonal"
                   :block="true" :disabled="!submitAllowed()">
              {{ t(`$vuetify.action_${subject ? 'edit' : 'create'}`) }}
            </v-btn>
          </v-form>
        </v-card-item>
      </template>
    </LoadingScreen>
  </v-card>
</template>

<style>
.guarantor-table th, .guarantor-table td {
  padding-top: 10px;
  padding-bottom: 5px;
  padding-left: 20px; /* Moves all content in the cells to the right */
  text-align: left; /* Aligns text to the left */
}
</style>
