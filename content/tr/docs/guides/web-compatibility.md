---
title: Her tarayıcıda açılan uygulama
description: Compose Multiplatform'un web uygulamasının bazı tarayıcılarda neden hiç açılmadığı, bunu nasıl teşhis ettiğimizi ve nasıl giderdiğimizi. Sorun Pantrix'e özgü değildir: web hedefi yalnızca Wasm (wasmJs) olan her Compose Multiplatform uygulamasında aynı şekilde görülür.
---

## 1. Sorun {#problem}

### Belirti

*   Sayfa açılır ama uygulama gelmez. Ne görüneceği sayfanın kendisine bağlıdır:
    *   Uygulamanın dışında hiçbir şey yazmayan bir sayfa **boş, beyaz** kalır.
    *   Bir yükleme işareti gösteren sayfa, işareti **sonsuza dek** gösterir.
    *   Her sayfanın statik HTML'ini yazan bir sitede (Pantrix'in sitesi gibi) uygulama yerine o HTML'in **biçimsiz metni** görünür: bağlantılar, başlıklar, düz yazı.
*   Masaüstü tarayıcıda ve iOS simülatöründe her şey yolundadır; sorun Android emülatöründe, eski bir telefonda ya da bazı gerçek iPhone'larda çıkar.
*   Konsolda şu satırlardan biri vardır:

```text
CompileError: WebAssembly.Module(): Unknown type code 0x5e, enable with --experimental-wasm-gc @+11
Please make sure that your runtime environment supports the latest version of Wasm GC and Exception-Handling proposals.
```

:::sketch web-compatibility
:::

### Neden

*   Kotlin/Wasm'ın çıktısı iki Wasm önerisine dayanır: **çöp toplama** (GC) ve **istisnalar** (exception handling). Bunları destekleyen ilk sürümler:

:::table
| Tarayıcı | İlk sürüm |
| :--- | :--- |
| Chrome, Edge, Android'deki Chrome ve WebView | 119 |
| Firefox | 120 |
| Safari, iOS'taki bütün tarayıcılar (WebKit) | 18.2 |
:::

*   Daha eski bir tarayıcı modülü derleyemez ve uygulama hiç başlamaz. Android emülatörünün sistem imajlarındaki Chrome çoğu zaman eskidir (Android 14 imajında Chrome 113); iOS 18.2'ye geçemeyen iPhone'larda Safari ve Chrome Wasm GC'siz kalır.
*   Özellikleri destekleyen bir tarayıcıda da Wasm sürümü açılamayabilir. Bir telefonun tarayıcısı, bir modülü derlemek için ayırdığı belleği aşabilir. Compose'un web uygulaması iki büyük modülden oluşur (bizde 14,5 MB uygulama, 8,6 MB Skia). Mac'teki simülatörün belleğiyle derlenen bir modül telefonda reddedilebilir.
*   **WebKit 27.2'de Wasm sürümü boş kalır.** Safari 27.2'nin ve iOS 27.2'nin WebKit'inde Wasm derleyicisi, ısındıktan sonra Compose'un metin ölçümünü yanlış derler: uygulama ilk karesini çizer, sonra bağlantılı bir metnin (`LinkAnnotation`) kutusunu sorarken `SkiaParagraph.getBoundingBox`'ta bir `NullPointerException` ile durur ve sayfa boş kalır. Aynı uygulamanın JS sürümü aynı tarayıcıda çizer; iOS 27.0'ın WebKit'inde Wasm sürümü de çizer. Hata Wasm'ın değil uygulamanın hatası gibi, ilk kareden sonra geldiği için yükleyici JS'e geçmez (5.2).
*   Compose'un kendi Gradle eklentisindeki `composeCompatibilityBrowserDistribution`, JS ve Wasm dağıtımlarını birleştirir ve tarayıcıyı açılışta bir kez sınar. Wasm sürümü sınamadan geçip sonra çökerse JS'e geçmez: ikinci durum için bir şey yapmaz.

