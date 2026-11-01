---
title: Pantrix'i bir asistana öğretmek
description: Kodlama asistanlarının (Claude, Copilot, Cursor…) Pantrix'i tahminle değil kütüphanenin kendi kaynaklarından öğrenmesi için yazılan belgeler.
---

Burada söylenen her şey bu depoda koşan testlerde ve derlemelerde görülmüştür. Neyin nerede görüldüğü 9. bölümde yazılıdır. Sunucu ve belgeleri kütüphaneyle, onun sürümünde `com.pantrixui:mcp` olarak yayınlanır (4. bölüm).

## 1. Sorun {#problem}

Kütüphaneyi kaynaklarından okumamış bir asistan şunları yapar:

*   **Material'e kaçar:** `PantrixButton` yerine `Button`, `PantrixTheme` yerine `MaterialTheme` yazar. Pantrix'te Material yoktur ve bu kod derlenmez.
*   **Parametre uydurur:** bir bileşende olmayan bir adı verir (`label =`), olan birini atlar ya da tipini yanlış yazar.
*   **Paketi tahmin eder:** `import com.pantrixui.components.Button` gibi var olmayan bir import yazar. Pantrix'in bileşenleri `com.pantrixui.components.elements.button` gibi kendi paketlerindedir.
*   **Kuralları bilmez:** otomasyon etiketinin `tagKey`'den kurulduğunu, metnin Pantrix'in kendi sarmalayıcılarından geçtiğini, ölçülerin tokenlardan geldiğini.

Elle yazılan bir belge bunların bir kısmını çözer ama kütüphane değiştikçe bayatlar. Bir parametre yeniden adlandırılır, belgede eski adı kalır, asistan o eski adı yazar.

:::sketch ai
:::

## 2. Üretilenler {#what-is-written}

