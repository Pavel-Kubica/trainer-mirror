<script setup>
import { inject, ref } from 'vue'
import { useLocale } from 'vuetify'
import * as Nav from '@/service/nav'
import { useUserStore } from '@/plugins/store'
import TooltipIconButton from "@/components/custom/TooltipIconButton.vue";

const appState = inject('appState')
const moduleData = inject('moduleData')
const moduleEditItem = inject('moduleEditItem')
const moduleEditTabList = inject('moduleEditTabList')

const collapsed = ref({})
const props = defineProps(['lesson', 'module'])
const userStore = useUserStore()
const { t } = useLocale()

const selectItem = (item) => {
  if (item.tabList) {
    collapsed.value[item.id] = !collapsed.value[item.id]
    return
  }
  moduleEditItem.value = item
  if (!props.module) return
  userStore.moduleEditItemIds[props.module.id] = item.id
}
</script>

<template>
  <v-navigation-drawer v-if="moduleData" v-model="appState.leftDrawer">
    <v-list v-model="moduleEditItem" density="comfortable">
      <v-list-item :to="module ? new Nav.ModuleDetail(lesson, module).routerPath() : new Nav.LessonDetail(lesson).routerPath()">
        <template #prepend>
          <v-icon size="small" icon="mdi-arrow-left" />
        </template>
        <template #title>
          <strong>{{ lesson.name }}</strong>
        </template>
      </v-list-item>

      <template v-for="item in moduleEditTabList" :key="item.id">
        <v-list-item :title="t(item.name)" :active="moduleEditItem.id === item.id" @click="() => selectItem(item)">
          <template #prepend>
            <v-icon size="small" :icon="item.icon" />
          </template>
          <template v-if="item.tabList?.length" #append>
            <v-btn density="compact" variant="text" :icon="!collapsed[item.id] ? 'mdi-chevron-up' : 'mdi-chevron-down'" />
          </template>
          <template v-else-if="item.guideLink" #append>
            <TooltipIconButton :to="item.guideLink" target="_blank" density="compact" icon="mdi-information-outline" :tooltip="t('$vuetify.module_edit_guide_link')" @click.stop="" />
          </template>
        </v-list-item>

        <v-expand-transition v-if="item.tabList?.length">
          <div v-if="!collapsed[item.id]">
            <v-list-item v-for="tabItem in item.tabList" :key="tabItem.id" :title="tabItem.name"
                         :active="moduleEditItem.id === tabItem.id" style="padding-inline-start: 32px !important" @click="() => selectItem(tabItem)" />
          </div>
        </v-expand-transition>
      </template>
    </v-list>
  </v-navigation-drawer>
</template>
