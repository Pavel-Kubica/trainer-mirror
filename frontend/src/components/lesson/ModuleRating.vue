<script setup>
import {onMounted, ref, provide, inject} from 'vue'
import {moduleApi} from '@/service/api'

import LoadingScreen from '@/components/custom/LoadingScreen.vue'

//const appState = inject('appState')
const ratings = ref(null)
const error = ref(null)

const courseSecretDialog = ref(false)
provide('courseSecretDialog', courseSecretDialog)
const module = inject('module')
import {useLocale} from 'vuetify'
import ModuleRatingRow from "@/components/lesson/ModuleRatingRow.vue";

const {t} = useLocale()



onMounted(async () => {
    if (module.value===undefined)
      return
    moduleApi.getModuleRatings(module.value.id)
        .then((result) => {
          //appState.value.navigation = [new Nav.ModuleRating()]
          ratings.value = result;
        })
        .catch((err) => {
          error.value = err.code
        })


});


</script>

<template>
  <v-card class="mx-8 my-4">
    <LoadingScreen v-if="module!==undefined" :items="ratings" :error="error">
      <template #table>
        <ModuleRatingRow v-for="rating in ratings" :key="rating.id"
                         :rating="rating" />
        <tr v-if="!ratings.length">
          <td>{{ t('$vuetify.tab_rating_empty') }}</td>
        </tr>
      </template>
    </LoadingScreen>
  </v-card>
</template>
