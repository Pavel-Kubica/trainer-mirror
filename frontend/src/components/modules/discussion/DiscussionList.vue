<script setup>

import LoadingScreen from "@/components/custom/LoadingScreen.vue";
import {ref, onMounted, provide} from "vue";
import {discussionApi} from "@/service/api";
import {useLocale} from "vuetify";
import {useUserStore} from "@/plugins/store";
import DiscussionComment from "@/components/modules/discussion/DiscussionComment.vue";
import DiscussionInput from "@/components/modules/discussion/DiscussionInput.vue";


const comments = ref([])
const commentNum = ref(0)
const maxSize = ref(3)
const props = defineProps(['moduleId'])
const error = ref('')
const {t} = useLocale()
const userStore = useUserStore()

const loadComments = async () => {
  await discussionApi.getCommentsBasedOnModule(props.moduleId).then((result) => {
        comments.value = result
        commentNum.value = result.length
        comments.value = comments.value.slice(0, maxSize.value)
      }
  )
      .catch((err) => {
        error.value = err.code
      })
}

onMounted(async () => {
  await loadComments()
})

provide('loadComments', loadComments)

</script>

<template>
  <v-card min-width="500" max-height="500" max-width="500">
    <LoadingScreen :items="comments" :error="error">
      <template #content>
        <div class="d-flex">
          <h2>{{ t('$vuetify.module_edit_discussion') }}</h2>
          <v-btn v-if="commentNum > 3" class="ml-auto" size="small" color="purple" variant="tonal"
                 @click="maxSize === commentNum ? maxSize = 3 : maxSize = commentNum; loadComments()">
            {{ maxSize === commentNum ? t('$vuetify.module_show_less') : t('$vuetify.module_show_more') }}
          </v-btn>
        </div>
        <v-divider />
        <v-list :items="comments">
          <v-list-item v-for="comment in comments" :key="comment.id" style="padding: 5px;">
            <DiscussionComment :module-id="moduleId" :comment="comment" :lang="userStore.locale === 'customCs' ? 'cs-CZ' : 'en-US'" />
          </v-list-item>
          <v-divider style="margin-top: 10px" />
          <DiscussionInput :module-id="moduleId" />
        </v-list>
      </template>
    </LoadingScreen>
  </v-card>
</template>

<style scoped>
</style>