### Teşhis

*   **Android emülatöründe Chrome'un sürümü:**

```bash
adb shell dumpsys package com.android.chrome | grep versionName
```

*   **Emülatördeki Chrome'un konsolu,** Mac'ten DevTools protokolüyle: önce yönlendirme, sonra `http://127.0.0.1:9333/json/list` sayfaları listeler ve her sayfanın `webSocketDebuggerUrl`'ine bağlanılır.

```bash
adb forward tcp:9333 localabstract:chrome_devtools_remote
```

*   **Yerel bir sunucuyu emülatörde açmak:** `adb reverse tcp:4004 tcp:4004`; emülatör `http://localhost:4004`'ü Mac'teki sunucuya götürür.
*   **Gerçek bir iPhone'un konsolu:** iPhone'da Ayarlar → Safari → İleri Düzey → Web Denetçisi açılır, telefon Mac'e kabloyla bağlanır, Mac'teki Safari'nin Geliştir menüsünde telefonun sayfası seçilir.
*   **Wasm'ın hangi önerilere dayandığı:** Node'un V8'i bir öneri kapatılarak çalıştırılır; modül derlenemiyorsa ona dayanıyordur. Kotlin 2.4.21'in çıktısı eski istisna önerisini (`legacy-eh`) kullanır, yenisini (`exnref`) kullanmaz.

```bash
node --no-experimental-wasm-legacy-eh -e "new WebAssembly.Module(require('fs').readFileSync('app.wasm'))"
```

## 2. Çözüm: iki sürüm ve bir yükleyici {#fix}

:::table
| Parça | Ne yapar |
| :--- | :--- |
| `js` hedefi | Uygulamanın aynı kodu bir de Kotlin/JS ile derlenir. Skia yine Wasm'dır, ama GC'siz düz Wasm: her tarayıcı çalıştırır |
| Ayrı adlı iki paket | `site-wasm.js` ve `site-js.js`; ikisi aynı klasöre konur |
| Yükleyici (`site.js`) | Tarayıcıyı Compose'un kendi modülüyle sınar ve sürümü seçer; Wasm sürümü ilk karesini çizmeden bir Wasm hatasıyla çökerse JS sürümünü başlatır; o da çökerse statik metni gösterir |
| Sayfadaki işaret | Wasm'ı olan bir tarayıcıda `<html class="app">`: statik metin gizlenir, uygulamanın kutusu görünür |
:::

Uygulamanın ilk karesini çizdiği an, yükleme işaretini kaldırdığı andır: yükleyici bir hatanın uygulamanın açılışına mı, açıldıktan sonraki bir işe mi ait olduğunu bundan anlar.

## 3. Gradle {#gradle}

### 3.1. Hedefler

Uygulamanın web modülü iki hedefle, iki ayrı paket adıyla derlenir. Uygulamanın kullandığı her çoklu platform modülüne de `js` hedefi eklenir.

```kotlin
kotlin {
    js {
        browser {
            commonWebpackConfig { outputFileName = "site-js.js" }
        }
        binaries.executable()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            commonWebpackConfig { outputFileName = "site-wasm.js" }
        }
        binaries.executable()
    }
}
```

Uygulamanın modüllerinde ortak testler zaten Wasm'da tarayıcıda koşuyorsa, JS'teki tarayıcı testleri kapatılabilir (`js { browser { testTask { enabled = false } } }`): JS sürümü aynı koddur. Testlerin JS'te derlendiği yine denetlenir (`compileTestKotlinJs`).

### 3.2. Tek klasör

JS dağıtımının paketi ve Skia'nın hash adlı Wasm'ı, Wasm dağıtımının klasörüne kopyalanır; sayfa, stiller ve kaynaklar Wasm dağıtımında zaten vardır. Skia'nın Wasm'ı iki sürümde aynı dosyadır, aynı adı taşır ve bir kez indirilir.

