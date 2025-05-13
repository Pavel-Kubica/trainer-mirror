<script setup>
import {inject, watch} from 'vue'
import { useLocale } from 'vuetify'



const props = defineProps({
  guarantors: Array
});
const subjectData = inject('subjectData')
const subjectGuarantors = inject('subjectGuarantors')
//const userStore = useUserStore()
const { t } = useLocale()
const translate = (key) => t(`$vuetify.subject_create_edit_${key}`)


const isSame = (arr1, arr2) => {
  const sorted1 = arr1.sort()
  const sorted2 = arr2.sort()
  if (sorted1.length !== sorted2.length) return false
  for (let ix of sorted1)
    if (sorted1[ix] !== sorted2[ix])
      return false
  return true
}


watch(subjectGuarantors, (selected, previous) => {
  const guarantorIds = props.guarantors.map((t) => t.id)
  const guarantorsNoAll = guarantorIds.filter((x) => x !== -1)

  // Clicked "all teachers"
  if (selected.includes(-1) && !previous.includes(-1))
    subjectGuarantors.value = guarantorIds
  // Unclicked "all teachers" manually
  if (!selected.includes(-1) && previous.includes(-1) && isSame(previous.filter((x) => x !== -1), guarantorsNoAll))
    subjectGuarantors.value = []

  // Unselected a teacher = deactivate "all teachers"
  if (selected.includes(-1) && !isSame(subjectGuarantors.value.filter((x) => x !== -1), guarantorsNoAll))
    subjectGuarantors.value = subjectGuarantors.value.filter((x) => x !== -1)
  // Selected all teachers = activate "all teachers"
  if (!selected.includes(-1) && isSame(subjectGuarantors.value, guarantorsNoAll))
    subjectGuarantors.value = guarantorIds

  subjectData.value.users = subjectGuarantors.value.filter((x) => x !== -1)
})
/*
watch(courseTeachers, () => {
  courseData.value.users = courseTeachers.value.filter((x) => x !== -1)
})



onMounted(() => courseTeachers.value = courseData.value.users || [])
*/

</script>

<template>
  <v-text-field v-model="subjectData.name" :label="translate('name')" />
  <v-text-field v-model="subjectData.code" :label="translate('code')" />
  <div class="d-flex mt-4" style="gap: 10px">
    <v-autocomplete v-model="subjectGuarantors" multiple :items="guarantors" item-title="username" item-value="id"
                    :label="translate('guarantors')"
                    :no-data-text="translate('no_users')">
      <template #selection="{item }">
        <v-chip>
          <span>{{ item.title }}</span>
        </v-chip>
      </template>
    </v-autocomplete>
  </div>
</template>
