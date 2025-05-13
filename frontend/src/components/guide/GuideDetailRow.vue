<script setup>
import * as Nav from '@/service/nav'
defineProps(['mdFiles', 'guideId'])
</script>

<template>
  <tbody>
    <tr v-for="mdFile in mdFiles" :key="mdFile.id">
      <td>{{ mdFile.name }}</td>
      <td>
        <router-link :to="new Nav.GuideMarkdown(guideId, mdFile).routerPath()" class="d-flex justify-end text-decoration-none">
          <v-btn variant="text" icon="mdi-arrow-right" />
        </router-link>
      </td>
    </tr>
  </tbody>
</template>