:::table
| Dosya | Ne | Ne için |
| :--- | :--- | :--- |
| `llms.txt` | Dizin, [llmstxt.org](https://llmstxt.org)'un biçiminde. Kütüphanenin ne olduğu, artifact'ları, onunla yayınlanan araçlar (detekt kuralları ve bu sunucu), en küçük uygulama ve Pantrix'le yazılan kodun kuralları; demo'nun her sayfası için, bölümünün altında bir satır (bağlantı, özetinin ilk cümlesi, bileşenin adı); her rehber için bir satır (bağlantı, özetinin ilk cümlesi); modüllerin API başvuruları; skill'in bağlantısı. 33 KB kadar, yaklaşık 8.500 token. | Bir asistanın ilk okuyacağı dosya: neyin nerede olduğunu söyler. |
| `llms-full.txt` | Hepsi bir dosyada: aynı açılış, demo'nun 118 sayfası menünün sırasıyla, 24 rehber, ardından hiçbir sayfanın belgelemediği API, modül modül. Bağlantı taşımaz. 4,7 MB kadar, yaklaşık 1,3 milyon token. | Bağlamı çok geniş bir model ya da bir belge dizinine (RAG) yüklemek. |
| `pages/<anahtar>.md` | Demo'nun bir sayfası: özeti, özellikleri, bileşenin kısa örnekleri, derlenen örnekleri (importlarıyla ve kullandıkları yardımcıların koduyla) ve sayfanın belgelediği API. Anahtar web'deki adrestir: `date-picker`. | Tek bir ekran yazdırırken o ekranın bileşenini vermek. |
| `api/<modül>.md` | Bir modülün bütün genel API'si, paket paket: imzalar, açıklamalar, parametreler, enum girdileri, üyeler. | Bir bildirimin tamamına bakmak. |
| `guides/<anahtar>.md` | Uygulamalara dönük bir rehber, İngilizce: kütüphanenin bir parçasının bir uygulama boyunca nasıl kullanıldığı, her Compose Multiplatform uygulamasının orada karşılaştığı sorun, kütüphanenin cevabı, tuzakları ve nasıl sınandığı. Anahtar rehberin `_GUIDE`'sız, küçük harfli adıdır: `docs/guide/date-time/DATE_TIME_GUIDE.en.md`, `guides/date-time.md`'dir. Bağlantıları belgeler arasındaki öteki rehberlere, deponun öteki dosyalarına ise GitHub'da gider. | Bir asistana bir uygulamanın bütün bir parçasının nasıl yapıldığını vermek: testleri, pencereye göre düzeni, tarihler, pano. |
| `pantrix-ui.json` | Kütüphane veri olarak: modüller, sayfalar ve örnekleri, bileşenler, her genel bildirim ve parametreleri. 5 MB kadar. | Araçlar: MCP sunucusu, bir site, bir kod üretici. |
| `pantrix-ui.schema.json` | `pantrix-ui.json`'ın JSON Schema'sı (2020-12). Elle yazılır: `ai/schema/pantrix-ui.schema.json`. | JSON'u okuyan bir aracın neye güvenebileceğini bilmesi. |
| `CHANGELOG.md` | Deponun değişiklik günlüğü, olduğu gibi. | Bir sürümde neyin değiştiği. |
| `tr/pages/<anahtar>.md`, `tr/guides/<anahtar>.md`, `tr/CHANGELOG.md` | Demo sayfalarının, uygulamalara dönük rehberlerin ve değişiklik günlüğünün Türkçesi, aynı biçimde. Bir sayfa demo'nun yazdığı gibidir, örneklerinin kodu da; API'sinde kütüphanenin KDoc'u Türkçesi olan yerde Türkçe, olmayan yerde İngilizcedir. Bileşenlerin anotasyonundan gelenler, açıklamaları ve kısa örneklerinin başlıkları, İngilizce kalır. Bir rehberin bağlantıları öteki rehberlerin Türkçesine gider. Dizin (`llms.txt`) ve MCP sunucusu yalnızca İngilizcesini anar. | Türkçe okuyan bir yer, örneğin sitenin Türkçe sayfaları. |
| `skills/pantrix-ui/` | Skill. `SKILL.md`: nereye bakılacağı, kütüphanenin kuralları ("Core rules"), Material'ın adlarının Pantrix'teki karşılıkları, bir ekranı yazmanın ve kodu gözden geçirmenin adımları, referansların listesi. `references/`: kurulum, tema, yerleşim, formlar, katmanlar, grafikler, diller ve platform servisleri, erişilebilirlik, testler, Material 3. Elle yazılır (`ai/skills/pantrix-ui/`); yalnızca `references/components.md` her derlemede katalogdan yazılır: her bileşen importu, ne için olduğu, anahtar sözcükleri, etiketleri ve sayfasıyla, ve ikonlar. | Bir asistanın kurduğu skill (4. bölüm). `llms.txt` ve prompt'lar kuralları buradan okur; MCP sunucusu skill'in kendisini de verir (`get_rules`, `get_documentation_page`, `pantrix://skill`). |
| `files.txt` | Bütün belgelerin listesi, satırda bir yol. | Jar'ın kaynaklarını listeleyemeyen bir okuyucu: sunucunun `docs` ve `skill` komutları. |
:::

## 3. Üretmek {#making-them}

```bash
./gradlew :ai:mcp:generateAiDocs
```

*   **Nerede:** dosyalar deponun kökündeki `ai/generated/` klasörüne yazılır: `ai/generated/llms.txt`, `ai/generated/pages/button.md`…
*   **Git'e girmez:** klasör `.gitignore`'dadır. Her derleme dosyaları kaynaklardan yeniden yazar.
*   **Elle düzenlenmez:** görev klasörü her çalışmada boşaltıp yeniden yazar; elle yapılan bir değişiklik bir sonraki derlemede gider. Değişiklik kaynağa yapılır (5. bölüm).
*   **Başka ne üretir:** `:ai:mcp:jar`, `:ai:mcp:test` ve `./gradlew build` de görevi çalıştırır. Jar dosyaları `pantrix-ui/` altında taşır.
*   **Ne zaman yeniden üretir:** kaynaklar değişmediyse Gradle sonucu önbellekten getirir (`UP-TO-DATE`, `FROM-CACHE`). Bir KDoc, bir bileşenin anotasyonu, bir demo sayfası ya da onun İngilizcesi değişince görev yeniden çalışır.
*   **Silmek:** `./gradlew clean` klasörü de siler.

### Bir site için

Belgeler bir siteden sunulacaksa adresi verilir; bağlantılar o zaman mutlak olur:

```bash
./gradlew :ai:mcp:generateAiDocs -Ppantrix.ai.baseUrl=https://site-adresi
```

*   Sonundaki `/` verilmezse eklenir.
*   Adres verilmezse bağlantılar görelidir (`pages/button.md`) ve dosyalar yan yana durdukça çalışır.
*   `pantrix-ui.json`'ın `$schema`'sı da adresle yazılır; adres yoksa yalnızca dosya adıdır.

## 4. Bir asistana vermek {#giving-them-to-an-assistant}

### MCP sunucusu

Bir asistan belgeleri en iyi Pantrix'in MCP sunucusundan okur: neyi soracağını kendisi seçer ve yalnızca gerekeni alır. Sunucu kendi başına çalışan tek bir jar'dır: `mcp-<sürüm>-all.jar`. Kütüphaneyle birlikte, `all` sınıflandırıcılı `com.pantrixui:mcp` olarak yayınlanır: `./gradlew publishToMavenLocal`'dan sonra `~/.m2/repository/com/pantrixui/mcp/<sürüm>/`'dedir (yayın rehberi). Bu depoda şöyle üretilir:

```bash
./gradlew :ai:mcp:allJar
```

Jar `ai/mcp/build/libs/mcp-<sürüm>-all.jar`'a yazılır. Bir asistanın ayarlarında onu çalıştıran komut anılır. Claude Code'da:

```bash
claude mcp add pantrix-ui -- java -jar /yol/mcp-<sürüm>-all.jar
```

Ayarlarını bir `mcpServers` alanından okuyan istemcilerde (Claude Desktop, Cursor…):

```json
{
  "mcpServers": {
    "pantrix-ui": {
      "command": "java",
      "args": ["-jar", "/yol/mcp-<sürüm>-all.jar"]
    }
  }
}
```

*   **Çalıştığı yer:** JDK 17 ya da daha yenisi. İstemci jar'ı kendisi başlatır ve onunla stdio üzerinden konuşur.
*   **Araçlar:**

    | Araç | Ne verir |
    | :--- | :--- |
    | `search_components` | Ne için olduklarıyla bulunan bileşenler (`toast`, `bottom sheet`); sorgusuz hepsi |
    | `get_component` | Bileşenin sayfası: özellikleri, örnekleri importlarıyla, API'si |
    | `get_component_metadata` | Bileşen veri olarak: importu, artifact'ı, anahtar sözcükleri, etiketleri, her biçimi ve parametreleri |
    | `search_api`, `get_api` | Bir bildirimi adıyla ya da ne yaptığıyla bulmak, tamamını almak |
    | `search_icons` | `PantrixIcons`'un ikonları |
    | `search_documentation`, `get_documentation_page` | Temanın, pencerenin ve servislerin sayfaları da içinde her sayfa; rehberler (`guide/testing`, `guide/adaptive-usage`…, ya da başlıkları); skill'in referansları (`skill/forms`, `skill/testing`…, ya da skill'in yazdığı bağlantı: `references/forms.md`) ve `skill` anahtarıyla skill'in kendisi |
    | `list_examples`, `get_example` | Bir sayfanın örnekleri, bir örneğin bütün kodu |
    | `get_changelog` | Değişiklik günlüğü ya da bir sürümünün bölümü |
    | `get_rules` | Skill'in kendisi: Pantrix'le yazılan kodun kuralları, Material'ın adlarının Pantrix'teki karşılıkları, bir ekranı yazmanın ve gözden geçirmenin adımları, referansların listesi. Skill'i kurmamış bir asistan da onu buradan okur |
