<script setup>
import {createApp, inject, shallowRef, h, ref, watch, onMounted} from 'vue'
import { Codemirror } from 'vue-codemirror'
import { useLocale, useTheme } from 'vuetify'
import { cpp } from '@codemirror/lang-cpp'
import { oneDark } from '@codemirror/theme-one-dark'
import { charCounter } from '@/plugins/codemirror-charcount'
import { gutter, GutterMarker } from '@codemirror/view'
import { VTextarea } from "vuetify/components";
import vuetify from "@/plugins/vuetify";
import {CODE_MODULE_CODE_COMMENT_MAX_LENGTH} from "@/plugins/constants";

const { t } = useLocale()
const theme = useTheme()
const codeData = inject('codeData')
const props = defineProps(['codeKey', 'fileId', 'fileKey', 'disabled'])
const darkMode = () => theme.global.name.value.endsWith('Light') ? [] : [oneDark]
const charCtr  = () => charCounter(t, codeData.value.files[props.fileId].codeLimit)

const view = shallowRef()
const handleReady = (editor) => {
  view.value = editor.view
}

const updateKey = ref(0)
let gutters = []
const prepareCommentGutters = () => {
  const commentTexts = (codeData.value.comments ?
      codeData.value.comments
          .filter(c => c.fileName === codeData.value.files[props.fileId].name)
          .reduce((acc, c) => {
            acc[c.rowNumber] = c.comment
            return acc
          }, {})
      : {})
  const newComments = {}
  if (codeData.value.creatingComments) {
    codeData.value.newComments[codeData.value.files[props.fileId].name] = newComments
  }

  const commentGutter = gutter({
    lineMarker(view, line) {
      const rowNumber = view.state.doc.lineAt(line.from).number
      return rowNumber in commentTexts ? new CommentMarker(commentTexts[rowNumber]) : null
    },
    initialSpacer: () => new CommentMarker("")
  })

  let lineMarkers = {}

  const createCommentGutter = [
    gutter({
      lineMarker(view, line) {
        const rowNumber = view.state.doc.lineAt(line.from).number
        if (!(rowNumber in lineMarkers)) {
          lineMarkers[rowNumber] = new CreateCommentMarker(CreateCommentButtonState.UNSET, "", rowNumber, newComments)
        }
        return lineMarkers[rowNumber]
      },
      initialSpacer: () => new CreateCommentMarker(),
      domEventHandlers: {
        mousedown(view, line) {
          const rowNumber = view.state.doc.lineAt(line.from).number
          lineMarkers[rowNumber].onClicked()
          return true
        }
      }
    })
  ]

  gutters = []
  if (codeData.value.creatingComments) gutters.push(createCommentGutter)
  if (Object.keys(commentTexts).length > 0) gutters.push(commentGutter)
  updateKey.value += 1
}



watch(() => codeData.value.comments, () => prepareCommentGutters())

class CommentMarker extends GutterMarker {
  constructor(comment) {
    super()
    this.comment = comment
  }

  toDOM() {
    const noteIcon = document.createElement("div")
    noteIcon.className = "codeCommentIcon"
    noteIcon.textContent = "📝"
    noteIcon.style.cursor = "pointer"

    const tooltip = document.createElement("span")
    tooltip.className = "codeCommentText"
    tooltip.textContent = this.comment

    noteIcon.appendChild(tooltip)
    return noteIcon
  }
}



const CreateCommentButtonState = Object.freeze({
  UNSET:   1,
  SET:     2,
  ACTIVE:  3
});

class CreateCommentMarker extends GutterMarker {
  constructor(state, comment, rowNumber, newComments) {
    super()
    this.comment = ref(comment)
    this.state = state
    this.rowNumber = rowNumber
    this.newComments = newComments
  }

