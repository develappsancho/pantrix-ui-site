# Pantrix UI'ın Sitesine Katkı

English: [CONTRIBUTING.md](./CONTRIBUTING.md)

Bu dosya, bir değişikliğin siteye nasıl geldiğini, neler taşıdığını ve nasıl denetlendiğini anlatır. Buradaki kurallar projenin kendi kurallarıdır; kim yazarsa yazsın her değişiklik onlara uyar.

## Başlamadan Önce

*   Bir hata ya da bir fikir bir issue ile başlar: ne görüldüğü ya da ne istendiği, hangi sayfada ve platformda olduğu ve nasıl görüleceği.
*   Daha büyük bir değişiklik, koddan önce, kendi issue'sunda bir öneriyle başlar. Yeni bir sayfa ya da bölüm, yeni bir modül ve sitenin nasıl derlendiğine ya da yayına alındığına dair bir değişiklik büyük sayılır. Öneri şunları söyler:
    *   değişikliğin ne işe yaradığı; neyi kapsadığı ve neyi dışarıda bıraktığı;
    *   tartılan seçenekler ve birinin neden seçildiği;
    *   hâlâ açık olan sorular.

    Kod, öneri üzerinde anlaşıldıktan sonra gelir.
*   Bir güvenlik açığı asla bir issue'da bildirilmez: nasıl bildirileceğini [SECURITY.tr.md](./SECURITY.tr.md) anlatır.
*   Katılan herkes [davranış kurallarına](./CODE_OF_CONDUCT.tr.md) uyar.

## Kurulum

*   JDK 21 ya da daha yenisi. Gradle sarmalayıcısı (`./gradlew`) kendi Gradle'ını getirir.
*   Pantrix UI'ı GitHub Packages'tan almak için paket okuyabilen bir token, `~/.gradle/gradle.properties`'te: `GitHubPackagesUsername` ve `GitHubPackagesPassword`. GitHub açık bir paket için bile bir token ister. Kütüphanenin bir `-SNAPSHOT`'ı bunun yerine yerel Maven deposundan gelir; kütüphanenin kendi deposundan yayınlanır.
*   Android uygulaması için platform 37'li Android SDK. Uygulama API 30 ve üstünde çalışır.
*   iOS uygulaması için Xcode'lu bir Mac.
*   Siteyi kendi makinenizde yayına almak için Docker.

Sitenin her platformda nasıl çalıştırılacağını ve nasıl yayına alınacağını [README.tr.md](./README.tr.md) anlatır.

## Sitenin Kuralları

*   **Pantrix UI'ın bir uygulaması.** Site kütüphanenin genel API'si üzerine kurulur, Material olmadan. Bir Pantrix bileşeni bir sayfanın gerektirdiğini çizemiyorsa kütüphane düzeltilir, çevresinden dolanılmaz: eksik kütüphaneye bildirilir ve orada, kendi kurallarıyla düzeltilir; site o arada kendisi çizdiği bir parçada bunu KDoc'unda söyler.
*   **Tasarım birebir uygulanır:** masaüstü (1440), tablet (834) ve telefon (390) genişliğinde.
*   **MVI,** bütün tabanları tek bir modülde, `:core:base`: bir ekranın durumu, eylemleri ve etkileri vardır.
*   **Gezinme sitenin kendisinindir** (`:core:navigation`); başka bir projenin alabileceği biçimde yazılır.
*   **Derleme mantığı Kotlin sınıflarıdır:** bir kural eklentisi `build-logic/src/main/kotlin`'de bir `Plugin<Project>`'tir; asla önceden derlenmiş bir `.gradle.kts` betiği değildir.
*   **Sitenin adresi asla elle yazılmaz.** Tek yeri `gradle.properties`'teki `site.url`'dir: bir sayfa onu `{{site}}` diye anar, uygulama `SiteAddress`'ten okur, çalışan bir yığın `SITE_URL`'de sunulur.
*   **Bir sayfa kod değil, içeriktir:** `content/<dil>/` altında Markdown; aynı sayfa İngilizce ve Türkçe, sitenin kendi parçalarıyla (`:::steps`, `:::tabs`, …). Bir rehber sayfasını kütüphanenin rehberlerinden `./gradlew :core:content:writeGuidePages` yazar.
*   **Her ekranın önizlemeleri vardır:** yanındaki bir `…ScreenPreviews.kt`'de dört tane, masaüstünün ve telefonun sayfası, her biri açık ve koyu temada. Tasarım sisteminin bir parçasının iki önizlemesi vardır, açık ve koyu.
*   **Bir arka uç Spring Boot'tur,** MCP SDK'sının getirdiği Ktor olmadan.