*   **Kaynaklar:** `pantrix://components`, `pantrix://api`, `pantrix://examples`, `pantrix://documentation` (sayfalar, rehberler ve skill'in referansları) ve `pantrix://icons` JSON listeler; `pantrix://llms.txt`, `pantrix://llms-full.txt`, `pantrix://schema` (`pantrix-ui.json`) ve `pantrix://skill` (skill'in bütünü: `SKILL.md` ve referansları) belgelerin kendisi.
*   **Prompt'lar:** `find_component_for_use_case` (bir ihtiyaç için bileşen ve nasıl kullanıldığı), `implement_screen` (bir ekran, kurallarıyla), `write_ui_test` (bir bileşenin testi, etiketleriyle ve `com.pantrixui:testing`'in yardımcılarıyla). Her biri belgelerin söylediğini ve kuralları kendi içinde taşır.
*   **Araç süzgeci:** `java -jar mcp-<sürüm>-all.jar --tools search_components,get_component` sunucuyu yalnızca o araçlarla açar. Arasından seçeceği araç azaldıkça asistan daha az yanılır.
*   **Komutlar:** `java -jar mcp-<sürüm>-all.jar docs <klasör>` bütün belgeleri bir sitenin sunacağı gibi o klasöre yazar; `skill <klasör>` skill'i `<klasör>/pantrix-ui`'ye yazar (ör. `~/.claude/skills`).
*   **stdout yalnızca protokole aittir:** sunucu çalışırken standart çıktısına protokolden başka hiçbir şey yazılmaz, yoksa istemci okuyamadığı satırda düşer. SDK'nın günlükçüsü (kotlin-logging) ilk kaydedicide stdout'a bir satır basar; sunucu bunu kapatır ve standart çıktıya yazan her şeyi standart hataya yönlendirir. Protokol testi bunu ölçer (8. bölüm).
*   **Kütüphane olarak:** sınıflandırıcısız `com.pantrixui:mcp` sunucunun kodu ve belgeleridir; bağımlılıkları POM'undadır. Aynı araçları HTTP üzerinden sunacak bir site onu alır ve sunucusunu `PantrixMcp.server()` ile kurar.
*   **Lisanslar:** jar çalıştığı kütüphanelerin kodunu da taşır. `META-INF/LICENSE` Pantrix'in lisansıdır; `META-INF/THIRD-PARTY.md` paketlediği her kütüphaneyi lisansıyla, sonunda da her modülü sürümüyle sayar; kütüphanelerin kendi lisans dosyaları `META-INF/licenses/` altındadır. Bildirimle jar'ın paketledikleri ayrıldığında derleme durur (6. bölüm).

### Skill

Skill bir asistana Pantrix'le nasıl çalışılacağını öğretir: kuralları, bir ekranı yazmanın adımlarını ve kütüphanenin parçalarının birbirine nasıl oturduğunu. Agent Skills biçimindedir: bir klasör, içinde `SKILL.md` ve referansları. Asistan `SKILL.md`'nin açıklamasından onu ne zaman kullanacağını anlar, referansları gerektikçe okur.

```bash
java -jar mcp-<sürüm>-all.jar skill <klasör>
```

*   **Nereye:** Claude Code bir projenin `.claude/skills/` klasöründeki ve kullanıcının `~/.claude/skills/` klasöründeki skill'leri okur. Uygulamanın deposuna yazılan skill git'le ekibe geçer; kullanıcının klasörüne yazılan her projede açılır. Öteki asistanlar skill'leri kendi klasörlerinden okur; klasörü asistanın belgesi söyler.
*   **Sunucuyla ya da onsuz:** skill, sunucu varsa araçlarını (`search_components`, `get_component_metadata`…), yoksa `docs` komutunun yazdığı belgeleri kullanmayı söyler. İkisi birlikte en iyisidir: skill ne yapılacağını, sunucu kütüphanenin bugünkü hâlini söyler.
*   **Depodaki kopyası kurulmaz:** `ai/skills/pantrix-ui/`'de `references/components.md` yoktur, derlemede yazılır. Kurulacak hâli jar'dakidir ya da `ai/generated/skills/pantrix-ui/`.
*   **Sürüm:** skill kütüphanenin bir sürümüne aittir. Kütüphane güncellenince skill de yeniden yazılır.

### Başka bir projede, baştan sona

Pantrix'i kullanan bir uygulamanın deposu skill'i ve sunucuyu şöyle alır. İlk adım Pantrix'in deposunda, ikincisi her makinede, ötekiler uygulamanın deposunda atılır.

1.  **Kütüphane ve sunucu:** Pantrix'in deposunda `./gradlew clean publishToMavenLocal` ikisini de yayınlar. Uygulama kütüphaneyi `mavenLocal()`'dan alır; bağımlılık satırları skill'in `references/setup.md`'sinde, yayının ayrıntısı yayın rehberindedir.
2.  **Jar:** sunucunun jar'ı artık yerel Maven deposundadır. Her makine onun yerini kendi ortam değişkeninde tutar, ör. kabuğun profilinde:

    ```bash
    export PANTRIX_MCP_JAR=$HOME/.m2/repository/com/pantrixui/mcp/<sürüm>/mcp-<sürüm>-all.jar
    ```
