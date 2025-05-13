<script setup>

import {inject, provide, ref, watch} from "vue";
import {useUserStore} from "@/plugins/store";
import {discussionApi} from "@/service/api";
import {getErrorMessage} from "@/plugins/constants";
import {useLocale} from "vuetify";
import DeleteDialog from "@/components/custom/DeleteDialog.vue";

const appState = inject('appState')
const {t} = useLocale()
const userStore = useUserStore()

const props = defineProps(['comment', 'lang', 'moduleId'])
const loadComments = inject('loadComments')
const commentDeleteDialog = ref(false)
const displayRedactDialog = ref(false)
const commentData = ref(null)

provide('deleteDialog', commentDeleteDialog)

commentData.value = {
  content: props.comment.content,
  created: ""
}

const deleteComment = async () => {
  commentDeleteDialog.value = false
  try {
    await discussionApi.deleteComment(props.moduleId, props.comment.id)
    appState.value.notifications.push({
      type: "success", title: t('$vuetify.module_delete_comment_title'), text: t('$vuetify.module_delete_comment_success')
    })
    await loadComments()
  } catch (err) {
    appState.value.notifications.push({
      type: "error", title: t('$vuetify.module_delete_comment_title'),
      text: getErrorMessage(t, err.code)
    })
  }
}

const updateComment = async () => {
  displayRedactDialog.value = false
  commentData.value.created = Date.now()
  try {
    await discussionApi.updateComment(props.moduleId, props.comment.id, commentData.value)
    appState.value.notifications.push({
      type: "success", title: t('$vuetify.module_update_title'), text: t('$vuetify.module_update_title_success')
    })
    await loadComments()
  } catch (err) {
    appState.value.notifications.push({
      type: "error", title: t('$vuetify.module_update_title'),
      text: getErrorMessage(t, err.code)
    })
  }
}

watch(displayRedactDialog, (newValue) => {
  if (newValue === true){
    commentData.value.content = props.comment.content
  }
})
</script>

<template>
  <DeleteDialog title="$vuetify.module_delete_comment_title"
                text-start="$vuetify.module_delete_comment_text"
                item-name=""
                text-before-line-break=""
                text-second-line="$vuetify.irreversible_action"
                :on-cancel="() => commentDeleteDialog = false"
                :on-confirm="() => deleteComment()"
                text-confirm-button="$vuetify.action_delete" />
  <div class="comment-div">
    <div class="d-flex">
      <h4>
        <v-icon size="x-large" style="padding-right: 10px; padding-left: 10px">
          mdi-account
        </v-icon>
        {{ comment.author.username }} ({{ comment.author.name }})
      </h4>
      <v-btn v-if="(userStore.user.username === comment.author.username) && displayRedactDialog === false"
             class="ml-auto" size="small"
             variant="text"
             color="blue" icon="mdi-pencil"
             @click="displayRedactDialog = true" />
      <v-btn v-if="(userStore.user.username === comment.author.username) && displayRedactDialog === false" size="small"
             variant="text" color="red"
             icon="mdi-trash-can"
             @click="commentDeleteDialog = true" />
    </div>
    <span v-if="displayRedactDialog === false" class="comment-text">{{ comment.content }}</span>
    <v-textarea v-if="displayRedactDialog === true" id="input_comment"
                v-model="commentData.content"
                outlined
                counter="1000"
                auto-grow
                :rules="[v => v.length <= 1000 || 'Max 1000 characters']"
                clearable
    />
    <v-btn v-if="displayRedactDialog === true" icon="mdi-check" size="small" variant="text" color="green"
           @click="updateComment" />
    <v-btn v-if="displayRedactDialog === true" icon="mdi-close" size="small" variant="text" color="red"
           @click="displayRedactDialog=false" />
    <br>
    <span class="comment-time">{{
      new Date(comment.created).toLocaleString(lang, {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: 'numeric',
        minute: 'numeric'
      })
    }}<i>{{ comment.redacted === true ? " (edited)" : "" }}</i></span>
  </div>
</template>

<style scoped>

.comment-div {
  display: inline-block;
  width: 100%;
  border: 2px solid rebeccapurple;
  border-radius: 10px;
  background-color: #eaeaea;
  padding: 10px;
}

.comment-text {
  font-size: 15px;
  text-align: justify;
  float: left;
}

.comment-time {
  font-size: 13px;
  float: right;
}
</style>