## Pull Request Açmadan Önce

Değişikliğin dokunabileceği her şey koşturulur.

*   **Biçim ve statik analiz:** `./gradlew detekt lintKotlin`. Düzeni `formatKotlin` düzeltir.
*   **Testler, her hedefte:** `./gradlew jvmTest iosSimulatorArm64Test wasmJsBrowserTest`. Ortak testler Android'in ana makine ağacında koşmaz.
*   **Önizlemeler:** `app/shared`'in düz bir `jvmTest`'i olan `SitePreviewsTest`, bir ekranın önizlemesi eksikken başarısız olur ve hepsini çizer.
*   **Sunucu:** `./gradlew :server:test` her tür MCP mesajını gerçek HTTP üzerinden sorar.
*   **Sayfanın yükleyicisi,** `site.js` değişince: `check`'in parçası olan `./gradlew :app:webApp:loaderTest` onu yerine konan parçalardan kurulmuş bir tarayıcıda koşturur; `./gradlew :app:webApp:loaderBrowserTest` siteyi derler ve onu Chrome'da koşturur (Mac'teki olağan yerinde olmayan bir Chrome'u `CHROME` adlandırır): Wasm derlemesi, Wasm'ın çöp toplaması yoksa ya da Wasm derlemesi düşerse JavaScript derlemesi, Wasm hiç yoksa sayfanın durağan sözleri.
*   **Cihazda ana yollar,** kabuk, başlık, menü, arama ya da onların götürdüğü bir sayfa değişince: `./gradlew :app:androidApp:maestroTest` ve `./gradlew :app:shared:maestroIosTest`, `-Pmaestro.device=` ve `-Pmaestro.flows=` ile. `app/maestro`'daki akışları bir telefonda koştururlar.
*   **Ekran görüntüleri,** bir sayfanın görünüşü değişince: `./gradlew :app:shared:verifyRoborazziJvm`. İstenen bir değişiklik `recordRoborazziJvm` ile yeniden kaydedilir; koyu görüntülere de bakılır.
*   **Yayına alma,** `deploy/`, sunucu ya da web derlemesi değişince: `deploy/deploy.sh local` iki imajı da yapar, yığını `http://localhost:4004`'te çalıştırır ve `deploy/check.sh` ile denetler.
*   **Kütüphanenin sürümü:** `gradle/libs.versions.toml`'daki `pantrix`, yayınlanınca `LibraryFacts.RELEASE`, yeni jar'la yeniden yazılan beceri ve kodlama asistanları sayfasındaki dosyaların yaklaşık boyutları (`FilesSay`).
*   **Kütüphanenin rehberleri:** `./gradlew :core:content:writeGuidePages`; değişiklik teslim edilmeden önce `content/*/docs/guides/`'ın farkı okunur.

Pull request'te neyin doğrulandığı, hangi platformlarda ve neyin doğrulanmadığı söylenir.

## Değişiklik Günlüğü

[CHANGELOG.md](./CHANGELOG.md) ve Türkçe kopyası [CHANGELOG.tr.md](./CHANGELOG.tr.md), siteyi okuyan ya da uygulamalarını kullanan biri için neyin değiştiğini söyler.

