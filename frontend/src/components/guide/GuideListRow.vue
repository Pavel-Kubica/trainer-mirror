<script setup>
import * as Nav from '@/service/nav'
import {useUserStore} from "@/plugins/store";


defineProps(['guides'])
const userStore = useUserStore();
const getGuideName = (guide) => {
  return userStore.locale === 'customEn' ? guide.nameEn : guide.nameCz;
};
</script>


<template>
  <tbody>
    <tr v-for="guide in guides" :key="guide.id">
      <td>{{ getGuideName(guide) }}</td>
      <td>
        <router-link :to="new Nav.GuideDetail(guide).routerPath()" class="d-flex justify-end text-decoration-none">
          <v-btn variant="text" icon="mdi-arrow-right" />
        </router-link>
      </td>
    </tr>
  </tbody>
</template>


