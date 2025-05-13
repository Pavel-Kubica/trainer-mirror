<script setup>
  
import {useLocale} from 'vuetify'
import {topicApi} from "@/service/api";
import {inject, onMounted, ref} from "vue";
import TooltipIconButton from "@/components/custom/TooltipIconButton.vue";

const props = defineProps(['topic']);
const {t} = useLocale();
const numberOfModulesWithTopic = ref(0);

//Delete
const deleteDialog = inject('deleteDialog')
const deleteTopic = inject('deleteTopicEntity')

//Edit
const editDialog = inject('editDialog')
const editTopic = inject('editTopicEntity')

function retrieveModules(topic) {
  return new Promise((resolve, reject) => {
    topicApi.topicModulesList(topic.id)
        .then((result) => {
          numberOfModulesWithTopic.value = result.length;
          resolve(result);
        })
        .catch((err) => {
          numberOfModulesWithTopic.value = 0;
          reject(err);
        });
  });
}
onMounted(() => {
  retrieveModules(props.topic);
});
</script>

<template>
  <tr>
    <td>{{ topic.name }}</td>
    <td>
      <span>{{ t("$vuetify.topic_list_modules", numberOfModulesWithTopic) }}</span>
    </td>
    <td style="text-align: right">
      <TooltipIconButton icon="mdi-pencil"
                         @click="(e) => { e.stopPropagation(); editTopic = topic; editDialog = true }" />
      <TooltipIconButton icon="mdi-delete" color="red"
                         @click="(e) => { e.stopPropagation(); deleteTopic = topic; deleteDialog = true }" />
    </td>
  </tr>
</template>
