# 🚀 Yayın Rehberi

English: [DEPLOY_GUIDE.en.md](./DEPLOY_GUIDE.en.md)

Bu belge, Pantrix UI'ın sitesinin nasıl yayına alındığını adım adım verir: önce bu makinede, Docker Compose ile; sonra Coolify'da. Neyin nerede çalıştığı ve neden böyle kurulduğu [ARCHITECTURE_GUIDE.md](./ARCHITECTURE_GUIDE.md)'dedir.

## 1. Özet

| Komut | Ne yapar |
| :--- | :--- |
| `deploy/deploy.sh local` | Siteyi ve sunucuyu derler, iki imajı yapar, yığını `http://localhost:4004`'te kaldırır ve denetler |
| `deploy/deploy.sh local --no-gradle` | Aynısını Gradle'ın son çıktısıyla yapar: yalnızca `deploy/` değiştiğinde |
| `deploy/deploy.sh stop` | Yığını durdurur ve konteynerlerini kaldırır; imajlar kalır |
| `deploy/check.sh [adres]` | Denetimleri bir adreste çalıştırır: lokalde ya da canlıda |
| `deploy/deploy.sh web [sürüm]` | İki imajı iki mimari için GHCR'ye gönderir, Coolify'a yayını başlatır ve canlı adresi denetler |
| `deploy/deploy.sh desktop [sürüm]` | macOS uygulamasını imzalı ve Apple'ın onayından (notarization) geçmiş bir DMG yapar, sürümün GitHub sayfasına koyar |
| `deploy/deploy.sh release [sürüm]` | Önce `web`, sonra `desktop`; değişiklik günlüğünde sürümün bölümünü ister |
| `deploy/deploy.sh prepare [sürüm]` | Değişiklik günlüğünün `Unreleased` bölümüne ve Türkçesinin `Yayınlanmamış` bölümüne sürümün adını ve tarihini verir |

Bir sürüm, yayının tarihidir: `YY.A.G` (`26.10.31`); verilmezse bugününki. `--dry-run` her şeyi derler ve hiçbir yere, GHCR'ye, Coolify'a, GitHub'a ya da Apple'a, bir şey göndermez; `--no-gradle` Gradle'ın son çıktısını alır; `--no-coolify` imajları gönderir ve Coolify'a dokunmaz.

## 2. Gerekenler

*   **Docker Desktop**, açık. `docker compose` onunla gelir.
*   **JDK 21** ve Gradle'ın kendisi (`./gradlew`).
*   **GitHub Packages'ın anahtarı:** Pantrix UI'ın bir sürümü GitHub Packages'tan gelir ve GitHub, açık paketler için bile bir anahtar ister. `~/.gradle/gradle.properties`'te `GitHubPackagesUsername` ve `read:packages` yetkili bir token olarak `GitHubPackagesPassword`. Bir `-SNAPSHOT` sürümü yerel Maven deposundan gelir ve anahtar istemez.
*   **Canlıya yayın için** (Bölüm 7): GitHub'ın komut satırı aracı `gh`, `write:packages` izniyle; Coolify'ın adresi, API anahtarı ve kaynağın UUID'si `~/.gradle/gradle.properties`'te.
*   **Masaüstü sürümü için:** Xcode'un komut satırı araçları, anahtar zincirinde Developer ID sertifikası ve Apple'ın onayı için `pantrix-notary` adıyla kaydedilmiş bilgiler (Bölüm 7.1).

## 3. Lokalde Yayın

```bash
deploy/deploy.sh local
```

Komut dört şey yapar:

1.  **Gradle:** `:app:webApp:wasmJsBrowserDistribution` sitenin üretim çıktısını ve her sayfanın statik HTML'ini yazar (`app/webApp/build/dist/wasmJs/productionExecutable`), `:server:bootJar` sunucunun jar'ını yapar (`server/build/libs/server.jar`). İlk derleme birkaç dakika sürer; Wasm'ın en iyilenmesi en uzun adımdır.
2.  **Docker:** İki imajı yapar ve derlendikleri commit'in kısa karmasıyla adlandırır; ağaçta commit'lenmemiş değişiklik varsa adın sonuna `-dirty` eklenir: `ghcr.io/develappsancho/pantrix-ui-site-web:fbc4fe2-dirty`.
3.  **Compose:** Yığını kaldırır ve iki konteyner de sağlıklı (`healthy`) olana kadar bekler.
4.  **Denetim:** `check.sh`'i `http://localhost:4004`'te çalıştırır (Bölüm 4).

Sonunda site `http://localhost:4004`'tedir; yalnızca bu makineden açılır. Başka bir port için:

