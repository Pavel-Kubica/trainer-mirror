<script setup>
import {inject, ref} from "vue";
import {discussionApi} from "@/service/api";
import {getErrorMessage} from "@/plugins/constants";
import {useLocale} from "vuetify";

const commentData = ref(null)
const appState = inject('appState')
const {t} = useLocale()
const props = defineProps(['moduleId'])

commentData.value = {
  content: "",
  created: ""
}
const loadComments = inject('loadComments')
const translate = (key) => t(`$vuetify.${key}`)

const createPost = async () => {
  try {
    commentData.value.created = Date.now()
    if (commentData.value.content.length < 1 || commentData.value.content.length > 1000) {
      appState.value.notifications.push({
        type: "error", title: t('$vuetify.module_create_comment_title'),
        text: t('$vuetify.module_create_comment_too_long')
      })
      return
    }
    await discussionApi.postComment(props.moduleId, commentData.value)
    appState.value.notifications.push({
      type: "success", title: t('$vuetify.module_create_comment_title'), text: t('$vuetify.module_create_comment_success')
    })
    await loadComments()
    document.getElementById('input_comment').value = ""
  } catch (err) {
    appState.value.notifications.push({
      type: "error", title: t('$vuetify.module_create_comment_title'),
      text: getErrorMessage(t, err.code)
    })
  }
}

</script>

<template>
  <div class="input-div">
    <h4 class="mb-2">
      {{ t('$vuetify.module_comment_input') }}:
    </h4>
    <v-textarea id="input_comment" v-model="commentData.content"
                class="input-comment"
                :label="translate('module_comment_name')"
                outlined
                rows="5"
                counter="1000"
                auto-grow
                :rules="[v => v.length <= 1000 || 'Max 1000 characters']"
                clearable
    />
    <div class="d-flex">
      <v-btn class="mt-2 mb-2 mx-2 pa-2 ml-auto" color="purple" variant="tonal" width="140" @click="createPost">
        Send
      </v-btn>
    </div>
  </div>
</template>

<style scoped>
.input-comment {
  width: 100%;
  border: 2px solid rebeccapurple;
  border-radius: 10px;
  padding: 5px;
  background-color: #eaeaea;
  font-size: 15px;
}

.input-div {
  margin: 5px;
}
</style>