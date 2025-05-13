const {defineConfig} = require('@vue/cli-service')

module.exports = defineConfig({
    transpileDependencies: true,

    pluginOptions: {
        vuetify: {},
    },

    configureWebpack: {
        devServer: {
            headers: {
                "Cross-Origin-Opener-Policy": "same-origin",
                "Cross-Origin-Embedder-Policy": "require-corp"
            }
        }
    },

    chainWebpack: config => {
        config.module
            .rule('svg')
            .uses.clear()
            .end()
            .use('vue-svg-loader')
            .loader('vue-svg-loader')
            .end();

        config.module
            .rule('markdown')
            .test(/\.md$/)
            .use('raw-loader')
            .loader('raw-loader')
            .end();
    },

    devServer: {
        // --- This hides the "ResizeObserver loop-error" ------
        client: {
            overlay: {
                runtimeErrors: (error) => {
                    const ignoreErrors = [
                        "ResizeObserver loop limit exceeded",
                        "ResizeObserver loop completed with undelivered notifications.",
                    ];
                    return !ignoreErrors.includes(error.message);
                },
            },
        },
    },
})