```bash
SITE_PORT=4100 deploy/deploy.sh local
```

Konteynerlerin günlükleri:

```bash
docker compose -f deploy/compose.yaml -f deploy/compose.local.yaml logs -f
```

Durdurmak:

```bash
deploy/deploy.sh stop
```

## 4. Denetimler

`check.sh` siteyi bir tarayıcının ve bir asistanın ulaştığı gibi dener; her denetim bir satır yazar ve biri başarısız olursa betik başarısız döner.

| Bölüm | Denenen |
| :--- | :--- |
| Sayfalar | Ana sayfa; `/docs/installation` yönlendirmesiz ve kendi adresi olarak denenen adresi veriyor; Türkçesi; bir bileşen sayfası; olmayan bir adres 404 sayfasıyla ve 404 durumuyla; `llms.txt` UTF-8 düz metin ve denenen adrese götürüyor; `sitemap.xml` denenen adresle; `robots.txt` |
| Sıkıştırma ve önbellek | Bir sayfa gzip ile ve `no-cache`; `site.js` `no-cache`; her Wasm dosyası `application/wasm`, gzip ile ve bir yıl önbellekte; sunucunun sürümü yazmıyor, `nosniff` var |
| İkinci ad | `www` altındaki ad kalıcı olarak denenen adrese gidiyor (lokalde bir `Host` başlığıyla). Adı olmayan bir deneme adresinde `CHECK_WWW=no` ile atlanır |
| MCP sunucusu | `initialize` pantrix-ui'den yanıt alıyor; `tools/list`; bir araç çağrısı; GET'in reddi (405); sitenin kendi sayfasından gelenin yanıtlanması; başka bir sitenin sayfasından gelenin reddi (403); 64 KB'ı aşan mesajın reddi (413) |

Herhangi bir adreste çalışır. Verilen adres, yığının `SITE_URL`'idir: denetimler, sayfaların bu adrese götürdüğünü de dener.

```bash
deploy/check.sh https://pantrixui.com
```

## 5. Sitenin Adresi

Adres iki yerde verilir, kodda hiçbir yerde yazılmaz ([ARCHITECTURE_GUIDE.md](./ARCHITECTURE_GUIDE.md), bölüm 6.2):

| Ayar | Nerede | Ne zaman değişir |
| :--- | :--- | :--- |
| `site.url` | `gradle.properties` | Sitenin kalıcı adresi değişince; site yeniden derlenir |
| `SITE_URL` | Compose'un ortam değişkeni: lokalde `compose.local.yaml` (`http://localhost:4004`), Coolify'da kaynağın ortam değişkenleri | Yığın başka bir adreste sunulunca; konteyner yeniden başlatılır |

İçerik sayfaları adresi `{{site}}` diye anar: `claude mcp add -t http pantrix-ui {{site}}/mcp`.

Yeni bir alan adı:

1.  `gradle.properties`'te `site.url=https://yeni-alan-adi.com`.
2.  `deploy/deploy.sh local` ile derleyip denemek.
3.  Coolify'da `SITE_URL=https://yeni-alan-adi.com` vermek ve yayımlamak.

## 6. Sorun Giderme

| Belirti | Neden ve çözüm |
| :--- | :--- |
| `Docker is not running` | Docker Desktop'ı açın, komutu yeniden çalıştırın |
| `port is already allocated` | 4004'ü başka bir şey kullanıyor: `SITE_PORT=4100 deploy/deploy.sh local` |
| Gradle `com.pantrixui` paketlerini bulamıyor | GitHub Packages'ın anahtarı eksik ya da `read:packages` yetkisi yok (Bölüm 2) |
| `/mcp` 502 dönüyor | Sunucu ayakta değil: `docker compose … logs server` |
| Yeni bir değişiklik görünmüyor | `--no-gradle` eski çıktıyı kullanır; komutu onsuz çalıştırın |
| Bağlantılar başka bir adrese götürüyor | Yığına `SITE_URL` verilmemiş ya da yanlış: `docker compose … exec web env \| grep SITE_` |
| Eski imajlar yer kaplıyor | `docker image ls 'ghcr.io/develappsancho/pantrix-ui-site-*'` ile bakın, `docker image rm` ile silin |

## 7. Coolify'da Yayın

Site canlıda Coolify'ın yönettiği VPS'te, `https://pantrixui.com`'da çalışır. İmajlar bu makinede derlenir ve GHCR'ye gönderilir; Coolify depodaki `deploy/compose.yaml`'ı okur, imajları çeker ve yığını kaldırır. VPS'te hiçbir şey derlenmez. Masaüstü uygulaması aynı sürümle, sitenin GitHub'daki sürüm sayfasında yayımlanır.