```kotlin
val copyJs = tasks.register<Copy>("copyJsApp") {
    mustRunAfter("jsBrowserDistribution")
    from(layout.buildDirectory.dir("dist/js/productionExecutable")) {
        include("site-js.js", "*.wasm")
        exclude("skiko.wasm")
    }
    into(layout.buildDirectory.dir("dist/wasmJs/productionExecutable"))
}
tasks.named("wasmJsBrowserDistribution") {
    dependsOn("jsBrowserDistribution")
    finalizedBy(copyJs)
}
```

*   JS dağıtımı kopyanın değil, Wasm dağıtımının **bağımlılığıdır**. Kopya JS dağıtımına bağımlı bir sonlandırıcı (finalizer) olursa Gradle "Unable to make progress running work" ile durur: Kotlin'in JS görevlerinin kendi sonlandırıcıları vardır ve Gradle, başka sonlandırıcıları olan görevleri bekleyen bir sonlandırıcıyı yürütemez.
*   `skiko.wasm` (hash'siz ad) paket tarafından yalnızca sayfa kendi `locateFile`'ını verirse istenir; paket hash adlı kopyayı ister.

### 3.3. Yolları silen webpack eklentisi

Web modülünün `webpack.config.d/stripFilePaths.js`'i; Kotlin'in webpack kurulumu onu kendiliğinden alır (7. bölüm):

```javascript
// Takes the path of the machine that built a script out of it.
//
// Skiko's skiko.mjs reads `import.meta.url`, and webpack writes it into the script as the module's own path where it
// was built: "file:///Users/<name>/…/kotlin/skiko.mjs", the builder's user name and folders in a public file. The
// script reads it only for the folder it was loaded from, which it asks for nothing here: it finds Skiko's Wasm by the
// address webpack gives it. Each such string becomes an empty one, which the script takes for no path at all. It is
// done after the script is minified and before its source map is written, through a ReplaceSource, so that the map
// still matches the script. A development build writes each module into a string that it evaluates, where the path's
// quotes are escaped, `\"file:///…\"`: the empty string keeps their escape, or it would end the module's string.
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
                        const paths = [...text.matchAll(/(\\?)"file:\/\/\/[^"\\]*\1"/g)];
                        if (paths.length === 0) continue;
                        const stripped = new sources.ReplaceSource(asset.source);
                        for (const path of paths) {
                            const quote = `${path[1]}"`;
                            stripped.replace(path.index, path.index + path[0].length - 1, quote + quote);
                        }
                        compilation.updateAsset(asset.name, stripped);
                    }
                }
            );
        });
    }
});
```

## 4. Kod {#the-code}

*   **`wasmJsMain` → `webMain`.** Kotlin 2.4'te tarayıcıyla konuşan kodun çoğu (`JsAny`, `JsString`, `toJsString`, `js("...")`, `kotlinx.browser.window`) iki web hedefinde ortaktır ve `webMain`'e olduğu gibi taşınır. Kaynaklar da (`index.html`, stiller, simgeler) `webMain/resources`'a.
*   **Yalnızca Wasm'da olan** bir şey `expect`/`actual` olur. Örneğin Compose'un kaynak okuyucusuna Wasm belleğini veren kod (`kotlin.wasm.unsafe.wasmMemory`): `wasmJsMain`'de asıl hâli, `jsMain`'de boş bir `actual`.
*   **`fetch`:** JavaScript'in `window.fetch` bildiriminde ikinci parametrenin varsayılanı yoktur: `window.fetch(url, RequestInit())`.
*   **`JsException`, JavaScript'te `Throwable`'ın takma adıdır.** `catch (e: JsException)` JS'te coroutine'in iptalini de yakalar; iptal önce yukarı gönderilir:

```kotlin
} catch (cancelled: CancellationException) {
    throw cancelled
} catch (failure: JsException) {
    null
}
```

*   **`JsString.toString()`** JS'te gereksiz bir dönüşümdür (orada `JsString` Kotlin'in `String`'idir) ve uyarı verir; Wasm'da gereklidir. Uyarı yerinde, gerekçesiyle bastırılır: `@Suppress("REDUNDANT_CALL_OF_CONVERSION_METHOD")`.

## 5. Yükleyici ve sayfa {#the-loader-and-the-page}

### 5.1. Sayfa

```html
<head>
    <!-- Wasm'ı olan bir tarayıcı uygulamayı çalıştırır: statik metni gizler. -->
    <script>
        if (typeof WebAssembly === "object") document.documentElement.className = "app";
    </script>
