<script setup>
import Datepicker from '@vuepic/vue-datepicker'
import { cs, enGB } from 'date-fns/locale'
import { useUserStore } from '@/plugins/store'
import { useLocale } from 'vuetify'

const { current } = useLocale()
const userStore = useUserStore()

const props = defineProps({
  withTime: {
    type: Boolean,
    default: false
  }
})

const getLocaleStr = (current) => {
  if (current === 'customCs') return 'cz'
  return 'en'
}
const getLocaleFormat = (current) => {
  if (current === 'customCs') return cs
  return enGB
}
</script>

<template>
  <!-- without time -->
  <Datepicker v-if="!props.withTime" :locale="getLocaleStr(current)" :format-locale="getLocaleFormat(current)" :enable-time-picker="false"
              :teleport-center="true" format="yyyy-MM-dd" :dark="userStore.darkMode" auto-apply text-input />
  <!-- with time -->
  <Datepicker v-else :locale="getLocaleStr(current)" :format-locale="getLocaleFormat(current)"
              :teleport-center="true" :time-picker-inline="true" format="yyyy-MM-dd HH:mm"
              :minutes-grid-increment="15" :dark="userStore.darkMode" auto-apply partial-flow text-input />
</template>