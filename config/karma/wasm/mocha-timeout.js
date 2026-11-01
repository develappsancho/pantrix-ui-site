// Mocha's two seconds, raised for Compose UI tests.
//
// A test here draws through Skiko in a browser, and the heavier ones take longer than two seconds with nothing
// wrong. Twenty seconds still catches a hang.
//
// Kotlin appends every file in this directory to the karma.conf.js it generates, where `config` is in scope.
(function (config) {
    const client = config.client || {};
    config.set({ client: Object.assign({}, client, { mocha: Object.assign({}, client.mocha, { timeout: 20000 }) }) });
})(config);
