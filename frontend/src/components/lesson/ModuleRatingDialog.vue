<script setup>

import {ref, inject, watch, onMounted} from "vue";
import {moduleRatingApi} from "@/service/api";
import {useLocale} from "vuetify";
import StarRating from "@/components/custom/StarRating.vue";
import {useUserStore} from "@/plugins/store";

const props = defineProps(['moduleId', 'lessonId', 'callback'])
const {t} = useLocale()
const appState = inject('appState')

const displayRatingDialog = inject('displayRatingDialog')

const error = ref(null)
const loading = ref(false)
const module = inject('module')
const ratingData = ref(null)
const userRating = ref(0);
const rating = ref(null);
const userStore = useUserStore();
const submitAllowed = () => {
  return module.value !== undefined && module && ratingData.value
}

const translate = (key) => t(`$vuetify.rating_edit_${key}`)
const loaded = () => {
  console.log("MODULE - ", module)
  updateRating(0)
  setTimeout(() => {
    if (module.value !== undefined && module.value !== null){
      console.log("module ratings - ", module.value.ratings)
      console.log("find st id - ", userStore.user.id)
      rating.value = module.value.ratings.find((rat) => rat.student.id === parseInt(userStore.user.id));
      if (rating.value) {
        console.log("rating - ", rating.value)
        ratingData.value = Object.assign({}, rating.value)
        updateRating(rating.value.points)
      } else {
        ratingData.value = {
          moduleId: module.value.id, studentId: userStore.user.id, points: 0, text: "",
        }
    }
    } else {
      ratingData.value = {
        points: 0, text: "",
      }
      updateRating(ratingData.value.points)
    }
    loading.value = false
  }, 500);

}


const submit = () => {
  if (!submitAllowed()) {
    return false
  }
  loading.value = true
  ratingData.value.points = userRating.value
  rating.value = module.value.ratings.find((rat) => rat.student.id === parseInt(userStore.user.id))
  console.log("rating - ", rating.value)
  const promise = module.value !== undefined && module.value !== null && rating.value !== undefined ? moduleRatingApi.editRating(rating.value.id, ratingData.value) : moduleRatingApi.createRating(ratingData.value)
  promise
      .then(async () => {
        const key = module.value !== undefined && module.value !== null && rating.value !== null ? 'edit' : 'create'
        appState.value.notifications.push({
          type: "success", title: t(`$vuetify.rating_${key}_notification_title`),
          text: t(`$vuetify.rating_${key}_notification_text`),
        })
        props.callback()
        loading.value = false
        //displayRatingDialog.value = false
      })
      .catch((err) => {
        error.value = err.code
      })
  displayRatingDialog.value = false
}


const updateRating = (newRating) => {
  userRating.value = newRating;
  if (newRating)
    ratingData.value.points = newRating;
};

watch(props, loaded)
onMounted(loaded)

</script>


<template>
  <v-dialog v-model="displayRatingDialog" width="500">
    <v-card class="pb-6" :title="translate('dialog_edit')">
      <v-card-item v-if="loading" class="flex justify-center">
        <v-progress-circular indeterminate />
      </v-card-item>
      <v-card-item v-else>
        <v-form @submit.prevent="submit">
          <div>
            <StarRating v-model="ratingData.points" :max-stars="5" :value="userRating" @ratingData="updateRating" />
          </div>
          <v-textarea v-if="ratingData" v-model="ratingData.text" :rows="3" :autofocus="true"
                      @keydown.enter.prevent="submit" />
          <div class="d-flex justify-space-between">
            <v-btn type="button" color="red" variant="tonal"
                   @click="displayRatingDialog = false">
              {{ t('$vuetify.dialog_close') }}
            </v-btn>
            <v-btn type="button" color="green" variant="tonal"
                   :disabled="!ratingData.text.length" @click="submit">
              {{ t(`$vuetify.dialog_save`) }}
            </v-btn>
          </div>
        </v-form>
      </v-card-item>
    </v-card>
  </v-dialog>
</template>
