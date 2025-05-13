<script setup>
import {inject, ref} from "vue";
import {useLocale} from "vuetify";
import {useUserStore} from "@/plugins/store";
import {getErrorMessage} from "@/plugins/constants";
import {userApi} from "@/service/api";

const { t } = useLocale()
const appState = inject('appState')
const userStore = useUserStore()

const gitlabTokenData = ref(null)
const displayDialog = inject('displayGitlabTokenUpdateDialog')

gitlabTokenData.value = {
  gitlabToken: userStore.gitlabToken
}

const editGitlabToken = async() => {
  try{
    await userApi.editGitlabToken(userStore.user.id, gitlabTokenData.value)
    displayDialog.value = false
    appState.value.notifications.push({
      type: "success", title: t('$vuetify.user_menu_update_token'), text: t('$vuetify.user_menu_update_token_success')
    })
    userStore.gitlabToken = gitlabTokenData.value.gitlabToken
  } catch (err){
    appState.value.notifications.push({
      type: "error", title: t('$vuetify.user_menu_update_token'),
      text: getErrorMessage(t, err.code)
    })
  }
}
</script>

<template>
  <v-dialog v-model="displayDialog">
    <v-form @submit.prevent="editGitlabToken">
      <v-card :title="t('$vuetify.user_menu_update_token')" class="mx-auto px-6 py-8" width="380px">
        <v-card-text>
          <v-text-field v-model="gitlabTokenData.gitlabToken" :label="t('$vuetify.user_menu_gitlab_token')" />
        </v-card-text>
        <v-card-actions class="d-flex justify-center">
          <v-btn class="mr-15" variant="tonal" color="deep-purple-darken-1" @click="displayDialog=false">
            {{ t('$vuetify.dialog_close') }}
          </v-btn>
          <v-btn class="ml-15" variant="tonal" color="blue" type="submit">
            {{ t('$vuetify.dialog_save') }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-form>
  </v-dialog>
</template>

<style scoped>

</style>