3.  **Skill:** uygulamanın kökünde `java -jar "$PANTRIX_MCP_JAR" skill .claude/skills`. `.claude/skills/pantrix-ui/` git'e girer; ekip skill'i depoyla alır.
4.  **Sunucu:** uygulamanın kökünde bir `.mcp.json`:

    ```json
    {
      "mcpServers": {
        "pantrix-ui": {
          "command": "java",
          "args": ["-jar", "${PANTRIX_MCP_JAR}"]
        }
      }
    }
    ```

    Claude Code `${PANTRIX_MCP_JAR}`'ı her makinenin kendi değişkeniyle açar: dosya git'e girer, jar'ın yolu girmez. Etkileşimli bir oturum depodaki sunucuyu ilk açılışta onaya sorar; `claude -p` sormadan yükler.
5.  **Denemek:** uygulamanın klasöründe `claude` açılır. `/skills` listesinde `pantrix-ui`, `/mcp` listesinde bağlı `pantrix-ui` görünür. "Bir kayıt ekranı yaz" gibi bir istekte asistan skill'i yükler, sunucunun araçlarını çağırır ve Pantrix'in bileşenleriyle yazar (9. bölümdeki deneme).
6.  **Güncel tutmak:** Pantrix güncellenince 1–3 yeniden atılır: kütüphane, jar ve skill aynı sürümden olur. Skill komutu klasörü baştan yazar; değişiklik uygulamanın deposunda bir commit'tir.
7.  **Öteki asistanlar:** ayarlarını bir `mcpServers` alanından okuyan istemciler aynı girdiyi kendi ayar dosyalarında alır. Skill'leri okuyan bir asistan `pantrix-ui` klasörünü kendi skill klasöründen okur. İkisini de okumayan bir asistana `llms.txt` verilir (aşağıda).

### Dosyaları vermek

:::table
| Ne zaman | Ne verilir |
| :--- | :--- |
| Asistan Pantrix'te bir ekran ya da bir özellik yazacak | `llms.txt`. Asistan gerektikçe ilgili sayfayı ya da modülün başvurusunu okur. |
| Tek bir bileşenle çalışılacak | O bileşenin sayfası: `pages/data-table.md`. Sayfada örneklerin kodu importlarıyla, bileşenin bütün parametreleri açıklamalarıyla vardır. |
| Asistanın her şeyi bir seferde bilmesi isteniyor | `llms-full.txt`. Büyüktür: bağlamı geniş bir model ya da bir belge dizini için. |
| Bir araç Pantrix'i okuyacak | `pantrix-ui.json` ve şeması (7. bölüm). |
:::

*   **Dosyayı vermek:** dosya konuşmaya eklenir ya da asistanın proje kurallarında anılır. Claude Code'da bir dosya `@ai/generated/llms.txt` diye anılır; bir `CLAUDE.md` bir dosyayı `@yol` satırıyla içine alır.
*   **Bir uygulamanın deposunda:** henüz bir site yoktur, bu yüzden dosyalar kendi sürümünün belgelerini taşıyan sunucu jar'ından gelir: `docs` komutu onları uygulamanın deposuna kopyalar, oradan anılırlar.

## 5. Belgelerin kaynağı {#where-the-documents-come-from}

Belgelerin her satırı derlemenin zaten tuttuğu bir kaynaktan gelir:

:::table
| Kaynak | Belgelere giren |
| :--- | :--- |
| Kütüphanenin KDoc'u (İngilizce) | Her genel bildirimin, parametrenin, enum girdisinin ve üyenin açıklaması. detekt'in `UndocumentedParameter` kuralı açıklamasız bir parametrede derlemeyi durdurur ([detekt rehberi](/docs/guides/detekt)). |
| `@PantrixComponent` ve `@PantrixExample` | Bileşenin adı, grubu, tek cümlelik açıklaması, anahtar sözcükleri ve kısa örnekleri. |
| Demo'nun sayfaları (`@DemoPage`, `@DemoExample`) | Sayfanın özeti ve özellikleri, örneklerin başlıkları, açıklamaları ve kodu. |
| Demo'nun İngilizcesi (`app/shared/src/commonMain/translations/en`) | Sayfaların ve örneklerin İngilizcesi; örneklerin kodundaki metinler de. |
| README | İlk paragrafı kütüphanenin özeti, ilk Kotlin örneği en küçük uygulamadır. Örneği de derleme kütüphaneye karşı okur (6. bölüm). |
| `ai/skills/pantrix-ui/` | Skill, elle yazılmış hâliyle. `SKILL.md`'nin "Core rules" bölümü kütüphanenin kurallarıdır: skill'in, `llms.txt`'nin, `get_rules`'un ve prompt'ların söylediği, kuralların yazıldığı tek yer. |
| Rehberlerin İngilizcesi (`docs/guide/<anahtar>/X_GUIDE.en.md`) | Uygulamalara dönük rehberler (`guides/<anahtar>.md`). Derleme önce her birini Türkçesine karşı okur (6. bölüm). |
| Rehberlerin Türkçesi (`docs/guide/<anahtar>/X_GUIDE.md`) ve `CHANGELOG.tr.md` | Türkçe belgeler (`tr/guides/<anahtar>.md`, `tr/CHANGELOG.md`): yalnızca İngilizcesi de olan rehberler. |
| Demo'nun kendi metinleri ve KDoc'un Türkçesi (`app/shared/src/commonMain/translations/tr`) | Sayfaların Türkçesi (`tr/pages/<anahtar>.md`). |
:::