</head>
<body>
    <div id="loading">…</div>
    <div id="app"></div>
    <script type="application/javascript" src="site.js"></script>
</body>
```

```css
html.app #static { display: none; }
html:not(.app) #loading,
html:not(.app) #app { display: none; }
```

Uygulama ilk karesini çizince `#loading`'i kaldırır (Compose'un `ComposeViewport`'u `#app`'e kurulur; işaret onun üstünde durur).

### 5.2. `site.js`

```javascript
(function () {
    "use strict";
    // Compose'un yükleyicisinin modülü: bir struct türü ve bir `try`. Bunu kabul eden tarayıcıda GC de istisnalar da var.
    var FEATURES = new Uint8Array([
        0, 97, 115, 109, 1, 0, 0, 0, 1, 8, 2, 95, 1, 120, 0, 96, 0, 0, 3, 3, 2, 1, 1, 10, 14, 2, 6, 0, 6, 64, 25, 11,
        11, 5, 0, 208, 112, 26, 11, 0, 45, 4, 110, 97, 109, 101, 1, 15, 2, 0, 5, 102, 117, 110, 99, 48, 1, 5, 102, 117,
        110, 99, 49, 4, 8, 1, 0, 5, 116, 121, 112, 101, 48, 10, 11, 1, 0, 1, 0, 6, 102, 105, 101, 108, 100, 48
    ]);
    var WASM = "site-wasm.js", JS = "site-js.js";
    var starting = null;

    function drawn() { return !document.getElementById("loading"); }
    function load(src, onFailure) {
        var script = document.createElement("script");
        script.src = src;
        script.onerror = onFailure;
        document.body.appendChild(script);
    }
    function showStaticWords() {
        starting = null;
        document.documentElement.classList.remove("app");
    }
    function startJavaScript() {
        starting = "js";
        var box = document.getElementById("app");
        while (box && box.firstChild) box.removeChild(box.firstChild);
        load(JS, showStaticWords);
    }
    function wasmFailed(error) {
        var wasm = typeof WebAssembly === "object";
        return error instanceof RangeError || wasm && (error instanceof WebAssembly.CompileError ||
            error instanceof WebAssembly.LinkError || error instanceof WebAssembly.RuntimeError);
    }
    function thrownBy(error, script) { return String(error && error.stack || "").indexOf(script) >= 0; }
    // WebKit: Safari ve iOS'taki her tarayıcı. Chrome'un üstüne kurulanlar kimlik dizgisinde Chrome'u da anar.
    function webKit() {
        var agent = typeof navigator === "object" && navigator.userAgent || "";
        return /AppleWebKit\//.test(agent) && !/Chrom(e|ium)\//.test(agent);
    }
    function failed(error) {
        if (starting === null || drawn() || !wasmFailed(error)) return;
        if (starting === "wasm") startJavaScript();
        else if (!thrownBy(error, WASM)) showStaticWords();
    }

    window.addEventListener("error", function (event) { failed(event.error); });
    window.addEventListener("unhandledrejection", function (event) { failed(event.reason); });

    var collected = false;
    try { collected = typeof WebAssembly === "object" && WebAssembly.validate(FEATURES); } catch (e) {}
    if (collected && !webKit()) { starting = "wasm"; load(WASM, startJavaScript); }
    else if (typeof WebAssembly === "object") startJavaScript();
    else showStaticWords();
})();
```

