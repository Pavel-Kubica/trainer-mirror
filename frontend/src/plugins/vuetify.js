// Styles
import '@mdi/font/css/materialdesignicons.css'
import 'vuetify/styles'

// Vuetify
import { createVuetify } from 'vuetify'
import { cs, en } from 'vuetify/locale'
import { customCs, customEn } from '@/plugins/translations'
import { VDataTable } from 'vuetify/components'

export default createVuetify({
    components: [ VDataTable ],
    locale: {
        locale: navigator.language === 'cs-CZ' ? 'customCs' : 'customEn',
        fallback: 'cs',
        messages: { cs, en, customCs, customEn },
    },
    theme: {
        defaultTheme: 'customLight',
        themes: {
            customLight: {
                dark: false,
                colors: {
                    anchor: '#0000ff',
                    progress: '#b388ff',
                    mermaidBg: '#ffffff',
                }
            },
            customDark: {
                dark: true,
                colors: {
                    anchor: '#a9edfe',
                    progress: '#6200ea',
                    mermaidBg: '#202020',
                }
            },
            halloween: {
                dark: true,
                colors: {
                    anchor: '#ffa500',
                    progress: '#FF8C00',
                    mermaidBg: '#202020',
                }
            },
            christmasLight: {
                dark: false,
                colors: {
                    anchor: '#0000ff',
                    progress: '#b388ff',
                    mermaidBg: '#ffffff',
                }
            },
            christmasDark: {
                dark: true,
                colors: {
                    anchor: '#a9edfe',
                    progress: '#6200ea',
                    mermaidBg: '#202020',
                }
            },
        }
    }
})
