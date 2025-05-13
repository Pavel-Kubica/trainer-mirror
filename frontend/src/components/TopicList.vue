<script setup>
import {inject, onMounted, provide, ref} from 'vue'
import {topicApi} from '@/service/api'
import TopicListHeader from "@/components/topic/TopicListHeader.vue";
import TopicListDeleteDialog from "@/components/topic/TopicListDeleteDialog.vue";
import LoadingScreen from '@/components/custom/LoadingScreen.vue'
import TopicListRow from "@/components/topic/TopicListRow.vue";
import {useLocale} from 'vuetify'
import TopicListEditDialog from "@/components/topic/TopicListEditDialog.vue";
import * as Nav from "@/service/nav";

const appState = inject('appState')
const topics = ref(null)
const error = ref(null)

//Delete
const deleteDialog = ref(false)
const deleteTopic = ref(null)
provide('deleteDialog', deleteDialog)
provide('deleteTopicEntity', deleteTopic)

//Edit
const editDialog = ref(false)
const editTopic = ref(null)
provide('editDialog', editDialog)
provide('editTopicEntity', editTopic)

const {t} = useLocale()

const loadTopicList = async () => {
  topicApi.listTopics()
      .then((result) => {
        appState.value.navigation = [new Nav.CourseList(),new Nav.TopicList()]
        topics.value = result.sort((a, b) => a.name.localeCompare(b.name));
      })
      .catch((err) => {
        error.value = err.code
      })
}
onMounted(async () => {
  await loadTopicList()
})

</script>

<template>
  <TopicListDeleteDialog :delete-topic="deleteTopic" :callback="loadTopicList" />
  <TopicListEditDialog :edit-topic="editTopic" :callback="loadTopicList" />
  <TopicListHeader :edit-topic="editTopic" :callback="loadTopicList" />
  <v-card class="mx-8 my-4">
    <LoadingScreen :items="topics" :error="error">
      <template #table>
        <TopicListRow v-for="topic in topics" :key="topic.id" :topic="topic" @topicDeleted="loadTopicList" />
        <tr v-if="!topics.length">
          <td>{{ t('$vuetify.topic_list_empty') }}</td>
        </tr>
      </template>
    </LoadingScreen>
  </v-card>
</template>


