# 🏗️ Barındırma Mimarisi Rehberi

English: [ARCHITECTURE_GUIDE.en.md](./ARCHITECTURE_GUIDE.en.md)

Bu belge, Pantrix UI'ın sitesinin (`pantrixui.com`) bir sunucuda nasıl çalıştığını anlatır: hangi parçalardan oluştuğunu, bir isteğin hangi yoldan geçtiğini, nginx'in ne olduğunu ve burada neden kullanıldığını, imajların nasıl yapıldığını, lokal ile canlı arasındaki farkı. Şekiller sunumlarda olduğu gibi kullanılabilir: her birinin resmi [`images/`](./images) klasöründedir, altında da açılıp kapanan Mermaid kaynağı durur. Bir şeklin kaynağı değişince resimleri `node docs/deploy/draw-diagrams.mjs` yeniden çizer.

Adım adım yayın [DEPLOY_GUIDE.md](./DEPLOY_GUIDE.md)'dedir; bu belge, o adımların arkasındaki mantığı verir.

## 1. Bir Bakışta

Site iki parçadan oluşur, ikisi de bu depodan derlenir:

*   **Sitenin dosyaları:** Gradle'ın derlediği web uygulaması (Kotlin/Wasm ile Compose; Wasm sürümünü çalıştıramayan bir tarayıcı için Kotlin/JS ile de), her sayfanın statik HTML'i (284 sayfa ve 404 sayfası), paylaşım görselleri, `sitemap.xml`, `robots.txt`, `llms.txt`. Toplam yaklaşık 77 MB. Bunlar değişmeyen dosyalardır: onları sunan şeyin yalnızca göndermesi gerekir, hesaplayacağı bir şey yoktur.
*   **Sitenin sunucusu:** Spring Boot üzerinde, kütüphanenin MCP sunucusunu `/mcp` adresinde HTTP ile sunan küçük bir uygulama. Kodlama asistanları (Claude Code, Cursor…) kütüphanenin belgelerini buradan sorar. Bu, çalışan koddur.

İkisi iki ayrı konteynerde, tek bir Docker Compose yığınında çalışır. Dışarıdan gelen her istek önce nginx'e uğrar: dosyaları nginx kendisi verir, `/mcp`'yi arkadaki sunucuya geçirir.

![Genel bakış: ziyaretçi ve asistan, Traefik, web ve server](images/overview.png)

<details>
<summary>Şeklin Mermaid kaynağı</summary>

```mermaid
flowchart LR
    visitor["Ziyaretçi<br/>tarayıcı"]
    assistant["Kodlama asistanı<br/>Claude Code, Cursor"]
    subgraph host["VPS · Coolify"]
        proxy["Traefik<br/>HTTPS · alan adı"]
        subgraph stack["Docker Compose yığını"]
            web["web<br/>nginx · port 80<br/>sitenin dosyaları"]
            server["server<br/>Spring Boot · port 8080<br/>MCP sunucusu"]
        end
    end
    visitor -- "pantrixui.com/..." --> proxy
    assistant -- "pantrixui.com/mcp" --> proxy
    proxy -- "http, port 80" --> web
    web -- "yalnızca /mcp" --> server
```

</details>

| Parça | Ne yapar | Nerede çalışır | Dışarıya açık mı |
| :--- | :--- | :--- | :--- |
| Traefik | Coolify'ın önündeki vekil: HTTPS sertifikasını tutar, gelen isteği alan adına göre doğru uygulamaya yollar | VPS'te, Coolify ile gelir | Evet: 80 ve 443 |
| `web` (nginx) | Sitenin dosyalarını sunar, `/mcp`'yi sunucuya geçirir, `www`'yi ana ada yönlendirir | Konteyner | Yalnızca Traefik'e (lokalde `localhost:4004`'e) |
| `server` (Spring Boot) | MCP mesajlarını yanıtlar | Konteyner | Hayır: yalnızca nginx ulaşır |

> **Tek kapı ilkesi.** Yığına tek bir kapıdan girilir: nginx. Sunucunun portu hiçbir yere yayımlanmaz; ona ancak nginx'in izin verdiği tek yol, `/mcp`, ulaşır.

## 2. Bir İsteğin Yolu

### 2.1 Bir Sayfa

Bir ziyaretçi `https://pantrixui.com/docs/installation`'ı açtığında:

![Bir sayfanın yolu: tarayıcı, Traefik, nginx](images/page-request.png)

<details>
<summary>Şeklin Mermaid kaynağı</summary>

```mermaid
sequenceDiagram
    autonumber
    participant B as Tarayıcı
    participant T as Traefik
    participant N as nginx (web)
    B->>T: GET https://pantrixui.com/docs/installation
    T->>N: GET /docs/installation (http)
    N->>N: try_files: docs/installation/index.html
    N-->>B: 200 · sayfanın HTML'i, gzip ile
    Note over B: Sayfanın metni hemen görünür.<br/>Arama motorları da bunu okur.
    B->>N: GET /site.js
    N-->>B: 200 · no-cache · yükleyici
    Note over B: Çöp toplamalı Wasm var mı?<br/>site-wasm.js, yoksa site-js.js
    B->>N: GET /site-wasm.js
    N-->>B: 200 · no-cache
    B->>N: GET /4f3270175eff673b81ee.wasm, Skia'nınki
    N-->>B: 200 · gzip · bir yıl önbellekte
    Note over B: Uygulama açılır, statik metni gizler<br/>ve sayfayı Compose ile çizer.
```