  toDOM() {
    this.createIcon = document.createElement("div")
    this.createIcon.className = "createCodeCommentIcon"
    this.createIcon.textContent = "✏️"
    this.createIcon.style.cursor = "pointer"
    this.createIcon.title = t("$vuetify.code_module.create_code_comment")

    this.tooltip = document.createElement("div")
    this.tooltip.className = "createCodeCommentInput"
    this.tooltip.addEventListener("focusout", () => this.onTooltipBlur())
    this.tooltip.addEventListener("mousedown", e => e.stopPropagation())
    this.createIcon.appendChild(this.tooltip)

    const comment = this.comment
    const textarea = createApp({
      render() {
        return h(VTextarea, {
          label: t("$vuetify.code_module.code_comment"),
          variant: "solo",
          bgColor: "transparent",
          color: "white",
          modelValue: comment,
          "onUpdate:focused": (focused) => {
            if (!focused) {
              this.$emit("focusout")
            }
          },
          autofocus: true,
          maxLength: CODE_MODULE_CODE_COMMENT_MAX_LENGTH,
          hideDetails: true,
          rows: 3,
        });
      }
    });

    textarea.use(vuetify);
    textarea.mount(this.tooltip);

    this.update()

    return this.createIcon
  }

  update() {
    switch (this.state) {
      case CreateCommentButtonState.UNSET:
        this.unset()
        break
      case CreateCommentButtonState.SET:
        this.set()
        break
      case CreateCommentButtonState.ACTIVE:
        this.active()
        break
      default:
        break
    }
  }

  unset() {
    this.state = CreateCommentButtonState.UNSET
    this.tooltip.style.display = "none"
    this.createIcon.classList.remove("active")
  }

  active() {
    this.state = CreateCommentButtonState.ACTIVE
    this.tooltip.style.display = "inline"
    this.createIcon.classList.add("active")
    this.tooltip.querySelector("textarea").focus()
  }

  set() {
    this.state = CreateCommentButtonState.SET
    this.tooltip.style.display = "none"
    this.createIcon.classList.add("active")
  }

  onClicked() {
    if (this.state === CreateCommentButtonState.UNSET) {
      this.active()
    }
    else if (this.state === CreateCommentButtonState.SET) {
      this.active()
    }
  }

  onTooltipBlur() {
    if (this.state === CreateCommentButtonState.ACTIVE) {
      if (this.comment.value.trim() === "") {
        this.unset()
        // Remove the comment
        delete this.newComments[this.rowNumber]
      } else {
        this.set()
        // Save the comment
        this.newComments[this.rowNumber] = this.comment.value
      }
    }
  }
}

onMounted(() => {
  if (!props.codeKey)
    prepareCommentGutters()
})

</script>

<template>
  <v-theme-provider with-background>
    <codemirror v-if="codeKey" v-model="codeData[codeKey]" :autofocus="true" :indent-with-tab="true" :tab-size="4"
                :disabled="disabled" :extensions="[cpp()].concat(darkMode())" />
    <codemirror v-else :key="updateKey" v-model="codeData.files[fileId][fileKey]" :autofocus="true" :indent-with-tab="true"
                :tab-size="4" :disabled="disabled" :extensions="gutters.concat([cpp(), charCtr()]).concat(darkMode())"
                @ready="handleReady"
    />
  </v-theme-provider>
</template>

<style>
.codeCommentIcon {
  cursor: pointer;
}

.codeCommentIcon > .codeCommentText {
  visibility: hidden;
  background-color: #000000CC;
  color: #fff;
  text-align: left;
  padding: 5px 1em;
  border-radius: 6px;
  position: absolute;
  z-index: 1;
  margin-left: 5px;
  width: max-content;
  max-width: 60ex;
  white-space: pre-line;
  opacity: 0;
  transition: visibility 0.15s, opacity 0.15s linear;
  cursor: text;
}
.codeCommentIcon:hover .codeCommentText {
  visibility: visible;
  opacity: 1;
}

.codeCommentIcon .codeCommentText::after {
  content: " ";
  position: absolute;
  top: 12px;
  right: 100%; /* To the left of the tooltip */
  margin-top: -5px;
  border-width: 5px;
  border-style: solid;
  border-color: transparent black transparent transparent;
}

.createCodeCommentIcon {
  cursor: pointer;
  opacity: 0;
}

.createCodeCommentIcon:hover {
  opacity: 1;
}

.createCodeCommentIcon > .createCodeCommentInput {
  background-color: #000000CC;
  color: #fff;
  text-align: left;
  border-radius: 6px;
  position: absolute;
  z-index: 1;
  margin-left: 5px;
  width: 60ex;
  white-space: pre-line;
  transition: visibility 0.15s, opacity 0.15s linear;
  cursor: text;
}

.createCodeCommentIcon.active {
  visibility: visible;
  opacity: 1;
}

</style>