*   **Yeni bir bileşen** CONTRIBUTING'in yeni bileşen kuralını yerine getirdiğinde (anotasyonları, KDoc'u, demo sayfası) belgelere kendiliğinden girer. Ayrıca yapılacak bir şey yoktur.
*   **Örneklerin kodu** demo'nun derlenen kodudur; demo'nun gösterdiği gibi, dosyasının kullandığı yardımcılarla birlikte. Kodun gerektirdiği importlar da kodun başına yazılır. Kodun demo uygulamasından çağırdığı birkaç ad (`ownColors()` gibi) uygulamaya aittir ve örneğin altında ne oldukları yazılır.
*   **Bileşenlerin kısa örnekleri** (`@PantrixExample`) derlenmez. Metinlerini bir uygulamanın kaynaklarından aldıkları için (`stringResource(Res.string.save)`) bir uygulamanın yazacağı gibi dururlar. Derlenmedikleri için 6. bölümdeki koruma onları kütüphaneye karşı okur.

## 6. Derlemeyi durduranlar {#what-stops-the-build}

Belgeler bir asistana yanlış bir şey söyleyecekse ya da hiçbir şey söylemeyecekse görev derlemeyi durdurur; sunucunun jar'ı da, paketlediklerinin bildirimi paketledikleri değilse durdurur. Mesaj neyin nerede düzeltileceğini söyler:

:::table
| Ne | Mesaj | Düzeltmek |
| :--- | :--- | :--- |
| Hiçbir sayfanın belgelemediği bir bileşen | `ui/…/Gauge.kt:198: no page of the sample app documents PantrixGauge, …` | Bileşen sayfasının `@DemoPage`'inde, `api`'de anılır. |
| `@PantrixExample`'da kütüphanenin hiçbir biçiminde olmayan adlı bir argüman | ``PantrixButton, "Primary action": `tag` is given to PantrixButton, and no form of PantrixButton has a parameter of that name`` | Örneğin kodu bileşenin bugünkü imzasıyla yazılır. |
| İngilizcesi olmayan bir demo metni | `These texts of the sample app have no English, …` ve eksik metinlerin listesi | `./gradlew :app:shared:generateDemoCatalog` çalıştırılır ve `app/shared/build/reports/demoCatalog/translations/en` altındaki dosyalar doldurulup `translations/en`'e kopyalanır (demo kataloğu). |
| Belgelerde aynı kimliği alacak iki bildirim | `… would share the id … in the documents for coding assistants` | Bildirimlerden birinin adı değiştirilir. |
| Anahtar sözcüğü boş, küçük harfle yazılmamış ya da bir sözcüğü iki kez veren bir bileşen | `ui/…/Button.kt:144: the @PantrixComponent of Button gives no keywords. …` | Anotasyonun `keywords`'üne, bileşeni arayan birinin kullanacağı ve adında geçmeyen sözcükler yazılır: başka tasarım sistemlerinin ve platformların ona dediği, ne için kullanıldığı (snackbar için `toast`, `undo`). Üç ile sekiz, İngilizce ve küçük harfle. |
| Skill'in ya da README'nin ilk örneğinin, kütüphanede olmayan bir adı (`PantrixSlider`) ya da bir tipin olmayan bir girdisini veya üyesini (`ButtonVariant.Primary`) anması | ``ai/skills/pantrix-ui/SKILL.md:73: `ButtonVariant` has no `Primary` `` | Ad kütüphanenin bugünkü adıyla yazılır. |
| Bir Kotlin bloğunda kütüphanede olmayan bir import, import edilmeyen bir kütüphane adı ya da hiçbir biçimde olmayan adlı bir argüman | ``…/references/overlays.md:55: `PantrixDialog` is the library's, and the code does not import it: `import com.pantrixui.components.overlay.dialog.PantrixDialog` `` | Blok importlarıyla ve imzanın bugünkü adlarıyla yazılır. |
| Yayınlanmayan bir artifact ya da kütüphanenin sürümünden başka bir sürüm | ``…/references/setup.md:36: `com.pantrixui:components:0.2.0` is not the version the library is published in, 0.1.0-SNAPSHOT`` | Sürüm `version.properties`'tekidir: sürüm yükseltilince skill'dekiler de yükseltilir. |
| Skill'in biçimi: adı klasörününki değil, açıklaması 1.024 karakterden, `SKILL.md` 500 satırdan uzun; `SKILL.md`'den bağlanmayan bir referans, skill'in bir dosyasına gitmeyen bir bağlantı; 100 satırdan uzun olup başında bölümlerini sayan bir `Contents:` satırı olmayan bir referans | ``…/references/layout.md:1: a reference of 193 lines lists its sections before the first: `Contents: …` `` | Mesajın söylediği yazılır. |
| Elle yazılmış bir `references/components.md` | `… the generator writes this reference; it is not written by hand` | Dosya silinir: onu üretici yazar. |
| Anahtarının klasörü (`docs/guide/<anahtar>/`) dışında duran bir rehber; İngilizcesi olmayan ya da İngilizcesi Türkçesinden ayrılmış, uygulamalara dönük bir rehber: ötekinde olmayan bir başlık, bir tablo satırı, bir kod bloğu, bir liste maddesi ya da bir görsel; öteki dile bağlanmayan bir üçüncü satır; hiçbir dosyaya ya da başlığa gitmeyen, ya da İngilizceden İngilizcesi olan bir rehberin Türkçesine giden bir bağlantı | `docs/guide/date-time/DATE_TIME_GUIDE.en.md:120: a bullet, where the Turkish has a numbered item, docs/guide/date-time/DATE_TIME_GUIDE.md:118` | İngilizce Türkçenin durduğu gibi yazılır; bir parça ikisine birden eklenir ya da ikisinden birden çıkarılır. Türkçe kalan, deponun kendi derlenişine bakan bir rehber `AiDocsPlugin`'deki `REPOSITORY_GUIDES` arasında anılır. |
| Sunucunun jar'ının paketlediği ve `ai/mcp/licenses/THIRD-PARTY.md` bildiriminin listelemediği bir modül, bildirimin listelediği ve jar'ın paketlemediği bir modül, ya da bildirimin andığı ve jar'da olmayan bir lisans metni | ``ai/mcp/licenses/THIRD-PARTY.md: `io.ktor:ktor-sse-jvm` is bundled into the jar, and no list names it: …`` | Bildirimde kütüphanenin bölümü lisansını söyler ve modülü listeler; paketlenen hiçbir jar'ın taşımadığı bir metin bildirimin yanına, `META-INF/licenses/`'ten sonraki yoluyla konur. |
:::