</details>

İlk yanıt, o sayfanın derleme sırasında yazılmış statik HTML'idir: başlığı, açıklaması, paylaşım görseli ve metni içindedir. Sonra sayfanın kendi `site.js`'i uygulamanın sürümünü seçer: tarayıcı Wasm'ı çöp toplamasıyla çalıştırıyorsa (Chrome 119, Safari 18.2, Firefox 120 ve sonrası) Wasm sürümünü, çalıştırmıyorsa ya da Wasm sürümü ilk karesini çizmeden çökerse, kodu için belleği kalmayan bir telefon tarayıcısında olabileceği gibi, JavaScript sürümünü. İkisi de statik metni gizler ve sayfayı kendisi çizer; hiçbiri çalışamıyorsa ya da tarayıcı betik çalıştırmıyorsa ziyaretçi o metni okur. Sonraki istekler de Traefik'ten geçer; şekilde sadelik için gösterilmedi.

### 2.2 Bir MCP İsteği

Bir asistan kütüphaneye bir soru sorduğunda:

![Bir MCP isteğinin yolu: asistan, Traefik, nginx, Spring Boot](images/mcp-request.png)

<details>
<summary>Şeklin Mermaid kaynağı</summary>

```mermaid
sequenceDiagram
    autonumber
    participant A as Asistan
    participant T as Traefik
    participant N as nginx (web)
    participant S as Spring Boot (server)
    A->>T: POST https://pantrixui.com/mcp · tools/call
    T->>N: POST /mcp
    N->>S: POST http://server:8080/mcp
    S->>S: McpEndpoint, PantrixMcp.http()
    S-->>N: 200 · application/json
    N-->>A: 200 · yanıt
```

</details>

Sunucu **durumsuzdur** (stateless): her POST kendi başına bir mesajdır, iki istek arasında bir oturum tutulmaz. Bu yüzden sunucu her an yeniden başlatılabilir, bir asistanın işi yarıda kalmaz.

### 2.3 Bir Adresin Karşılığı

nginx bir adresi şu sırayla arar:

