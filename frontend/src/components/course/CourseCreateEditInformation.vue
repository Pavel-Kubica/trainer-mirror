<script setup>
import {inject, watch} from 'vue'
import { useLocale } from 'vuetify'



const props = defineProps(["teachers", "reload"]);
const courseData = inject('courseData')
const courseTeachers = inject('courseTeachers')
//const userStore = useUserStore()
const { t } = useLocale()

const translate = (key) => t(`$vuetify.course_create_edit_${key}`)

watch(courseTeachers, () => {
  courseData.value.users = courseTeachers.value
})


</script>

<template>
  <v-text-field v-model="courseData.name" :label="translate('name')" />
  <v-text-field v-model="courseData.secret" :label="translate('code')" />
  <v-text-field v-model="courseData.shortName" :label="translate('short_name')" />

  <div class="d-flex mt-4" style="gap: 10px">
    <v-autocomplete v-model="courseTeachers" multiple :items="teachers" item-title="username"
                    :item-value="teacher => ({ id: teacher.id, username: teacher.username })"
                    :label="translate('teachers')"
                    :no-data-text="translate('no_users')"
                    @update:search="props.reload">
      <template #selection="{item}">
        <v-chip>
          <span>{{ item.title }}</span>
        </v-chip>
      </template>
    </v-autocomplete>
  </div>
  <v-switch v-model="courseData.public" class="ps-2" color="primary"
            :label="translate('public')" />
</template>
