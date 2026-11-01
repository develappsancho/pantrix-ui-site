// The site's addresses are paths, /docs/installation, and a page reloaded or opened by its address is asked of the
// server at that path. The site's own server answers each with its page's folder; webpack's, which serves the app
// while it is developed (wasmJsBrowserDevelopmentRun), has no such folders and answers with the app's index.html,
// whose app opens the page the address names.
config.devServer = Object.assign({}, config.devServer, { historyApiFallback: true });