### 7.1 Bir Kerelik Kurulum

1.  **GHCR'ye gönderme izni.** `gh` hangi hesapla açıksa imajlar o hesabın adına gider: `develappsancho`. İzin bir kez eklenir:

    ```bash
    gh auth refresh -h github.com -s write:packages
    ```

2.  **Coolify'ın API anahtarı.** Coolify'da **Keys & Tokens** → **API Tokens**'tan `read` ve `deploy` yetkili bir anahtar yaratılır. Panelin adresi ve anahtar `~/.gradle/gradle.properties`'e yazılır; depoya girmez:

    ```properties
    CoolifyUrl=https://coolify.ornek-alan-adi.com
    CoolifyToken=…
    ```

    Bu anahtar ortam değişkenlerinin değerlerini okuyamaz: API onları boş gösterir. Değerler panelde görülür.
3.  **İlk gönderim.** Coolify'daki kaynak, imajlar GHCR'de olmadan yayımlayamaz. İlk gönderim Coolify'a dokunmadan yapılır:

    ```bash
    deploy/deploy.sh web --no-coolify
    ```

    İki paket, `pantrix-ui-site-web` ve `pantrix-ui-site-server`, gizli olarak açılır. İmajların dizinindeki `org.opencontainers.image.source` bilgisiyle depoya kendiliğinden bağlanırlar. Her biri için GitHub'da paketin sayfası → **Package settings** → **Danger Zone** → **Change visibility** → **Public**. Coolify onları anahtarsız çeker.
4.  **Coolify'da kaynak.** **Projects**'te projenin ortamı → **+ New** → **Public Repository**:

    | Alan | Değer |
    | :--- | :--- |
    | Repository URL | `https://github.com/develappsancho/pantrix-ui-site` |
    | Branch | `main` |
    | Build Pack | Docker Compose |
    | Base Directory | `/` |
    | Docker Compose Location | `/deploy/compose.yaml` |
    | `web`'in alan adları | `https://pantrixui.com,https://www.pantrixui.com` |
    | `server`'ın alan adı | Boş: yalnızca nginx'in arkasındadır |
    | Ortam değişkeni | `SITE_URL=https://pantrixui.com` |

    **Save** ile kaydedilir; ilk yayını betik başlatır.
5.  **Kaynağın UUID'si.** Panelde kaynağın adresinin son parçasıdır: `…/application/<uuid>`. Kaynağın adındaki kimlik UUID değildir: ad `pantrix-ui-site:main-cntrex…` iken UUID `sfyuo3…`'tür. UUID de `~/.gradle/gradle.properties`'e yazılır:

    ```properties
    CoolifySiteUuid=sfyuo3lhlbdznx7whlflkz3f
    ```

6.  **DNS.** `pantrixui.com` ve `www.pantrixui.com` VPS'in adresine gider. Sertifikaları Traefik, Let's Encrypt'ten ilk yayında alır.
7.  **Apple'ın onayı.** Developer ID sertifikası anahtar zincirinde olmalıdır. Onay için gereken Apple bilgileri bir kez anahtar zincirine, `pantrix-notary` adıyla kaydedilir ve betik onları o addan okur:

    ```bash
    xcrun notarytool store-credentials pantrix-notary
    ```

### 7.2 Her Yayın

Bir yayın, GitHub'daki bir commit'ten yapılır. Ağaçta commit'lenmemiş bir değişiklik varsa ya da yerel `main` GitHub'daki `main` değilse betik başlamaz.

| Ne yayımlanıyor | Adımlar |
| :--- | :--- |
| Yalnızca site | Commit ve push, sonra `deploy/deploy.sh web` |
| Site ve masaüstü uygulaması | `deploy/deploy.sh prepare`, iki değişiklik günlüğüne bakmak, commit ve push, sonra `deploy/deploy.sh release` |
| Yalnızca masaüstü uygulaması | Değişiklik günlüğünde sürümün bölümü, commit ve push, sonra `deploy/deploy.sh desktop` |

Komutlar, sitenin deposunun kökünde çalıştırılır. Sürüm verilmezse bugünün tarihidir; başka bir gün için sona yazılır: `deploy/deploy.sh release 26.11.2`.

*   **Yalnızca site:**

    ```bash
    deploy/deploy.sh web
    ```

*   **Site ve masaüstü uygulaması:** önce değişiklik günlüklerinin bölümü adlandırılır:

    ```bash
    deploy/deploy.sh prepare
    ```

    İki değişiklik günlüğüne bakılır, commit'lenir ve push edilir; sonra:

    ```bash
    deploy/deploy.sh release
    ```

