# 🚀 Yayın Rehberi

Bu belge, Pantrix UI'ın sitesinin nasıl yayına alındığını adım adım verir: önce bu makinede, Docker Compose ile; sonra Coolify'da. Neyin nerede çalıştığı ve neden böyle kurulduğu [ARCHITECTURE_GUIDE.md](./ARCHITECTURE_GUIDE.md)'dedir.

## 1. Özet

| Komut | Ne yapar |
| :--- | :--- |
| `deploy/deploy.sh local` | Siteyi ve sunucuyu derler, iki imajı yapar, yığını `http://localhost:4004`'te kaldırır ve denetler |
| `deploy/deploy.sh local --no-gradle` | Aynısını Gradle'ın son çıktısıyla yapar: yalnızca `deploy/` değiştiğinde |
| `deploy/deploy.sh stop` | Yığını durdurur ve konteynerlerini kaldırır; imajlar kalır |
| `deploy/check.sh [adres]` | Denetimleri bir adreste çalıştırır: lokalde ya da canlıda |

## 2. Gerekenler

*   **Docker Desktop**, açık. `docker compose` onunla gelir.
*   **JDK 21** ve Gradle'ın kendisi (`./gradlew`).
*   **GitHub Packages'ın anahtarı:** Pantrix UI'ın bir sürümü GitHub Packages'tan gelir ve GitHub, açık paketler için bile bir anahtar ister. `~/.gradle/gradle.properties`'te `GitHubPackagesUsername` ve `read:packages` yetkili bir token olarak `GitHubPackagesPassword`. Bir `-SNAPSHOT` sürümü yerel Maven deposundan gelir ve anahtar istemez.

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
| Sayfalar | Ana sayfa; `/docs/installation` yönlendirmesiz; Türkçesi; bir bileşen sayfası; olmayan bir adres 404 sayfasıyla ve 404 durumuyla; `llms.txt` UTF-8 düz metin; `sitemap.xml`; `robots.txt` |
| Sıkıştırma ve önbellek | Bir sayfa gzip ile ve `no-cache`; `site.js` `no-cache`; her Wasm dosyası `application/wasm`, gzip ile ve bir yıl önbellekte; sunucunun sürümü yazmıyor, `nosniff` var |
| İkinci ad | `www.pantrixui.com` kalıcı olarak `pantrixui.com`'a gidiyor (lokalde bir `Host` başlığıyla) |
| MCP sunucusu | `initialize` pantrix-ui'den yanıt alıyor; `tools/list`; bir araç çağrısı; GET'in reddi (405); başka bir sitenin sayfasından gelenin reddi (403); 64 KB'ı aşan mesajın reddi (413) |

Herhangi bir adreste çalışır:

```bash
deploy/check.sh https://pantrixui.com
```

## 5. Sorun Giderme

| Belirti | Neden ve çözüm |
| :--- | :--- |
| `Docker is not running` | Docker Desktop'ı açın, komutu yeniden çalıştırın |
| `port is already allocated` | 4004'ü başka bir şey kullanıyor: `SITE_PORT=4100 deploy/deploy.sh local` |
| Gradle `com.pantrixui` paketlerini bulamıyor | GitHub Packages'ın anahtarı eksik ya da `read:packages` yetkisi yok (Bölüm 2) |
| `/mcp` 502 dönüyor | Sunucu ayakta değil: `docker compose … logs server` |
| Yeni bir değişiklik görünmüyor | `--no-gradle` eski çıktıyı kullanır; komutu onsuz çalıştırın |
| Eski imajlar yer kaplıyor | `docker image ls 'ghcr.io/develappsancho/pantrix-ui-site-*'` ile bakın, `docker image rm` ile silin |

## 6. Coolify'da Yayın

Bu bölüm bir sonraki adımda yazılacak: imajların GHCR'ye gönderilmesi, Coolify'da kaynağın kurulması, deneme adresinde denetim, alan adının taşınması ve eski sitenin kaldırılması.