*   **WebKit'e JS sürümü verilir,** Wasm GC'si olsa da: WebKit 27.2'nin Wasm derleyicisi Wasm sürümünü boş bırakır (1. bölüm). iOS'taki tarayıcılar kimlik dizgisinde WebKit'in sürümünü söylemez (iOS 27'nin dizgisi işletim sistemini bile `18_7` diye yazar), bu yüzden kural sürüme değil motora bakar.
*   Yalnızca **Wasm'ın kendi hataları** (`CompileError`, `LinkError`, `RuntimeError`) ve bellek yetmezliği (`RangeError`) JS'e geçirir. Uygulamanın kendi bir isteği düştüğünde (örneğin bir API çağrısı) sürüm değişmez.
*   JS sürümü başladıktan sonra Wasm sürümünün geride kalan parçaları da hata atabilir: Skia'nın ayrı yüklenen Wasm'ı, uygulamanın modülü düştükten sonra kendi hatasını atar. Yığını `site-wasm.js`'ten geçen bir hata JS sürümünün hatası sayılmaz; sayılsaydı çalışan bir JS sürümü statik metnin arkasında kalırdı.

## 6. Sunucu {#the-server}

*   **Önbellek:** hash adlı Wasm dosyaları bir yıl, `immutable`; betikler (`site.js`, `site-wasm.js`, `site-js.js`) ve sayfalar `no-cache`. Böylece yeni bir yayın anında görünür ve eski bir betik artık olmayan bir Wasm'ı aramaz.

```nginx
map $uri $site_cache {
    "~^/[0-9a-f]{16,}\.wasm$" "public, max-age=31536000, immutable";
    default                   "no-cache";
}
```

*   **İçerik güvenliği politikası:** `script-src 'self' 'wasm-unsafe-eval'` ve sayfadaki satır içi betiğin hash'i. Satır içi betik değişince hash de değişir. JS sürümü bu politikada hiçbir ihlal raporlamadı (5. bölümdeki betikle, rapor modunda ölçüldü).
*   **Boyutlar (gzip ile):** Wasm yolunda yaklaşık 7,2 MB (uygulama 3,8, Skia 3,3, betik 0,06), JS yolunda yaklaşık 6,7 MB (paket 3,4, Skia 3,3). Bir ziyaretçi yalnızca birini indirir.

## 7. Tuzaklar {#pitfalls}