![Bir adresin karşılığı: dosya, klasörün index.html'i ya da 404](images/address-rules.png)

<details>
<summary>Şeklin Mermaid kaynağı</summary>

```mermaid
flowchart TD
    r["İstek: /yol"] --> f{"/yol bir dosya mı?"}
    f -- "evet" --> s1["Dosyayı gönder"]
    f -- "hayır" --> d{"/yol/index.html var mı?"}
    d -- "evet" --> s2["O sayfayı gönder<br/>200, yönlendirme yok"]
    d -- "hayır" --> s3["404.html'i gönder<br/>durum 404"]
```

</details>

*   **Yönlendirme yok:** `/docs/installation` adresi `/docs/installation/`'a çevrilmez; adres çubuğundaki adres, sayfanın kendi bildirdiği adresle (canonical) aynı kalır ve bir gidiş dönüş daha yapılmaz.
*   **Gerçek 404:** Olmayan bir adres, sitenin "Bu sayfa yok" sayfasını **404 durumuyla** alır. Arama motorları olmayan sayfaları böylece dizine almaz.

## 3. nginx Nedir, Neden Kullanılır?

### 3.1 Tanım

**nginx** ("engine-x" diye okunur), açık kaynaklı bir web sunucusudur. 2004'te Igor Sysoev, çok sayıda eşzamanlı bağlantıyı az bellekle karşılamak için yazdı; bugün web'in en yaygın sunucularından biridir. İki ana rolü vardır:

*   **Web sunucusu:** Diskteki dosyaları HTTP ile gönderir.
*   **Ters vekil (reverse proxy):** Gelen bir isteği arkadaki başka bir uygulamaya iletir, yanıtını alıp istemciye geri verir. İstemci yalnızca nginx'i görür, arkadaki uygulamayı görmez.

Hızı mimarisinden gelir: her bağlantıya bir iş parçacığı açmak yerine birkaç işçi süreç çalıştırır ve her biri binlerce bağlantıyı tek bir olay döngüsünde yürütür (event-driven). Bir dosyayı gönderirken çekirdeğin `sendfile` çağrısını kullanır: dosya, uygulamanın belleğinden geçmeden diskten ağa kopyalanır. Bu sitede nginx yaklaşık 15 MB bellekle çalışır.

![nginx tek kapı: dosyalar ve /mcp](images/reverse-proxy.png)

<details>
<summary>Şeklin Mermaid kaynağı</summary>

```mermaid
flowchart LR
    c["İstemci"] --> n["nginx<br/>tek kapı"]
    n -- "/, /docs/..., *.wasm" --> disk[("Dosyalar<br/>/usr/share/nginx/html")]
    n -- "/mcp" --> app["Uygulama sunucusu<br/>Spring Boot"]
```

</details>

### 3.2 Bu Sitede Yaptığı İşler

Ayarların hepsi tek bir dosyadadır: [`deploy/web/default.conf.template`](../../deploy/web/default.conf.template).

| İş | Ayar | Neden |
| :--- | :--- | :--- |
| Dosyaları sunmak | `root /usr/share/nginx/html` | Sitenin bütün dosyaları imajda bu klasördedir |
| Adres kuralları | `try_files $uri $uri/index.html =404`, `error_page 404 /404.html` | Bölüm 2.3: her sayfa kendi klasörünün `index.html`'idir, yönlendirmesiz; olmayan adres 404 |
| Markdown | `location ~* \.md$`, `default_type text/markdown` | nginx `.md` için bir tür bilmez ve dosyayı kaydedilecek bir dosya olarak gönderir; kodlama asistanlarının belgeleri (beceri, API başvuruları) tarayıcıda okunan metin olarak, UTF-8'le gider |
| Sıkıştırma | `gzip_static on` | Dosyaların `.gz` kopyaları imaj yapılırken bir kez hazırlanır; nginx her istekte yeniden sıkıştırmaz, hazırını gönderir |
| Önbellek | `map $uri $site_cache` ve `Cache-Control` | Bölüm 8: adı içeriğinin karması olan dosya bir yıl, diğerleri her seferinde sorulur |
| `/mcp`'yi geçirmek | `location = /mcp`, `proxy_pass` | Sunucu dışarıya kapalı kalır; nginx yalnızca bu yolu ona iletir |
| İkinci ad | `server_name www.${SITE_HOST}`, `return 301` | `www.pantrixui.com` kalıcı olarak `pantrixui.com`'a gider: sitenin tek bir adı olur (bölüm 6.2) |
| Boyut sınırı | `client_max_body_size 64k` | 64 KB'tan büyük bir mesaj sunucuya ulaşmadan reddedilir (413) |
| Güvenlik başlıkları | `add_header`, `server_tokens off` | Bölüm 9 |

### 3.3 Neden Dosyaları Spring Boot Sunmuyor?

Spring Boot da dosya sunabilir; ama:

*   **İşine uygun araç:** nginx dosya göndermek için yazılmıştır: hazır `.gz`'yi seçmek, önbellek başlıkları, `sendfile`, adres kuralları birkaç satırdır. Spring'de her dosya isteği Java kodundan geçer ve aynı kurallar elle yazılır.
*   **Ayrı yaşam:** Sunucu yeniden başlarken ya da düşmüşken site açık kalır; o sırada yalnızca `/mcp` 502 döner. Tek konteynerde ikisi birlikte düşer.
*   **Küçük sunucu:** Spring Boot yalnızca MCP'ye bakar; JVM'in belleği dosya göndermeye harcanmaz.

### 3.4 Neden nginx, Başkası Değil?

| Seçenek | Artısı | Neden seçilmedi |
| :--- | :--- | :--- |
| **nginx** | En yaygın, olgun; Alpine imajı küçük; eski site de nginx'le sunuluyordu | Seçildi |
| Caddy | HTTPS'i kendisi alır, Brotli'yi kendisi sunar | HTTPS'i zaten Traefik veriyor; daha az bilinen bir ayar dili |
| Apache httpd | `.htaccess` ile esnek | Daha ağır, bu iş için gereğinden fazlası |
| Spring Boot | Tek konteyner | Bölüm 3.3 |
| Bir CDN ya da GitHub Pages | Dünyanın her yerinde yakın | `/mcp` için bir sunucu yine gerekir; alan adı VPS'te |

## 4. Docker, İmaj, Konteyner, Compose

### 4.1 Kavramlar

| Kavram | Ne | Bu projede |
| :--- | :--- | :--- |
| **İmaj** | Bir uygulamanın çalışması için gereken her şeyin değişmez paketi: işletim sisteminden bir parça, çalışma ortamı, dosyalar, başlatma komutu | `pantrix-ui-site-web`, `pantrix-ui-site-server` |
| **Konteyner** | Bir imajın çalışan, yalıtılmış kopyası | `web`, `server` |
| **Dockerfile** | Bir imajın tarifi: hangi tabanın üstüne ne konacağı | `deploy/web/Dockerfile`, `deploy/server/Dockerfile` |
| **Katman** | Dockerfile'ın her adımı bir katmandır; değişmeyen katman yeniden kullanılır, indirilmez | nginx'in ve Java'nın tabanı bir kez çekilir |
| **Compose** | Birlikte çalışan konteynerleri tek bir dosyada tanımlar ve aralarına ortak bir ağ kurar | `deploy/compose.yaml` |
| **Kayıt (registry)** | İmajların saklandığı depo | GitHub'ın imaj deposu GHCR (`ghcr.io`): `ghcr.io/develappsancho/pantrix-ui-site-web` ve `-server`, herkese açık |
| **Etiket (tag)** | Bir imajın sürüm adı | Lokalde derlendiği commit'in kısa karması (`fbc4fe2`); GHCR'de sürüm (`26.10.31`), commit ve `latest` |

### 4.2 İki İmaj

![İki imaj ve katmanları](images/images.png)

<details>
<summary>Şeklin Mermaid kaynağı</summary>

```mermaid
flowchart TB
    subgraph webimg["pantrix-ui-site-web"]
        w1["nginx:1.30-alpine<br/>Alpine Linux ve nginx<br/>51 MB"] --> w2["default.conf.template<br/>ve adres betikleri"] --> w3["Sitenin dosyaları<br/>ve her birinin .gz'si<br/>83 MB"]
    end
    subgraph srvimg["pantrix-ui-site-server"]
        s1["eclipse-temurin:21-jre-alpine<br/>Alpine Linux ve Java 21<br/>163 MB"] --> s2["server.jar<br/>Spring Boot ve PantrixMcp<br/>51 MB"]
    end
```

</details>

*   **Dockerfile'lar hiçbir şey derlemez.** Gradle bu makinede derler; Docker yalnızca paketler. Böylece derleme için gereken her şey (JDK, Gradle, GitHub Packages'ın anahtarı) imaja girmez.
*   **Web imajı iki aşamada yapılır** (multi-stage build). İlk aşama, Alpine'de dosyaları sıkıştırır; ikinci aşama nginx'in üstüne yalnızca sonucu koyar. Sıkıştırma aracı canlı imaja girmez. İlk aşama imajı yapan makinenin kendi mimarisinde çalışır, bu yüzden bir `amd64` imajı bir Mac'te de hızla yapılır.
*   **Sunucu imajı tek aşamadır:** Java 21'in çalışma ortamı ve `bootJar`'ın yaptığı, kendi başına çalışan jar.

