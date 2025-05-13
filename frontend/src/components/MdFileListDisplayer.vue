<script setup>
import {onMounted, ref, inject, watch} from "vue";
import TextEditor from "@/components/custom/TextEditor.vue";
import { useLocale } from 'vuetify'
import {getErrorMessage} from "@/plugins/constants";
import {useUserStore} from "@/plugins/store";


const props = defineProps(["mdFiles", "markdownId", "returnRoute"]);
const userStore = useUserStore()

const appState = inject('appState')
const selectedMdFileContent = ref('');
const error = ref(null)
const { t } = useLocale()


function loadMdFile(id) {
  if (!props.mdFiles)
    return
  try {
    selectedMdFileContent.value = props.mdFiles[id].content
  }
  catch (e) {
    error.value = e
    console.error(e)
  }
}

onMounted(() => {
      loadMdFile(props.markdownId)
})

watch(() => props.markdownId, () => {
  loadMdFile(props.markdownId)
})
watch(() => userStore.locale, () => {
  loadMdFile(props.markdownId);
});
</script>

<template>
  <v-navigation-drawer v-model="appState.leftDrawer">
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