:::table
| Tuzak | Ne olur | Ne yapılır |
| :--- | :--- | :--- |
| Arka plandaki sekme | DevTools ile sürülen sekme öndeki değilse tarayıcı kare çizmeyi durdurur: uygulama ilk karesini çizmez, yükleme işareti kalkmaz, hata da yazılmaz | Sınanan sekme öne alınır (`/json/activate/<id>`) |
| Emülatörün Chrome'u | Sistem imajının Chrome'u eskidir ve güncellenmeden kalır | Eski tarayıcı yolunu sınamak için tam da bu yüzden işe yarar |
| Gradle'da sonlandırıcı | "Unable to make progress running work" | JS dağıtımı Wasm dağıtımının bağımlılığı olur, kopya yalnızca sonlandırıcı (3.2) |
| `JsException` | JS'te iptali de yakalar | İptal önce yukarı gönderilir (4. bölüm) |
| Webpack ve `import.meta.url` | Skia'nın JavaScript'indeki `import.meta.url`, pakete derleyen makinedeki dosya yolu olarak yazılır (`file:///Users/…`): herkese açık bir dosyada kullanıcı adı ve klasörler | Paket yalnızca yüklendiği klasörü bulmak için okur ve burada kullanmaz: bir webpack eklentisi, paket küçültüldükten sonra ve harita yazılmadan önce, her `"file:///…"` dizgisini boş dizgi yapar (`webpack.config.d/stripFilePaths.js`, 3.3. bölüm) |
| Klib'lerin kaynak yolları | Kotlin her modülün klib'ine kaynak dosyalarının derleyen makinedeki tam yolunu yazar; Wasm'a girerler (`/Users/…/src/commonMain/…`): herkese açık bir dosyada kullanıcı adı ve klasörler. Kütüphanenin klib'leri 0.1.1'den beri yolları deponun köküne göre yazar | Uygulamanın web için derlenen her modülünde `tasks.withType<Kotlin2JsCompile>().configureEach { compilerOptions.freeCompilerArgs.add("-Xklib-relative-path-base=${rootDir.absolutePath}") }`; Wasm'da `strings` ile `/Users/` aranır (8. bölüm) |
| WebKit 27.2'nin Wasm derleyicisi | Uygulama ilk karesini çizer, sonra `SkiaParagraph.getBoundingBox`'ta `NullPointerException` ile durur; sayfa boş kalır, yükleyici JS'e geçmez | WebKit'e JS sürümü verilir (5.2). Bağlantılı her metin (`LinkAnnotation`, `PantrixLink`) tetikleyebilir; JS sürümünde yoktur |
| Sayfanın dili | Uygulama içinde dil değiştirilince sayfanın `<html lang>`'ı açıldığı dilde kalır: ekran okuyucu Türkçe metni İngilizce telaffuzla okur, tarayıcı yanlış dilden çeviri önerir | Kütüphaneyle: en dıştaki `PantrixTheme` `locale`'inin dilini yazar ve izler. Kütüphanesiz: dil değiştikçe `document.documentElement.setAttribute("lang", etiket)` |
| Ekran okuyucunun kopyası | Compose 1.12.2 anlam ağacını gizli bir DOM kopyasına yazar (`#cmp_a11y_root`, kökü `aria-live="polite"`), ama tek bir katmandan: bir menü, sheet ya da diyalog açılınca kopya onun öğeleriyle değişir, katman kapanınca o hâlde kalır. Sonra gösterilen bir snackbar'ın iletisi de sayfanın değişiklikleri de ekran okuyucuya gitmez | Compose 1.13.0 her katmanı tutar; kütüphane o kararlı olunca yükselir. O zamana kadar bir tarayıcı testi kopyaya bir katman açılmadan önce bakar; katman açılmamışsa snackbar'ın iletisi kopyada metin olarak durur ve söylenir |
| Alanda basılan kısayol | Bir metin alanı odaktayken uygulamanın karşıladığı Cmd+F, Ctrl+H ya da F3 tarayıcının bul çubuğunu, geçmişini ya da sonraki eşleşmesini de açar: Compose alandaki tuşu tarayıcıdan sonra işler ve yalnızca Tab'ı tutar | Kütüphaneyle: `onShortcut` kendi kısayolunu tutar, kendi karşılanan tuşlar `Modifier.keepFromBrowser(shortcuts)`'a verilir ([klavye kısayolları rehberi](/docs/guides/keyboard-shortcuts), 3.4). Kütüphanesiz: odak alandayken pencerenin `keydown`'unda, yakalama aşamasında, o tuşlar için `preventDefault()` |
| `adb screencap` | Birden çok ekranı olan bir emülatörde (katlanabilir) uyarı verir ve ilk ekranı alır | `-d <ekran>` ile ekran seçilir |
:::

## 8. Doğrulama {#verification}