### 4.3 Yığın ve Ağı

Compose, yığın için özel bir ağ kurar (`pantrix-ui-site_default`). Konteynerler birbirine servis adıyla ulaşır: nginx, sunucuyu `http://server:8080` olarak bilir. Bu adı Docker'ın kendi DNS'i (`127.0.0.11`) çözer. nginx bu adı her istekte yeniden sorar; böylece sunucu henüz ayağa kalkmamışken de nginx başlar ve site açılır.

![Yığın ve ağı: web, server ve Docker DNS](images/stack-network.png)

<details>
<summary>Şeklin Mermaid kaynağı</summary>

```mermaid
flowchart LR
    subgraph net["Yığının ağı"]
        web["web<br/>nginx :80"] -- "server:8080" --> server["server<br/>Spring Boot :8080"]
        dns["Docker DNS<br/>127.0.0.11"]
        web -. "server kim?" .-> dns
    end
    out["Dışarısı"] -- "lokalde 127.0.0.1:4004<br/>canlıda Traefik" --> web
```

</details>

## 5. Derlemeden Yayına

![Derlemeden yayına: Gradle, Docker, lokal yığın, GHCR, Coolify](images/build-to-deploy.png)

<details>
<summary>Şeklin Mermaid kaynağı</summary>

```mermaid
flowchart LR
    subgraph mac["Geliştiricinin Mac'i"]
        g["Gradle<br/>wasmJsBrowserDistribution, prerender<br/>bootJar"] --> d["docker compose build<br/>iki imaj"]
        d --> l["Lokal yığın<br/>localhost:4004"]
        l --> c["check.sh<br/>sayfalar, 404, gzip, önbellek, /mcp"]
    end
    d -. "deploy.sh web: push" .-> r[("GHCR<br/>ghcr.io/develappsancho")]
    r -. "pull" .-> v["Coolify · VPS<br/>pantrixui.com"]
```

</details>

İmajlar sunucuda değil, bu makinede derlenir:

*   **VPS'i yormamak:** VPS'te başka uygulamalar da çalışır. Wasm derlemesi gigabaytlarca bellek ve dakikalarca işlemci ister; Gradle'ın kendisi de öyle.
*   **Denenen, canlıya giden:** Lokalde `check.sh`'ten geçen imajlar, GHCR'ye gönderilen imajlardır. Canlıda yeniden bir derleme yapılmaz, bu yüzden aradaki fark da olmaz.
*   **Anahtar makinede kalır:** Kütüphaneyi GitHub Packages'tan çekmek için gereken anahtar yalnızca bu makinedeki Gradle'dadır. Ne imaja ne de VPS'e girer.

## 6. Lokal ve Canlı: Aynı Yığın, İki Yer

### 6.1 Farklar

| | Lokal | Canlı (Coolify) |
| :--- | :--- | :--- |
| Komut | `deploy/deploy.sh local` | `deploy/deploy.sh web` |
| Compose dosyaları | `compose.yaml` ve `compose.local.yaml` | `compose.yaml` |
| İmajlar | Bu makinede derlenir | GHCR'den çekilir |
| Adres | `http://localhost:4004` | `https://pantrixui.com` |
| HTTPS | Yok: yalnızca bu makine erişir | Traefik, Let's Encrypt'in sertifikasıyla |
| Port | `127.0.0.1:4004` → `web:80` | Traefik → `web:80`; makinede açılan port yok |
| `www` | `check.sh` bir `Host` başlığıyla dener | DNS ve Coolify'daki alan adı |

