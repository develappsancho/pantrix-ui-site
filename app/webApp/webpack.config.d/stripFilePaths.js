// Takes the path of the machine that built a script out of it.
//
// Skiko's skiko.mjs reads `import.meta.url`, and webpack writes it into the script as the module's own path where it
// was built: "file:///Users/<name>/…/kotlin/skiko.mjs", the builder's user name and folders in a public file. The
// script reads it only for the folder it was loaded from, which it asks for nothing here: it finds Skiko's Wasm by the
// address webpack gives it. Each such string becomes an empty one, which the script takes for no path at all. It is
// done after the script is minified and before its source map is written, through a ReplaceSource, so that the map
// still matches the script.
config.plugins.push({
    apply(compiler) {
        const { Compilation, sources } = compiler.webpack;
        compiler.hooks.thisCompilation.tap("StripFilePaths", (compilation) => {
            compilation.hooks.processAssets.tap(
                { name: "StripFilePaths", stage: Compilation.PROCESS_ASSETS_STAGE_OPTIMIZE_SIZE + 1 },
                () => {
                    for (const asset of compilation.getAssets()) {
                        if (!/\.m?js$/.test(asset.name)) continue;
                        const text = asset.source.source().toString();
                        const paths = [...text.matchAll(/"file:\/\/\/[^"]*"/g)];
                        if (paths.length === 0) continue;
                        const stripped = new sources.ReplaceSource(asset.source);
                        for (const path of paths) {
                            stripped.replace(path.index, path.index + path[0].length - 1, '""');
                        }
                        compilation.updateAsset(asset.name, stripped);
                    }
                }
            );
        });
    }
});
