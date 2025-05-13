<script setup>
import * as Nav from '@/service/nav'
import {useLocale} from "vuetify";


defineProps(['guides'])
const {t} = useLocale()
</script>


<template>
  <tbody>
    <tr v-for="guide in guides" :key="guide.id">
      <td>{{ t(guide.name) }}</td>
      <td>
        <router-link :to="new Nav.GuideDetail(guide).routerPath()" class="d-flex justify-end text-decoration-none">
          <v-btn variant="text" icon="mdi-arrow-right" />
        </router-link>
      </td>
    </tr>
  </tbody>
</template>


