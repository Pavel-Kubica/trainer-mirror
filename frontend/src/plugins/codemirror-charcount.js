import { showPanel } from '@codemirror/view'

const countCharacters = (doc, t, limit) => {
    let count = 0, iter = doc.iter()
    while (!iter.next().done) {
        for (const line of iter.value)
            count += line.length
    }
    return t('$vuetify.code_module.character_limit', count, limit)
}

const characterCountPanel = (t, limit, view) => {
    let dom = document.createElement("div")
    dom.style.paddingLeft = '16px'
    dom.textContent = countCharacters(view.viewState.state.doc, t, limit)
    return {
        dom,
        update(update) {
            if (update.docChanged)
                dom.textContent = countCharacters(view.viewState.state.doc, t, limit)
        }
    }
}

export function charCounter(t, limit) {
    return showPanel.of((view) => characterCountPanel(t, limit, view))
}