`compose.local.yaml` yalnızca üç şey ekler: imajların bu makinede nasıl yapılacağı (`build`), portun dışarıya açılması (`ports`) ve yığının adresi (`SITE_URL`). Gerisi ortaktır.

### 6.2 Sitenin Adresi

Sitenin adresi hiçbir dosyada elle yazılmaz. Tek bir yerde durur ve her ortam kendi adresini verir: lokaldeki site kendi sayfalarına, MCP sunucusuna ve `llms.txt`'sine `http://localhost:4004` üzerinden götürür, canlıdaki `https://pantrixui.com` üzerinden.

![Sitenin adresi: site.url, SITE_URL ve tarayıcının adresi](images/site-address.png)

<details>
<summary>Şeklin Mermaid kaynağı</summary>

```mermaid
flowchart LR
    prop["gradle.properties<br/>site.url"] --> build["Derleme<br/>statik sayfalar, sitemap, llms.txt,<br/>içerik, paylaşım görselleri,<br/>uygulamaların adresi"]
    build --> image["web imajı"]
    env["compose · SITE_URL<br/>lokalde localhost:4004<br/>canlıda pantrixui.com"] --> start["web konteyneri açılırken<br/>05-site-address.envsh<br/>40-site-address.sh"]
    image --> start
    start --> files["Dosyalardaki adres<br/>ve www yönlendirmesi<br/>SITE_URL olur"]
    env --> srv["server<br/>izinli kaynak: SITE_URL"]
    browser["Tarayıcıdaki uygulama"] -- "window.location.origin" --> app["MCP, llms.txt ve<br/>404'teki adres"]
```

</details>

*   **Kalıcı adres, `site.url`:** `gradle.properties`'te tek satır. Derleme onu statik sayfalara, `sitemap.xml`'e, `robots.txt`'ye, `llms.txt`'ye, paylaşım görsellerine ve masaüstü, Android ve iOS uygulamalarına (`BUILT_SITE_ADDRESS`) yazar. İçerik sayfaları adresi `{{site}}` diye anar; içerik aracı onu da bu adresle yazar.
*   **Yığının adresi, `SITE_URL`:** Compose'un ortam değişkeni. `web` konteyneri açılırken dosyalardaki adresi bununla değiştirir ve `.gz`'lerini yeniden yapar; nginx'in `www` yönlendirmesi de bu adrese gider. `server` bu adresin sayfalarına izin verir. Boşsa site derlendiği adreste sunulur. Değiştirmek için yeniden derlemek gerekmez; konteyneri yeniden başlatmak yeter.
*   **Web uygulaması:** Adresi gömmez, açıldığı adresi kendisi okur (`SiteAddress`, `window.location.origin`). Aynı derleme her adreste kendini gösterir.

| Adresi kullanan | Nereden alır | Ne zaman |
| :--- | :--- | :--- |
| Statik sayfalar, `sitemap.xml`, `robots.txt`, `llms.txt`, içerik | `site.url`, sonra `SITE_URL` | Derlemede; `web` konteyneri açılırken |
| Web uygulamasının gösterdiği adresler: MCP, `llms.txt`, 404 sayfası | Açıldığı adres | Her açılışta |
| Masaüstü, Android ve iOS uygulamaları | `site.url` | Derlemede |
| Paylaşım görselleri | `site.url` | Derlemede |
| nginx'in `www` yönlendirmesi | `SITE_URL` | `web` konteyneri açılırken |
| Sunucunun izin verdiği kaynak | `SITE_URL`, yoksa `site.url` | `server` açılırken |

Konteyner yalnızca şemasıyla yazılmış tam adresi değiştirir: `https://pantrixui.com`. Şemasız bir alan adı bir sözcüktür. Rehberlerin örnek kodları da onu böyle kullanır (`"pantrixui.com/tr"`), bu yüzden olduğu gibi kalır.

**Yeni bir alan adı** için `gradle.properties`'teki `site.url` değiştirilir, site yeniden derlenir ve Coolify'da `SITE_URL` yeni adres yapılır. Kodda değişen bir şey olmaz.

## 7. Coolify ve Traefik

*   **Coolify:** Kendi sunucunuzda çalışan, açık kaynaklı bir yayın platformudur (PaaS): Heroku ya da Vercel'in bir benzeri, ama kendi VPS'inizde. Uygulamaları konteyner olarak çalıştırır, alan adı ve sertifika verir, panelden ya da API'den yeniden yayımlar. Bu VPS'te 4.5.3 sürümü çalışıyor.
*   **Traefik:** Coolify'ın kurduğu ters vekildir. 80 ve 443 portlarını dinler, Let's Encrypt'ten sertifika alır ve yeniler, gelen isteğin alan adına bakıp doğru konteynere yollar.

