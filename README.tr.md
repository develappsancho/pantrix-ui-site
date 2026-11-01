# Pantrix UI Sitesi

English: [README.md](./README.md)

[Pantrix UI](https://github.com/develappsancho/pantrix-ui-cmp-package)'ın sitesi, Pantrix UI ile yapıldı: web (Wasm), Android, iOS ve masaüstü için tek bir Compose Multiplatform uygulaması. Her sayfa İngilizce ve Türkçedir, her biri kendi adresinde: `/docs/installation` ve `/tr/docs/installation`.

## Çalıştırmak

Pantrix UI, `gradle/libs.versions.toml`'daki sürümün seçtiği iki depodan birinden gelir: bir sürüm GitHub Packages'tan gelir ve paket okuyabilen bir token ister (`~/.gradle/gradle.properties`'te `GitHubPackagesUsername` ve `GitHubPackagesPassword`); bir `-SNAPSHOT` ise kütüphanenin kendi deposundan yayınlanan yerel Maven deposundan gelir (`./gradlew publishToMavenLocal`).

*   Web: `./gradlew :app:webApp:wasmJsBrowserDevelopmentExecutableDistribution`, ardından `app/webApp/build/dist/wasmJs/developmentExecutable`'ı sunmak: bir dosya olduğu gibi, sayfası olan bir adres klasörünün `index.html`'i, başka her adres `404.html`. Her sayfanın, uygulamanın yanında, arama motorları ve uygulamayı çalıştıramayan bir tarayıcı için statik bir `index.html`'i vardır; kökte `sitemap.xml`, `robots.txt` ve `llms.txt` durur.
*   Geliştirirken web: `./gradlew :app:webApp:wasmJsBrowserDevelopmentRun`. Webpack'in sunucusu her sayfanın adresine uygulamayla cevap verir; uygulama adresin adlandırdığı sayfayı açar, böylece bir sayfa olduğu yerde yeniden yüklenir (`app/webApp/webpack.config.d/historyFallback.js`); statik sayfaları yoktur.
*   Masaüstü: `./gradlew :app:desktopApp:run`
*   Android: `./gradlew :app:androidApp:installDebug`
*   iOS: `app/iosApp`'ı Xcode'da açıp `app.iosApp` şemasını çalıştırmak.
*   Pantrix UI'ın MCP sunucusunu `/mcp`'de sunan sunucu: `./gradlew :server:bootJar`, ardından `PORT=8080 java -jar server/build/libs/server.jar`. Bir asistana adresi verilir: `claude mcp add -t http pantrix-ui http://localhost:8080/mcp`.

## Denetlemek

*   `./gradlew detekt lintKotlin`
*   `./gradlew jvmTest iosSimulatorArm64Test wasmJsBrowserTest`
*   `./gradlew :server:test`
*   `./gradlew :app:shared:verifyRoborazziJvm`: her türden bir sayfa, masaüstü, tablet ve telefon boyutunda, açık ve koyu, `app/shared/screenshots/jvm/`'deki görüntülerle karşılaştırılır (`recordRoborazziJvm` onları yeniden yazar)

## Yayına Almak

Site tek bir Docker Compose yığınında iki konteyner olarak çalışır: nginx'in ardında dosyaları ve yanında, yalnızca nginx'in `/mcp`'de ulaştığı sunucu. İki imaj da bu makinede, Gradle'ın yaptıklarından yapılır; sunucuda hiçbir şey derlenmez.

*   Bu makinede: `deploy/deploy.sh local` siteyi ve sunucuyu derler, iki imajı yapar, yığını `http://localhost:4004`'te çalıştırır ve `deploy/check.sh` ile denetler; `deploy/deploy.sh stop` onu durdurur. Docker'ı ve GitHub Packages için yukarıdaki token'ı ister.
*   Sunucuda, `pantrixui.com`: `deploy/deploy.sh web` aynı imajları GitHub'ın imaj deposuna gönderir ve Coolify'a yayımlatır; `deploy/deploy.sh desktop` macOS uygulamasını imzalı ve Apple'ın onayından geçmiş olarak bu deponun sürümlerinde yayımlar. Adımlar [docs/deploy/DEPLOY_GUIDE.md](docs/deploy/DEPLOY_GUIDE.md)'dedir.

Sitenin adresi tek bir yerde yazılıdır: `gradle.properties`'teki `site.url`; derleme onu statik sayfalara, içeriğe (`{{site}}` olarak), paylaşım görsellerine ve uygulamalara verir. Çalışan bir yığın ise `SITE_URL`'de sunulur: web konteyneri açılırken onu sitenin dosyalarına yazar, web uygulaması da açıldığı adresi okur.

Rehberler, Türkçe ve İngilizce: [ARCHITECTURE_GUIDE.md](./docs/deploy/ARCHITECTURE_GUIDE.md) neyin nerede ve neden çalıştığını çizer (nginx, imajlar, yığın, sıkıştırma ve önbellek, sitenin adresi), [DEPLOY_GUIDE.md](./docs/deploy/DEPLOY_GUIDE.md) adımları verir.

## İçerik

Sayfalar `content/<dil>/` altında Markdown'dır, sitenin kendi parçalarıyla (`:::steps`, `:::facts`, `:::tabs`, …). İçerik aracı (`tools/content`) onları derlemede JSON'a çevirir; uygulama asla Markdown okumaz. Rehber sayfaları kütüphanenin rehberlerinden yazılır: kütüphane yayınlandıktan sonra `./gradlew :core:content:writeGuidePages`.

Bir bileşenin sayfası demo'nun örneklerini ya da, sitenin kendisinin yazdığı yerde, sitenin örneklerini gösterir: `content/examples/<dil>/<anahtar>.md`, demo'nun sayfalarının biçiminde; her örnek bir başlık, ne söylediği ve kodu. Derleme gösterdiği kodu çalıştırır ve sitenin kendi örneklerini çerçeveleri kadar geniş çizer.

## Kodlama Asistanları

Site, Pantrix UI'ın bir uygulamanın asistanına verdikleriyle yapıldı: `.claude/skills/pantrix-ui`'deki becerisi ve `.mcp.json`'daki MCP sunucusu; sunucu kütüphanenin jar'ını `PANTRIX_MCP_JAR`'dan çalıştırır. Her makine onu, örneğin kabuğun profilinde, ayarlar: kütüphanenin sürümüne ekli jar'a ya da yerel bir yayının yerel Maven deposuna koyduğuna.

```bash
curl -fL --create-dirs -o ~/.pantrix/mcp-<version>-all.jar https://github.com/develappsancho/pantrix-ui-cmp-package/releases/download/v<version>/mcp-<version>-all.jar
export PANTRIX_MCP_JAR=$HOME/.pantrix/mcp-<version>-all.jar
# Ya da kütüphanenin deposunda publishToMavenLocal'dan sonra
export PANTRIX_MCP_JAR=$HOME/.m2/repository/com/pantrixui/mcp/<version>/mcp-<version>-all.jar
```

`CLAUDE.md` bir asistan için projenin haritasını tutar; `.claude/skills/commit` bir değişikliğin commit'ini deponun biçiminde önerir.

## Katkı

Bir değişikliğin nasıl geldiği, sitenin kuralları ve pull request'ten önceki denetimler [CONTRIBUTING.tr.md](./CONTRIBUTING.tr.md)'dedir; neyin değiştiği [CHANGELOG.tr.md](./CHANGELOG.tr.md)'dedir. Bir güvenlik açığı asla bir issue'da değil, [SECURITY.tr.md](./SECURITY.tr.md)'nin söylediği gibi bildirilir. Katılan herkes [davranış kurallarına](./CODE_OF_CONDUCT.tr.md) uyar.

## Lisans

Site [Apache License 2.0](./LICENSE) ile lisanslanmıştır. Sitenin kodunun yazıldığı JetBrains Mono ile Arapçasının ve kendi işaretlerinin çizildiği Noto alt kümeleri SIL Open Font License ile gelir; lisansların metinlerini site taşır: `core/designsystem/src/commonMain/composeResources/files/licenses/`.