*   **Hangi değişiklik satır alır:** bir ziyaretçinin görebileceği değişiklik, aynı commit'te `Yayınlanmamış` altında bir satır alır: yeni ya da değişen bir sayfa, davranışta ya da görünüşte bir değişiklik, fark edeceği bir düzeltme. Yalnızca derleme mantığı, testler ve belgeler satır almaz.
*   **Nereye:** satır, türünün bölümüne gider: `Eklenenler`, `Değişenler`, `Kullanımdan kalkacaklar`, `Kaldırılanlar`, `Düzeltilenler` ya da `Güvenlik`.
*   **Nasıl:** sayfayı ya da parçayı adıyla anar ve ziyaretçi için neyin değiştiğini tek cümlede söyler.
*   **İki dosya:** aynı satır, kendi bölümünde, Türkçe kopyaya da girer.
*   **Site yayına girmeden önce** `Yayınlanmamış` girdisi ilk sürümü anlatır: listelediğinde bir değişiklik, `Değişenler` ya da `Kaldırılanlar` altına satır eklemek yerine o anlatımı günceller.
*   **Bir yayın,** ziyaretçinin gördüğünü değiştiriyorsa, iki dosyada da `Yayınlanmamış`'ı kendi sürümüyle, `YY.A.G` biçiminde tarihiyle, ve tarihle yeniden adlandırır: bunu `deploy/deploy.sh prepare` yapar.

## Commit'ler

Bir commit mesajı tek satırdır, `[TYPE][Summary]`, başka bir şey değil: gövde de imza satırı da yok.

| Tür | Ne için |
| :--- | :--- |
| `DEV` | Yeni ya da değişen davranış: sayfalar, özellikler, yeniden düzenlemeler, derleme mantığı, bağımlılıklar, araçlar, yayına alma |
| `FIX` | Yanlış olup şimdi doğru olan davranış: bir hata, bir çökme, yanlış bir renk |
| `TEST` | Yalnızca testler ve koştukları kurulum |
| `DOCS` | Yalnızca belgeler |
| `CI` | Yalnızca CI hizmetinin koşturduğu şeyler |
| `RELEASE` | Bir yayını hazırlamak ya da işaretlemek: değişiklik günlüğü, kütüphanenin sürümü |

*   **Karışık commit'ler:** karışık bir commit ana amacının türünü alır. Regresyon testiyle bir düzeltme `FIX`'tir; testleri ve belgeleriyle bir özellik `DEV`'dir.
*   **Özet:**
    *   İngilizcedir, emir kipindedir ve büyük harfle başlayan bir fiille başlar: `[DEV][Add the theme builder]`, `[FIX][Draw the sidebar's scroll bar in the dark theme]`.
    *   İşin nasıl gittiğini değil, neyin değiştiğini söyler.
    *   Birkaç değişiklik, her biri büyük harfli kendi fiiliyle başlayarak, virgüllerle ve sonuncudan önce `and` ile sıralanır: `[DEV][Add …, Draw … and Read …]`.
    *   Nokta ile bitmez. Tek bir değişiklik yaklaşık 72 karakterde kalır; değişikliklerin listesi gerektiği kadar uzar.

## Diller

*   **Kod, KDoc ve commit mesajları** İngilizcedir.
*   **Sayfalar** İngilizce ve Türkçedir: `content/en` ve `content/tr` aynı sayfaları tutar.
*   **`docs/`'taki rehberler** Türkçe yazılır, her birinin İngilizcesi yanındadır (`X_GUIDE.en.md`) ve ikisi birlikte değiştirilir:
    *   Her biri diğerine, başlıktan sonra üçüncü satırında bağlanır: `English: [X_GUIDE.en.md](./X_GUIDE.en.md)` ve `Türkçesi: [X_GUIDE.md](./X_GUIDE.md)`.
    *   İngilizcesi Türkçesinin biçimindedir: numaralarıyla aynı başlıklar, aynı tablolar, kod blokları ve resimler.
*   **Kökteki dosyalar** (bu dosya, README, değişiklik günlüğü, güvenlik politikası ve davranış kuralları) İngilizcedir. Her birinin yanında Türkçe bir kopyası vardır (`*.tr.md`) ve ikisi birlikte değiştirilir.

## Lisans

Site [Apache License 2.0](./LICENSE) ile lisanslanmıştır. Bir katkı aynı lisansla yapılır: imzalanacak ayrı bir sözleşme ve commit'lerde bir imza satırı yoktur.
