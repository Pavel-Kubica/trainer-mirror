<script setup>
import LoadingScreen from "@/components/custom/LoadingScreen.vue";
import GuideDetailRow from "@/components/guide/GuideDetailRow.vue";
import guides from "@/resources/guides.js"
import { ref, watch, onMounted} from "vue";
import {useLocale} from "vuetify";
import { useRoute } from "vue-router";
import {useUserStore} from "@/plugins/store";


const { t } = useLocale()
const error = ref("")

const route = useRoute();

const mdFiles = ref([]);
const currentGuideId = ref(null)

const userStore = useUserStore();


// Function to update mdFiles based on guideId
const updateMdFiles = () => {
  const guideId = route.params.guide;
  const guide = guides.find(g => g.id == guideId);
  if (guide) {
    mdFiles.value = userStore.locale === 'customEn' ? guide.mdFilesEn : guide.mdFilesCz;
    currentGuideId.value = guide.id;
  } else {
    mdFiles.value = [];
    currentGuideId.value = null;
    error.value = t('$vuetify.guide_not_found');
  }
};


watch(() => route.params.guide,() => {
  updateMdFiles();
});

watch(
    () => userStore.locale, () => {
      updateMdFiles();
    }
);

onMounted(() => updateMdFiles())
</script>

<template>
  <v-card :title="t('$vuetify.chapters')">
    <LoadingScreen :items="mdFiles" :error="error">
      <template #content>
        <v-table :class="'mt-0'">
          <GuideDetailRow :md-files="mdFiles" :guide-id="currentGuideId" />
        </v-table>
      </template>
    </LoadingScreen>
  </v-card>
</template>
