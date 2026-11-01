// The Compose resources a test reads, served beside the test.
//
// A module's resources are copied into the test's package (kotlin/composeResources), but Karma serves only the files
// it is told of: the resource reader's fetch of `composeResources/…` found nothing, and a page the site has read as
// one it does not. They are served here, and the reader's address is sent to them.
//
// Kotlin appends every file in this directory to the karma.conf.js it generates, where `config` is in scope.
(function (config) {
    const resources = config.basePath + '/kotlin/composeResources';
    config.files.push({ pattern: resources + '/**/*', included: false, served: true, watched: false, nocache: true });
    config.set({ proxies: Object.assign({}, config.proxies, { '/composeResources/': '/base/kotlin/composeResources/' }) });
})(config);
