<script setup>
import {inject} from "vue";
import {useLocale} from "vuetify";

const { t } = useLocale()
defineProps(['subject'])
const displayDeleteDialog = inject('displayDeleteDialog')
const emit = defineEmits(['deleteButtonClicked'])

</script>


<template>
  <v-dialog v-model="displayDeleteDialog" width="300px">
    <v-card
      :title="t('$vuetify.subject_delete')"
      :text="`${t('$vuetify.subject_delete_question')} ${subject.name}?`">
      <v-card-actions class="d-flex justify-center">
        <v-btn variant="tonal" color="deep-purple-darken-1" class="mr-13" @click="displayDeleteDialog=false">
          {{ t('$vuetify.dialog_close') }}
        </v-btn>
        <v-btn variant="tonal" color="red" class="ml-13" @click="emit('deleteButtonClicked')">
          {{ t('$vuetify.action_delete') }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>