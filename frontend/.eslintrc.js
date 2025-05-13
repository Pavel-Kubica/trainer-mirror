module.exports = {
    env: {
        browser: true,
        es2021: true,
        node: true,
        jest: true
    },
    extends: [
        'eslint:recommended',
        'plugin:vue/vue3-recommended',
        'plugin:jest/recommended'
    ],
    parserOptions: {
        parser: "@babel/eslint-parser",
        ecmaVersion: 12,
        sourceType: 'module'
    },
    plugins: [
        'vue',
        'jest'
    ],
    rules: {
        // Remove these rules when you want to make the code cleaner ;)
        'vue/max-attributes-per-line': 'off',
        'vue/first-attribute-linebreak': 'off',
        'vue/require-prop-types': 'off',
        'vue/no-template-shadow': 'off',
        'vue/html-closing-bracket-newline': 'off',
        'vue/v-on-event-hyphenation': 'off',
        'vue/require-default-prop': 'off',
        'vue/require-explicit-emits': 'off',
        'vue/no-v-html': 'off',
    },
    settings: {
        'vue': {
            'version': '3' // Enable Vue 3 support in eslint-plugin-vue
        }
    },
    globals: {
        defineProps: "readonly",
        defineEmits: "readonly",
        defineExpose: "readonly",
        withDefaults: "readonly"
    },
    ignorePatterns: ['node_modules/', 'dist/', 'worker.js']
};