*   **Denetimin sınırı:** kısa örneklerde ve skill'in bloklarında kütüphanenin fonksiyonlarına, yapıcılarına, bir tipin üyelerine (`Rules.minLength`) ve uzantılarına (`form.field`) adla verilen argümanlar denetlenir. Bir sınıfın üst tipinden aldığı üyeler (`ButtonStyle.override`) ve Compose'un fonksiyonları denetlenmez: onların parametrelerini kaynaklardan bilmiyor.
*   **Skill'in blokları derlenmez:** adları, importları, argümanları ve koordinatları her derlemede denetlenir; tipleri denetlenmez. Yazıldıkları gün bir kez derlendiler (9. bölüm).
*   **Hiç verilmeyen sözcükler:** `keywords`'ün anotasyonda varsayılanı yoktur. Onu vermeyen bir bileşen derlenmez; boş bir liste veren bileşeni üretim durdurur.

## 7. Biçim {#the-format}

`pantrix-ui.json`'ın her alanını şeması anlatır (`ai/schema/pantrix-ui.schema.json`). Bir aracın bilmesi gerekenler:

*   **Kimlik:** bir bildirim paketi ve adıyla anılır: `com.pantrixui.components.elements.button.ButtonVariant`. Bir uzantının tipi adının önündedir (`…button.ButtonVariant.colors`). Aynı kimliği paylaşacak fonksiyonların kimliğinde parametrelerinin tipleri parantez içinde durur: `…PantrixButton(String, () -> Unit, …)`. Bir tipin içindeki tip, tipinin kimliğinin ardından gelir (`…ChartPoint.Kind`).
*   **`kind`:** her parametrenin tipi yazıldığı gibi durur, yanında bir sözcük bir çağıranın ondan ne istediğini söyler. İlk gruptakiler bir metnin taşıyabileceği verilerdir, ikinci gruptakiler yalnızca bir programın verebileceği kodlardır:

    | `kind` | Ne | Örnek |
    | :--- | :--- | :--- |
    | `string`, `boolean`, `integer`, `number` | Yazılan bir değer | `"Save"`, `true`, `3`, `0.5f` |
    | `color`, `dimension` | Renk, ölçü | `Color(0xFF6750A4)`, `16.dp` |
    | `enum` | Kütüphanenin bir enum'unun girdisi; enum `ref`'tedir | `ButtonVariant.Text` |
    | `type` | Kütüphanenin başka bir tipinin değeri; tip `ref`'tedir | `NavigationItem(…)` |
    | `list`, `map` | Koleksiyon; `ref` tuttuğu tiptir, kütüphanenin bir tipiyse | `listOf(…)` |
    | `modifier` | `Modifier` | `Modifier.padding(8.dp)` |
    | `composable` | Çağıranın içine çizdiği bir yuva | `{ PantrixText("Save") }` |
    | `function` | Öteki lambdalar, geri çağrılar | `{ save() }` |
    | `generic` | Bildirimin tip parametresi | `T` |
    | `external` | Compose'un ya da Kotlin'in öteki tipleri | `TextStyle`, `ImageVector` |