*   **Yalnızca masaüstü uygulaması:**

    ```bash
    deploy/deploy.sh desktop
    ```

*   **Önce denemek:** her şey derlenir, DMG imzalanır, hiçbir yere bir şey gönderilmez:

    ```bash
    deploy/deploy.sh release --dry-run
    ```

*   **Canlıyı yeniden denetlemek:**

    ```bash
    deploy/check.sh https://pantrixui.com
    ```

`web` şunları yapar:

1.  Siteyi ve sunucuyu Gradle'la derler.
2.  İki imajı `linux/amd64` ve `linux/arm64` için yapar, üç etiketle GHCR'ye gönderir: sürüm (`26.10.31`), commit (`08ec2a5`) ve `latest`. VPS kendi mimarisininkini çeker.
3.  Coolify'a yayını POST ile sorar, her 10 saniyede durumunu okur ve bitmesini bekler (yaklaşık 30 saniye).
4.  Canlı adreste `check.sh`'i çalıştırır. `www` yalnızca kalıcı alan adında denenir.

`desktop` şunları yapar:

1.  Uygulamayı sürümüyle derler (`-Psite.version=26.10.31`).
2.  DMG'yi Developer ID ile imzalar, Apple'a onaya gönderir ve onayı içine ekler. Onay birkaç dakika sürer.
3.  GitHub'da `v<sürüm>` sürümünü açar ve `PantrixUI-macOS.dmg`'yi ekler. Notlar, değişiklik günlüğünün o bölümüdür; her paragrafı tek satıra birleştirilir, çünkü GitHub bir sürümün satırlarını kırıldıkları yerde kırar. Sonlarında DMG'nin SHA-256'sı durur.

Dosyanın adı her sürümde aynıdır, bu yüzden `releases/latest/download/PantrixUI-macOS.dmg` hep son sürümü verir. İndirme sayfası sürümü, günü, boyutu ve SHA-256'yı tarayıcıdan GitHub'ın API'sinden okur; gün, sürümün adındaki gündür.

### 7.3 Bir Sürüme Dönmek

Compose dosyası imajları `${SITE_TAG:-latest}` ile çeker. Coolify'da kaynağın ortam değişkenlerine `SITE_TAG=26.10.31` (ya da bir commit) eklenip yeniden yayımlanınca o sürüm çalışır. Değişken silinince yığın yine `latest`'e döner.

### 7.4 Eski Site

Önceki demo, `pantrix-u-i--demo`, Coolify'da durdurulmuş olarak ve aynı alan adlarıyla duruyor. Yeniden başlatılırsa iki kaynak aynı adı taşır ve Traefik'in yönlendirmesi karışır; alan adlarını ondan silmek ya da kaynağı silmek bunu önler.

Eski siteyi açmış bir tarayıcı, ilk açılışta yeni sitenin yalnızca JavaScript'siz okuyucular için yazılmış metnini gösterebilir: eski sitenin önbelleği uygulamayı başlatmaz. Sert bir yenileme (Cmd + Shift + R) ya da geliştirici araçlarında **Application** → **Clear site data** çözer.

### 7.5 Sorun Giderme

| Belirti | Neden ve çözüm |
| :--- | :--- |
| `gh may not write packages` | 7.1'deki izin eksik: `gh auth refresh -h github.com -s write:packages` |
| `main on GitHub is not this commit` | Son commit push edilmemiş |
| Coolify'ın `401` ya da `403` yanıtı | Anahtar yanlış ya da `deploy` yetkisi yok |
| Coolify'ın `404` yanıtı | `CoolifySiteUuid` kaynağın adındaki kimlik; adresin son parçası olmalı |
| Coolify'ın `405` yanıtı | Coolify 4.5'ten beri yayın POST ile istenir; betiğin eski bir kopyası GET gönderiyor |
| Coolify imajı çekemiyor | Paket hâlâ gizli (7.1, adım 3) |
| `www`'nin sertifikası yok | `www.pantrixui.com`, `web`'in alan adlarında değil |
| İndirme sayfası sürümü göstermiyor | Henüz sürüm yok, GitHub'ın API'si o adres için saatlik sınırını (60 istek) doldurmuş, ya da bir CSP `https://api.github.com`'a izin vermiyor |
| macOS uygulamayı açmıyor | DMG onaydan geçmemiş: `--dry-run` onaya göndermez. `spctl -a -vv -t open --context context:primary-signature PantrixUI-macOS.dmg` "Notarized Developer ID" demeli |