Görülenler (2026-10-31, Pantrix UI'ın sitesi, yerel yığında: emülatör ona `adb reverse` ile, simülatör `localhost` ile bağlandı):

:::table
| Ortam | Tarayıcı | Sonuç |
| :--- | :--- | :--- |
| Android emülatörü, Android 14 | Chrome 113 (Wasm GC yok) | Yükleyici `site-js.js`'i seçti; ana sayfa ve Kurulum JS sürümüyle çizildi |
| Masaüstü, başsız Chrome | Wasm GC var | Yalnızca `site-wasm.js` yüklendi ve çizildi |
| Masaüstü, başsız Chrome, uygulamanın Wasm'ı bozuk baytlarla yanıtlanınca | Wasm GC var | `CompileError` → JS sürümü başladı ve çizildi |
| Masaüstü, başsız Chrome, iki Wasm da bozuk | Wasm GC var | İki sürüm de açılamadı → statik metin göründü |
| Bu deponun demosu: emülatör, Chrome 113 ve başsız Chrome'da aynı üç yol | Hepsi | Button sayfası JS sürümüyle çizildi; bozuk Wasm'da JS'e geçti; iki Wasm da bozukken "bu tarayıcı demoyu çalıştıramıyor" yazısı kaldı |
| iOS simülatörü, iOS 27.0 | Safari | Wasm sürümü çizildi |
| iOS simülatörü, iOS 27.2 (2026-11-02) | Safari 27.2 | Wasm sürümü ilk kareden sonra `SkiaParagraph.getBoundingBox`'ta `NullPointerException` ile durdu, sayfa boş kaldı; zorla açılan JS sürümü çizdi. Bu deponun demosu da boş kaldı. Yükleyici WebKit'e JS sürümünü verince ikisi de çizdi |
| İki yol da, rapor modunda | Chrome | İçerik güvenliği politikasına hiçbir ihlal yok |
| Kütüphanenin klib'leri, göreli yollarla (2026-11-02) | Hepsi | `foundation`'ın Wasm, JS ve iOS klib'lerinde mutlak yol kalmadı, kaynaklar `ui/foundation/src/…` diye yazılı; 0.1.0'ın Wasm klib'inde 45 mutlak yol vardı |
:::

Görülmeyenler:

*   Gerçek bir iPhone'daki beyaz sayfanın kesin nedeni: telefonun konsolu okunmadı. Bellek, en güçlü açıklamadır; yükleyicinin JS'e geçişi bu durumu da karşılar, ama telefonda denenmedi.
*   Gerçek bir Android telefon, Firefox, Safari 18.0–18.1.
*   JS sürümünün yavaş bir telefonda ilk karesine kadar geçen süre.

## 9. Nerede {#where}

Pantrix UI'ın sitesi (`pantrix-ui-site` deposu):

:::table
| Dosya | Ne |
| :--- | :--- |
| `build-logic/.../WebApplicationConventionPlugin.kt` | İki paket, JS dağıtımının Wasm klasörüne kopyası |
| `build-logic/.../KmpLibraryConventionPlugin.kt` | Her modülde `js` hedefi, tarayıcı testleri kapalı |
| `app/webApp/src/webMain/resources/site.js` | Yükleyici |
| `app/webApp/src/webMain/resources/index.html` | `app` işareti ve yükleyiciyi çağıran betik |
| `app/webApp/src/{webMain,wasmJsMain,jsMain}/kotlin/.../WasmMemory*.kt` | Yalnızca Wasm'da olan kod, `expect`/`actual` |
| `deploy/check.sh` | Yükleyicinin, iki sürümün ve Wasm dosyalarının canlıdaki denetimi |
| `app/webApp/webpack.config.d/stripFilePaths.js` | Paketlerden derleyen makinenin yolunu çıkaran webpack eklentisi |
:::

Bu deponun web demosu (`app/webApp`) aynı çözümle: `webApp-wasm.js` ve `webApp-js.js`, yükleyici sayfanın zaten yüklediği `webApp.js`'tir. Statik metni olmadığından, iki sürüm de açılamazsa yükleme işaretinin yerine bunu söyleyen bir yazı kalır. Webpack eklentisi de orada aynıdır.

:::table
| Dosya | Ne |
| :--- | :--- |
| `app/webApp/build.gradle.kts` | İki paket, JS dağıtımının Wasm klasörüne kopyası (`copyJsApp`) |
| `app/webApp/src/webMain/resources/webApp.js` | Yükleyici |
| `app/webApp/webpack.config.d/stripFilePaths.js` | Derleyen makinenin yolunu paketlerden çıkarır |
:::
