<script setup>
import {onMounted, ref, watch} from "vue";
import TextEditor from "@/components/custom/TextEditor.vue";
import { useLocale } from 'vuetify'
import {getErrorMessage} from "@/plugins/constants";
import {useUserStore} from "@/plugins/store";


const props = defineProps(["translatedMdFiles", "markdownId", "returnRoute"]);
const userStore = useUserStore()

const selectedMdFileContent = ref('');
const mdFiles = ref([])

const error = ref(null)
const { t } = useLocale()


function loadAllMdFiles(){
  mdFiles.value = userStore.locale === 'customEn' ?
      props.translatedMdFiles.mdFilesEn : props.translatedMdFiles.mdFilesCz;
}

function loadMdFile(id) {
  if(!mdFiles.value)
    return
  try{
    selectedMdFileContent.value = mdFiles.value[id].content
  }
  catch (e){
    error.value = e
    console.log(e)
  }
}

onMounted(() => {
      loadAllMdFiles()
      loadMdFile(props.markdownId)
})

watch(() => userStore.locale, () => {
  loadAllMdFiles()
  loadMdFile(0);
});
</script>

<template>
  <v-navigation-drawer>
    <v-card class="mx-auto">
      <v-list density="comfortable">
        <v-list-item :to="returnRoute" link>
          <template v-if="returnRoute" #prepend>
            <v-icon size="small" icon="mdi-arrow-left" />
          </template>
          <template #title>
            <strong>{{ t('$vuetify.chapter_list') }}</strong>
          </template>
        </v-list-item>
        <v-list-item
          v-for="file in mdFiles" :key="file.id"
          :title="`${file.name}`"
          @click="loadMdFile(file.id)" />
        <v-list-item v-if="error" :title="getErrorMessage(t, error)" />
      </v-list>
    </v-card>
  </v-navigation-drawer>

  <v-main>
    <TextEditor v-model="selectedMdFileContent" />
  </v-main>
</template>