*   **`ref`:** parametrenin tipi kütüphanenin bir tipiyse ya da bir koleksiyon kütüphanenin bir tipini tutuyorsa o tipin kimliğidir.
*   **Açıklaması boş bir parametre:** bildirimin kendi metni onu adıyla anlatıyordur (`[name]`, Kotlin'in kodlama kurallarının önerdiği gibi). Açıklama bildirimin `description`'ındadır.
*   **`tags`:** bir bileşenin otomasyon etiketlerinin önekleri, belgesinin yazdığı gibi: `Sheet` ve `SheetClose`, `Sheet_<tagKey>` ve `SheetClose_<tagKey>` için. İlki bileşenin kendisininkidir ([test rehberi](/docs/guides/testing)).
*   **`platform`:** bir bildirim her platformdaysa `common`, değilse kaynak kümesinin adıdır: `android`, `ios`, `jvm`, `js`, `wasmJs`, `web`.
*   **`keywords`:** bir bileşeni arayan birinin kullanacağı ve adında geçmeyen sözcükler (snackbar için `toast`, `undo`). Bir arama onları bileşenin adıyla ve açıklamasıyla birlikte okur; sayfanın bileşen satırında da yazılırlar.

## 8. Doğrulamak {#checking-them}

```bash
./gradlew :ai:mcp:test
```

*   Şema JSON Schema 2020-12'nin meta-şemasına, belge şemaya uyar; şema bilmediği bir alanı ve eksik bir zorunlu alanı hata sayar.
*   Belgenin bir yerinde adı geçen her kimlik (bir sayfanın API'si, bir bileşenin biçimleri ve parçaları, bir parametrenin `ref`'i) belgede vardır.
*   Her sayfanın ve her modülün Markdown'ı jar'dadır.
*   `llms.txt` llmstxt.org'un biçimindedir. Her sayfa kendi bölümünün altında bir kez anılır, her bağlantının hedefi jar'dadır ve dizin 50.000 karakterin altında kalır. `llms-full.txt` her sayfanın Markdown'ını ve sayfası olmayan her bildirimi tutar.
*   **Sunucu:** her araç jar'ın belgelerine karşı sınanır (`toast` `Snackbar`'ı bulur, `Buton` `Button`'ı önerir). Jar bir asistanın çalıştırdığı gibi ayrı bir süreçte çalıştırılır ve onunla JSON-RPC konuşulur: listeler, bir araç, bir kaynak ve bir prompt. Standart çıktısında protokolden başka satır olmadığı ve girişi kapanınca sıfırla çıktığı ölçülür. `get_rules` skill'in kendisini, `get_documentation_page` referanslarını anahtarlarıyla ve skill'in bağlantılarıyla verir; arama referansları da bulur; `pantrix://skill` skill'in bütünüdür. Araç süzgeci ve olmayan bir aracın reddi de sınanır. `docs` ve `skill` komutlarının yazdıkları da sınanır; skill Agent Skills biçimine uyar (adı klasörünün adı, açıklaması 1.024 karakterden kısa, `SKILL.md` 500 satırın altında).
*   **Skill:** `skill` komutu referansları da yazar ve skill'in her bağlantısı onun bir dosyasına gider; `references/components.md` her bileşeni anar. `llms.txt` kuralları `SKILL.md`'nin yazdığı gibi söyler ve skill'e bağlanır.
*   **Rehberler:** jar uygulamalara dönük 24 rehberi İngilizce taşır, her birini anahtarıyla ve Türkçesine giden satırı olmadan; arama onları bulur, `get_documentation_page` onları anahtarlarıyla, yollarıyla ve başlıklarıyla verir; bağlantıları belgelere ya da deponun dosyalarına gider.
*   **Jar'ın lisansları:** `META-INF/LICENSE` Pantrix'inkidir ve paketlenen hiçbir jar'ın lisansı jar'ın kökünde ya da `META-INF`'inde durmaz; bildirim jar'ın paketlediği her modülü sürümüyle listeler ve andığı her metin jar'dadır.

Üreticinin kendi testleri `./gradlew :build-logic:test` ile koşar: tiplerin sınıflandırması, kısa örneklerin, skill'in, rehberlerin iki dilinin ve jar'ın bildiriminin denetimi, JSON yazıcısı, okuyucu ve korumalar, birebir beklenen Markdown, `llms.txt`, rehberlerin belgeleri ve bileşen dizini.

### Gerçek bir asistanda denemek

Skill'in ve sunucunun bir asistanda işe yaradığı, aynı görev iki klasörde verilerek görülür. Klasörlerin ikisi de Pantrix'e bağlı küçük bir uygulamanın iskeletidir (`settings.gradle.kts`, bağımlılığıyla `composeApp/build.gradle.kts`, `PantrixTheme`'i açan bir `App.kt`). Birine tarifteki gibi skill ve sunucu kurulur (4. bölüm), ötekine hiçbiri. Claude Code'un başsız kipi görevi bir kez koşar ve çağırdığı her aracı yazar:

```bash
claude -p "$(cat gorev.txt)" --mcp-config .mcp.json --strict-mcp-config \
  --setting-sources project,local --permission-mode acceptEdits --permission-prompts none \
  --allowedTools "mcp__pantrix-ui Skill" --disallowedTools "Bash WebFetch WebSearch" \
  --max-budget-usd 3 --no-session-persistence --output-format stream-json --verbose > oturum.jsonl
```

*   **Yalnızca bu sunucu:** `--mcp-config` ve `--strict-mcp-config` makinenin öteki MCP sunucularını dışarıda bırakır. Karşılaştırma klasöründe `--mcp-config` verilmez: orada hiç sunucu olmaz.
*   **Yalnızca bu proje:** `--setting-sources project,local` kullanıcının kendi ayarlarını, eklentilerini ve kancalarını dışarıda bırakır.
*   **İzinler:** dosyalar yalnızca klasörün içinde yazılır; Bash ve web kapalı; sunucunun araçları ve skill açık. Onay isteyecek her şey kendiliğinden reddedilir (`--permission-prompts none`).
*   **Tavan:** `--max-budget-usd` bir oturumun harcamasını sınırlar.
*   **Okumak:** `oturum.jsonl`'de bir `Skill` çağrısı skill'in yüklendiğini, `mcp__pantrix-ui__…` çağrıları sunucunun kullanıldığını gösterir; son satır turları, süreyi ve maliyeti yazar. Yazılan ekran bu depoda kütüphaneye karşı derlenir: uygulamanın `Res` sınıfı için bir taslakla, Material'ın olmadığı bir modülün JVM testlerinde.
*   **Görev** (9. bölümdeki denemenin `gorev.txt`'si): "This is a Compose Multiplatform app; its shared code is in composeApp/src/commonMain. Write a sign-up screen in composeApp/src/commonMain/kotlin/com/example/app/SignUpScreen.kt: a composable `SignUpScreen(onSignedUp: () -> Unit)` with fields for the name, the e-mail address and the password, a checkbox to accept the terms, and a button that signs up. The fields are checked, and an error shows under a field that is wrong. In a wide window a short panel about the app stands beside the form; in a narrow one only the form shows. Put every text the screen shows in composeApp/src/commonMain/composeResources/values/strings.xml. Use the UI library this project depends on, and write only these two files."

## 9. Doğrulananlar ve doğrulanmayanlar {#verification}

### Doğrulananlar

*   **Testler:** `:ai:mcp`'nin 28 ve `build-logic`'in 163 testi geçiyor.
*   **Sunucunun süresi:** süreç başlayıp `initialize`'ı 0,35 saniye kadarda yanıtlıyor; ilk araç çağrısı 0,03–0,07 saniye, sayfaların hepsini ilk kez tarayan belge araması yarım saniye kadar sürüyor.
*   **Korumalar:** her biri gerçek kaynaklar bozularak denendi (bir örneğin argümanı, bir sayfanın `api`'si, bir İngilizce metin); her biri 6. bölümdeki mesajıyla durdu.
*   **Rehberlerin denetimi:** koruma on iki yoldan bozuldu (bir tablonun satırları, bir liste maddesinin türü, bir görsel, yorumlarıyla okunan Kotlin, İngilizcenin bir Türkçeye bağlantısı, bir bağlantının gittiği başlık, öteki dile giden satır, İngilizcesi verilen bir depo rehberi, koddaki bağlantılar, Türkçeye giden satırın kalması, bloklardaki bağlantılar, bir rehberin Türkçesinin İngilizcesine gitmemesi); her biri bir testi kırmızıya çevirdi. Koruma derlemeye yazılmadan önce her rehberin İngilizcesi onunla Türkçesine karşı okundu.
*   **Yayın (2026-10-25):** `com.pantrixui:mcp` `~/.m2`'ye değil, karalama bir Maven deposuna yayınlandı. Modül meta verisinde dört çeşit var (`apiElements`, `runtimeElements`, `sourcesElements`, `allJarElements`), POM'unda sunucunun dört bağımlılığı. Ayrı bir projede `com.pantrixui:mcp` kütüphane jar'ını ve 32 bağımlılığını, `com.pantrixui:mcp:0.1.0-SNAPSHOT:all` yalnızca kendi başına çalışan jar'ı verdi.
*   **Lisansların denetimi:** bildirim üç yoldan bozuldu (bir modül çıkarıldı, bir modül eklendi, bir metin kaldırıldı); her biri 6. bölümdeki mesajıyla durdu. Paketlenen jar'ların lisansları dışarıda bırakılmadan yapılan bir jar, jar'ın testini kırmızıya çevirdi.
*   **Skill'in denetimi:** skill on altı yoldan bozuldu (bir enum girdisi, bir üye, bir argüman, bir import, bir sürüm, bir artifact, bir bağlantı, adı, içindekileri…); her biri 6. bölümdeki mesajıyla, dosyası ve satırıyla durdu.
*   **Gerçek bir asistan (2026-10-25):** yukarıdaki görev Claude Code 2.1.294'te, Opus 5.5 ile iki klasörde verildi.
    *   **Skill ve sunucu kurulu:** asistan skill'i kendiliğinden yükledi, `forms` ve `layout` referanslarını okudu, sunucudan `get_component`, `get_documentation_page` ve altı kez `get_api` çağırdı. Ekranı yalnızca Pantrix'le yazdı: forma bağlı alanlar ve kurallar, metinler kaynaklardan, değerler temadan, düğmede `tagKey`, pencereye göre panel (`isNarrow`, `isShort`, `byWidth`). Kod ilk denemede, uyarısız derlendi. 22 tur, 81 saniye, 0,85 $.
    *   **Hiçbiri kurulu değil:** asistan Pantrix'in API'sini göremedi (yerel Maven deposunu okuma isteği reddedildi). Ekranı düz Compose Foundation'la yazdı: `BasicText`, `BasicTextField`, elle yazılmış renkler (`Color(0xFF…)`), hiçbir kontrolde etiket yok. Kod derlendi, çünkü Pantrix'i hiç kullanmıyor. 11 tur, 80 saniye, 0,53 $.
*   **Skill'in kodu:** 17 Kotlin bloğu ve README'nin örneği bir modülün JVM testlerine konup kütüphaneye karşı derlendi, uyarısız. Platforma özgü üç giriş noktası (Android, iOS, web) ve Gradle blokları bu yolla derlenemez; onlar örnek uygulamaların kendi kodudur.
*   **Demo kataloğu:** üreticiyle paylaştığı kod ayrıldıktan sonra eski ve yeni kodla bayt bayt aynı çıktı: 226 dosya ve çeviri raporları.
*   **Site adresi:** sonunda `/` olan, olmayan ve hiç verilmeyen adresle denendi.
*   **Klasör ve önbellek:** `clean` klasörü siliyor; görev değişmeyen kaynaklarda önbellekten geliyor.

### Doğrulanmayanlar

*   Gerçek asistan denemesi tek bir görev ve tek bir model (Opus 5.5); başka görevler, modeller ve asistanlar denenmedi. Yazılan ekran derlendi, çizilmedi.
*   `llms.txt`'nin belli araçlarca (Cursor, Copilot…) okunuşu denenmedi.
*   Bir site henüz yok; mutlak bağlantılar yalnızca dosyalarda görüldü.
*   Rehberlerin İngilizcesini bu belgeleri yazan asistan yazdı; derleme onun yapısını ve bağlantılarını Türkçesine karşı okur, ikinci bir okuyucu okumadı.
*   Sunucu Claude Code'da çalıştırıldı (yukarıda); öteki istemcilerde (Cursor, Claude Desktop…) denenmedi.
*   Sunucu yerel Maven deposuna ya da GitHub Packages'a yayınlanmadı, bir istemci de onu oradan almadı. Lisansların metinleri istedikleri gibi jar'a kondu; bir hukukçu okumadı.