Böylece önde iki vekil vardır ve ikisi farklı soruları yanıtlar: Traefik "**hangi uygulama?**", nginx "**bu uygulamada hangi dosya, hangi servis?**"

![Traefik ve nginx: hangi alan adı, hangi yol](images/traefik-nginx.png)

<details>
<summary>Şeklin Mermaid kaynağı</summary>

```mermaid
flowchart LR
    i["İnternet<br/>443"] --> t["Traefik<br/>hangi alan adı?"]
    t -- "pantrixui.com" --> w["web · nginx<br/>hangi yol?"]
    t -- "diğer alan adları" --> o["VPS'teki diğer<br/>uygulamalar"]
    w -- "/mcp" --> s["server"]
    w -- "diğer yollar" --> f[("dosyalar")]
```

</details>

Coolify, depodaki `deploy/compose.yaml`'ı okur ve imajları GHCR'den çeker; hiçbir şey derlemez. Kurulumun ve her yayının adımları [DEPLOY_GUIDE.md](./DEPLOY_GUIDE.md)'nin 7. bölümündedir.

## 8. Sıkıştırma ve Önbellek

### 8.1 Sıkıştırma

Dosyaların `.gz` kopyaları imaj yapılırken `gzip -9` ile bir kez hazırlanır; nginx, gzip kabul eden tarayıcıya hazır kopyayı gönderir. Ölçülen boyutlar:

| Dosya | Ham | gzip | Kazanç |
| :--- | ---: | ---: | ---: |
| Uygulamanın Wasm'ı (`4f327017….wasm`) | 14,5 MB | 3,8 MB | %74 |
| Skia'nın Wasm'ı (`658820f7….wasm`), iki sürümün de çizdiği | 8,6 MB | 3,3 MB | %62 |
| `site-wasm.js`, Wasm sürümünün betiği | 340 KB | 60 KB | %82 |
| `site-js.js`, JavaScript sürümü | 12,4 MB | 3,4 MB | %73 |
| `site.js`, yükleyici | 4,3 KB | 1,7 KB | %61 |
| Bir sayfanın HTML'i | 8,9 KB | 3,5 KB | %61 |
| `llms-full.txt` | 4,7 MB | 0,95 MB | %80 |

Bir ziyaretçi, ilk açılışta uygulama için 23 MB yerine yaklaşık 7 MB indirir, JavaScript sürümü için de aşağı yukarı o kadar; ikinci açılışta Wasm'ı önbellekten okur.

### 8.2 Önbellek

| Dosya | `Cache-Control` | Neden |
| :--- | :--- | :--- |
| `<karma>.wasm` | `public, max-age=31536000, immutable` | Adı, içeriğinin karmasıdır: yeni bir derleme yeni bir ad verir. Tarayıcı bu dosyayı bir yıl boyunca bir daha sormaz |
| Diğerleri: HTML, betikler (`site.js`, `site-wasm.js`, `site-js.js`), `styles.css`, fontlar, görseller, `llms.txt` | `no-cache` | Adları sabittir. Tarayıcı her seferinde sorar; dosya değişmediyse nginx gövdesiz bir `304 Not Modified` döner |

`no-cache`, "önbelleğe alma" değil, "kullanmadan önce sor" demektir. Tarayıcı, elindeki kopyanın `ETag`'ini gönderir; dosya aynıysa yanıt birkaç yüz bayttır. Böylece yeni bir yayın anında görünür, eski bir betik artık olmayan bir Wasm'ı aramaz.

## 9. Güvenlik

*   **Tek kapı:** Dışarıya yalnızca `web` açıktır; `server`'ın portu hiçbir yere yayımlanmaz.
*   **Yetkisiz kullanıcı:** Sunucu, root değil, hiçbir yetkisi olmayan bir kullanıcıyla (`65534`, nobody) çalışır.
*   **Sınırlar:** Mesaj 64 KB'ı aşamaz (nginx), yanıt 30 saniyeyi aşamaz (sunucu). Sunucunun belleği 512 MB'la sınırlıdır, Java'nın yığını bunun dörtte üçüdür; bellek biterse sunucu durur ve Docker onu yeniden başlatır.
*   **Kaynak denetimi:** Başka bir sitenin sayfasından, tarayıcıyla gelen bir MCP isteği reddedilir (403). Asistanların kendi süreçleri bir kaynak (Origin) göndermez ve yanıtlanır.
*   **İmajlarda sır yok:** İmajlarda yalnızca derlenmiş çıktı vardır. GitHub Packages'ın anahtarı derleyen makinede kalır; imajlar bu yüzden herkese açık olabilir.
*   **Başlıklar:** `X-Content-Type-Options: nosniff` (tarayıcı bir dosyanın türünü tahmin etmez), `Referrer-Policy`, `X-Frame-Options: SAMEORIGIN` (site başka bir sitenin çerçevesine konamaz), `server_tokens off` (nginx'in sürümü söylenmez).
*   **HTTPS:** Traefik'te sonlanır. nginx, yığın HTTPS'te sunuluyorsa `Strict-Transport-Security: max-age=300` gönderir: tarayıcı siteye beş dakika boyunca yalnızca HTTPS'le gelir. Kısa süre bir denemedir; alan adının HTTPS'i bir süre sorunsuz görülünce bir haftaya, sonra bir yıla çıkar. Düz HTTP'de, lokalde, başlık gönderilmez (`05-site-address.envsh`'in `SITE_HSTS`'i).
*   **İçerik güvenliği politikası (CSP):** şimdilik yalnızca raporlanır (`Content-Security-Policy-Report-Only`): hiçbir şeyi engellemez, tarayıcı uymayanı konsoluna yazar. Sayfanın yükleyebildikleri:
    *   **betik:** sitenin kendisi, WebAssembly'si (`'wasm-unsafe-eval'`) ve her sayfadaki tek satır içi betik, karmasıyla. `check.sh` karmanın sayfadakiyle aynı olduğunu denetler;
    *   **stil:** sitenin kendisi ve Compose'un tuvalinin gölge köküne yazdığı stil (`'unsafe-inline'`): Compose'la değiştiği için karmayla tutulmaz;
    *   **bağlantı:** sitenin kendisi ve GitHub'ın API'si (indirme sayfası). Google'ın yazı tipleri yok: Compose for Web, sitenin yazı tiplerinde olmayan bir karakter için `fonts.gstatic.com`'dan bir Noto yazı tipi indirirdi (Symbols 2, Math ve Arabic görüldü). Sitenin yazı tipi o karakterlerin yedeklerini taşır: Mac tuşlarının işaretleri için kütüphanenin Pantrix Symbols'ı, Arapça için Noto Sans Arabic'in bir alt kümesi, sitenin kendi işaretleri için Pantrix Site Symbols (`SiteTypefaces`, `tools/fonts/site_fonts.py`);
    *   **çerçeve:** yalnızca site kendini çerçeveleyebilir (`frame-ancestors 'self'`).

    Yirmi sayfada ve aramada hiç rapor çıkmadı; canlıda bir süre rapor çıkmazsa politika `Content-Security-Policy` olarak uygulanır.

## 10. Seçilmeyen Yollar

| Seçenek | Neden seçilmedi |
| :--- | :--- |
| Coolify'da iki ayrı kaynak; `/mcp` yolu Coolify'ın yönlendirmesiyle sunucuya | Ayar iki yerde olur; yol önekinin nasıl iletileceği Coolify'ın davranışına bağlı kalır; lokalde aynı düzen kurulamaz |
| Dosyaları da Spring Boot'un sunması | Bölüm 3.3 |
| VPS'te, Git'ten derlemek | Sunucuyu zorlar; VPS'te başka uygulamalar çalışıyor |
| GitHub Actions'ta derlemek | İleride eklenebilir; şimdilik "lokalde denenen imaj canlıya gider" ilkesi seçildi |
| Her adres için ayrı derlemek | Lokaldeki ve canlıdaki imaj birbirinden ayrılır; deneme adresi de ayrı bir derleme ister. Adres, derlemenin değil, yığının ayarıdır (bölüm 6.2) |

## 11. Dosyalar

| Dosya | Ne |
| :--- | :--- |
| [`deploy/compose.yaml`](../../deploy/compose.yaml) | Yığın: iki servis, imajların adları, sunucunun bellek sınırı |
| [`deploy/compose.local.yaml`](../../deploy/compose.local.yaml) | Lokalde: imajların bu makinede yapılması ve 4004 portu |
| [`deploy/web/Dockerfile`](../../deploy/web/Dockerfile) | Web imajı: sıkıştırma aşaması ve nginx |
| [`deploy/web/default.conf.template`](../../deploy/web/default.conf.template) | nginx'in ayarları; `SITE_URL` ve `SITE_HOST`, konteyner açılırken yazılır |
| [`deploy/web/05-site-address.envsh`](../../deploy/web/05-site-address.envsh), [`deploy/web/40-site-address.sh`](../../deploy/web/40-site-address.sh) | Konteyner açılırken: yığının adresini bulur ve dosyalara yazar |
| [`deploy/server/Dockerfile`](../../deploy/server/Dockerfile) | Sunucu imajı: Java 21 ve `server.jar` |
| [`deploy/deploy.sh`](../../deploy/deploy.sh) | Lokalde ve canlıda yayın: derler, imajları yapar ya da GHCR'ye gönderir, Coolify'a yayını başlatır, masaüstü sürümünü yapar, denetler |
| [`deploy/desktop/macos-dmg.sh`](../../deploy/desktop/macos-dmg.sh), [`deploy/desktop/macos-entitlements.plist`](../../deploy/desktop/macos-entitlements.plist) | macOS uygulamasının DMG'si: Developer ID ile imza, Apple'ın onayı ve onayın DMG'ye eklenmesi |
| [`deploy/check.sh`](../../deploy/check.sh) | Denetimler: herhangi bir adreste, lokalde ya da canlıda |

## 12. Sözlük

| Terim | Anlamı |
| :--- | :--- |
| Ters vekil (reverse proxy) | İstemcinin isteğini arkadaki bir uygulamaya ileten, yanıtı geri getiren sunucu |
| Statik dosya | Sunucunun hesaplamadan, olduğu gibi gönderdiği dosya |
| Prerender | Her sayfanın HTML'inin derleme sırasında yazılması |
| Canonical | Bir sayfanın kendi bildirdiği asıl adresi |
| Karma (hash) | Bir dosyanın içeriğinden hesaplanan kısa parmak izi; içerik değişince değişir |
| ETag ve 304 | Bir dosyanın sürümünü söyleyen başlık, ve "değişmedi, elindekini kullan" yanıtı |
| gzip | Metin ve Wasm dosyalarını küçülten, her tarayıcının açabildiği sıkıştırma |
| MCP | Model Context Protocol: asistanların bir araca soru sorduğu protokol |
| Durumsuz (stateless) | İstekler arasında oturum tutmayan sunucu |
| TLS ve HTTPS | Bağlantının şifrelenmesi; sertifikayı Let's Encrypt verir |
| VPS | Sanal özel sunucu: kiralanan, kendi işletim sistemi olan bir makine |
| PaaS | Uygulamayı yayımlamayı üstlenen platform: burada Coolify |

## 13. Doğrulama

*   **Lokalde görülen (2026-10-30):** `deploy/deploy.sh local` iki imajı yaptı ve yığını `http://localhost:4004`'te kaldırdı; iki konteyner de sağlıklı (`healthy`). `check.sh`'in 21 denetimi geçti: sayfalar, Türkçe sayfa, bileşen sayfası, 404, `llms.txt`, `sitemap.xml`, `robots.txt`, gzip ve önbellek başlıkları, iki Wasm dosyası, `www` yönlendirmesi ve `/mcp`'ye gerçek MCP mesajları (initialize, tools/list, bir araç çağrısı, GET'in, başka sitenin ve 64 KB'ı aşan mesajın reddi). Sayfalar başsız Chrome'da ve iOS simülatöründeki Safari'de çizildi. 512 MB sınırında sunucu, 200 eşzamanlı araç çağrısından sonra 317 MB kullandı ve yeniden başlamadı.
*   **Adres, lokalde görülen (2026-10-30):** `SITE_URL=http://localhost:4004` ile `web` konteyneri açılırken 294 dosyadaki adresi yazdı (yaklaşık 1,3 saniye); dosyalarda canlı adres kalmadı, `.gz`'ler dosyalarıyla aynı, paket adları (`com.pantrixui.components`) ve rehberlerin örnekleri (`"pantrixui.com/tr"`) olduğu gibi. `check.sh`'in 22 denetimi bu adresle geçti: canonical, `llms.txt`, `sitemap.xml`, `www.localhost` yönlendirmesi ve sitenin kendi sayfasından gelen MCP isteği. Tarayıcıda yapay zekâ sayfası ve belgelerdeki asistan sayfası `http://localhost:4004/mcp`, 404 sayfası `localhost:4004/…` gösterdi. Aynı konteyner yeniden başlarken dosyalara dokunmadı; `SITE_URL` verilmeyen bir konteyner derlendiği adresle (`https://pantrixui.com`), başka bir adres verilen konteyner o adresle sunuldu.
*   **Adres, bilinen sınır:** JavaScript'siz okuyucular için gizlenen statik metinde şemasız yazılan adres (`pantrixui.com/mcp`) değişmez; uygulama bu adresi açıldığı adresle gösterir.
*   **Kaynakta okunan:** nginx'in `try_files`, `gzip_static`, `resolver` ve `add_header` davranışları nginx'in belgelerindedir.
*   **Canlıda görülen (2026-10-31):** `deploy/deploy.sh web` iki imajı iki mimari için GHCR'ye gönderdi, Coolify yayını yaklaşık 30 saniyede bitirdi ve `check.sh`'in 23 denetimi `https://pantrixui.com`'da geçti: Traefik'in iki adın sertifikasını Let's Encrypt'ten alması, `www.pantrixui.com`'un kalıcı yönlendirmesi, VPS'te çekilen imajlar ve `/mcp` dahil. Sayfalar başsız Chrome'da çizildi. Masaüstü sürümü `v26.10.31` GitHub'da açıldı; DMG'yi Gatekeeper "Notarized Developer ID" diye kabul etti ve indirme sayfası sürümü, günü, boyutu ve SHA-256'yı GitHub'ın API'sinden gösterdi.
*   **Canlıda bulunan:** Coolify 4.5.3 yayını yalnızca POST ile başlatır (GET'e 405); kaynağın adındaki kimlik UUID'si değildir; GitHub bir sürümün anını UTC ile verir, gece yarısından sonra açılan bir sürüm için bir önceki günü. Üçü de betikte ve sayfada düzeltildi. Eski siteyi açmış bir tarayıcı, sert bir yenilemeye kadar yalnızca statik metni gösterdi.
*   **Henüz denenmeyen:** `SITE_TAG` ile bir sürüme dönmek; HSTS ve içerik güvenliği başlıkları (bölüm 9); Windows ve Linux paketleri.
