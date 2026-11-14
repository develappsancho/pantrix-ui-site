# Değişiklik Günlüğü

English: [CHANGELOG.md](./CHANGELOG.md)

Pantrix UI'ın sitesinde, `pantrixui.com`'da, ve uygulamalarında nelerin değiştiği; onu okuyan ya da derleyen biri için.

*   **Biçim:** bu dosya [Keep a Changelog](https://keepachangelog.com/en/1.1.0/)'u izler. Bir sürüm, bir ziyaretçinin gördüğünü değiştiren bir yayındır ve tarihiyle, üç sayı olarak adlandırılır: `YY.A.G` (`26.11.2`); masaüstü uygulaması da bu sürümü taşır.
*   **Hangi değişiklik satır alır:** hangilerinin aldığını ve satırın nasıl yazıldığını [CONTRIBUTING.tr.md](./CONTRIBUTING.tr.md#değişiklik-günlüğü) anlatır.

## [Yayınlanmamış]

## [26.11.14] - 2026-11-14

Pantrix UI ile yapılan uygulamalar arasında Formatico, onları sayan her sayfada kütüphanenin kendisinde sayılan 108 bileşeni ve dar bir masaüstünde eşit sütunlarda duran ana sayfa sayıları.

### Eklenenler

- **Referanslar:** web'de, Android'de ve Mac'te yapılandırılmış metni biçimlendiren, doğrulayan, gezdiren ve dönüştüren Formatico, Bütün projeler arasında; kartında sayfanın temasında ekranının bir resmi ve açık kaynağına götüren bir etiket.

### Düzeltilenler

- **Bileşen sayısı:** ana sayfa, Giriş, referanslar ve sitenin çizimi 100 dedikleri yerde kütüphanenin 108 bileşenini söyler, kütüphanenin demosu da hepsini sayar; sayıyı derleme kütüphanenin kendisinde sayar.
- **Referanslar:** sitenin çizimi telefonunda "108 bileşene göz at"ı bütün yazar; düğmeleri, yazıları ve kenarları tasarımın çizdiği gibi.
- **Ana sayfa:** sayıların sözleri tek satıra sığmayan bir masaüstü penceresinde sayılar, birbirine sıkışacakları yerde, kısa sözleriyle beş eşit sütunda durur.

## [26.11.11] - 2026-11-11

Site Pantrix UI 0.2.1'de: denenecek çizimleriyle dört yeni rehber, GitHub token'ından insanın kendi markasına giden bir kurulum, becerinin bütün kurallarıyla kodlama asistanları sayfası ve Jetpack Compose kütüphanesinin yapıldığı, 0.2.1'e kadar güncel bir yol haritası.

### Eklenenler

- **Dört rehber:** uygulamanın kendi sözleri okurunun dilinde, bir metni başka bir uygulamaya göndermek, bir ekranın hâllerini önizlemek ve iOS için küçük bir semantik ağaç; her biri denenecek bir çizimle.

### Değişenler

- **Pantrix UI 0.2.1:** kütüphaneyi kuran satırlar, ana sayfada, Kurulum'da ve kodlama asistanları için, yapay zekâ sayfasının dosyaları ve beceri; site 108 bileşen ve 29 rehber sayar.
- **Kurulum:** bağımlılık bir sürüm kataloğuyla da, testlerin satırlarıyla; GitHub'ın token'ının nasıl alındığı ve aldığı iki satır; Android Studio sihirbazından gelen bir projede neyin değiştiği; ilk örneğin metni ve importları; adları var olan bir marka örneği; paketlerin istediği `compileSdk`; ve web'in satırı Safari'ye JavaScript sürümünü veren yükleyiciye götürür.
- **Kodlama asistanları sayfası:** becerinin on bir kuralı, tuttuğunun hepsi; ve Claude Code'un eklentisi bütün proje için kurulur, onu gösteren yeni bir oturumla.
- **Yol haritası:** 0.2.1'e kadar yayımlananlar, 0.2.0'ın bileşenleri ve araçları ve bu web sitesiyle; sürümler
  arasında 0.1.1, 0.2.0 ve 0.2.1; Jetpack Compose kütüphanesi yapılıyor; ve sunucu güdümlü UI kartında da 0.3.0 için
  planlandı.

### Düzeltilenler

- **Bir bileşenin sayfası:** 0.2.0'la gelen sekiz bileşen, kod editörü ve ötekiler, bunu söylüyor; her sayfa 0.1.0
  diyordu.
- **Yol haritası:** bugünkü kütüphanenin kartı 0.1.0'ı değil, son sürümünü anıyor.

## [26.11.7] - 2026-11-07

Site telefonda ve masaüstünde yeniden okundu: ana sayfanın turu düğmesini ve tek yüksekliğini korur, bir adımın "isteğe bağlı" sözü ve uyarlanabilir sayfanın çizimleri artık harf harf dizilmez, belgelerin başlıkları da başlık olarak okunur.

### Değişenler

*   **Ana sayfanın turunda her adımın kullandığı parçalar:** tasarımdaki gibi, kodun yazısıyla yazılmış küçük kutucuklar; düz bağlantılardı.

### Düzeltilenler

*   **Ana sayfanın masaüstündeki turu:** turu duraklatan ve oynatan düğme ile gösterilen adım, adımlar sütununun altında yeniden görünür; sütun turun kutusundan uzun olmuştu ve kutunun kenarı onları saklıyordu.
*   **Ana sayfanın turu her pencerede:** adımdan adıma geçerken tek bir yükseklikte kalır, altındaki sayfa da yerinde durur; her adımın açıklamasıyla uzayıp kısalıyordu.
*   **Telefonda atlanabilen bir adım:** "isteğe bağlı" sözü adımın başlığının ardından, son satırında ya da bir alttakinde gelir; satıra sığmayan bir başlığın yanında, kodlama asistanları sayfasında olduğu gibi, sayfanın kenarından aşağı harf harf diziliyordu.
*   **Uyarlanabilir sayfanın kalıpları telefonda:** her biri, telefonun tasarımındaki gibi iki küçük çizim çizer; masaüstünün üç çizimi kendi boyutlarıyla yan yana duruyordu, sonuncusu ince bir şeride sıkışıyor, yazısı harf harf diziliyordu.
*   **Ekran okuyucu için başlıklar:** belgelerde ve rehberlerde sayfanın başlığı, bölümleri ve adımları, rehberlerin konuları ve bir bileşenin örnekleri başlık olarak okunur; ekran okuyucu birinden ötekine geçebilir. Düz metin olarak okunuyorlardı.

## [26.11.2] - 2026-11-02

Site, Pantrix UI 0.1.1 ile yapıldı: Safari 27.2'de ve iOS 27.2'de yeniden açılıyor, sayfası okunduğu dili söylüyor, MCP sunucusu da her ziyaretçiye bir sınır içinde cevap veriyor.

### Değişenler

*   **Pantrix UI 0.1.1:** kütüphaneyi kuran satırlar, ana sayfada ve Kurulum'da, ve kodlama asistanları için dosyalar kütüphanenin 0.1.1'ini anar; web uyumluluğu rehberi Safari 27.2'nin Compose'un Wasm sürümünü neden boş bıraktığını ve bir sayfanın WebKit'e bunun yerine JavaScript sürümünü nasıl verdiğini anlatır.

### Düzeltilenler

*   **Safari 27.2 ve iOS 27.2'deki her tarayıcı:** site yeniden açılıyor; boş kalıyordu. WebKit 27.2'nin WebAssembly derleyicisi Wasm sürümünün metin ölçümünü yanlış derliyor; bu yüzden WebKit'e, Safari'ye ve iOS'taki her tarayıcıya JavaScript sürümü verilir.
*   **Sayfanın dili:** sitede dil değiştirilince sayfa da o dilde olduğunu söyler; böylece ekran okuyucu onu o dilin sesleriyle okur, tarayıcı da öbür dilden çeviri önermez. Adresin açıldığı dili söylüyordu.
*   **Ekran okuyucunun ayırt edebildiği sekmeler:** kodlama asistanları sayfasının istemcileri ve temeller sayfasının bölümleri, görünüşlerinin söylediği gibi hangisinin seçili olduğunu da söyler.

### Güvenlik

*   **MCP sunucusunda bir sınır:** bir adres `/mcp`'ye saniyede on kez, bir kerede yirmilik bir taşmayla sorabilir; ötesine site 429 (Too Many Requests) döner, böylece tek bir ziyaretçi sunucuyu herkes için meşgul edemez.
*   **Web uygulamasında derleme makinesinin yolları yok:** sitenin Wasm'ı kendi kaynak dosyalarını deponun köküne göre adlandırır; önce onları derleyen makinedeki tam yollarıyla, o kullanıcının adı ve klasörleriyle anıyordu. Pantrix UI'ın 0.1.1'i kütüphanenin kendi dosyalarını da aynı biçimde adlandırır.

## [26.11.1] - 2026-11-01

Site, kütüphanenin ilk sürümü Pantrix UI 0.1.0 ile yapıldı; onunla yapılmış uygulamalar, ana sayfanın uyarlanabilir turu ve sunucuya soran sayfa geldi. Masaüstü uygulamasının ikinci sürümü.

### Eklenenler

*   **Referanslar:** `/references` Pantrix UI ile yapılmış uygulamaları gösterir, ilki site: ne olduğu, nerede çalıştığı, kendi ekranlarını çizen 34 kütüphane bileşeni ve nasıl yapıldığı; başka bir uygulamanın nasıl ekleneceği de, sitenin deposunda bir issue ile. Onun altında öbürlerini platforma göre süzerek listeler, ilki kütüphanenin demo kataloğu, web sürümü ve macOS uygulamasıyla. Header'ın ve menünün son bağlantısıdır; ana sayfanın altbilgisi ve ana sayfadaki bir bant da ona götürür. Masaüstünün header'ı ona yer açmak için parçalarını, en çok da aramayı, dili, GitHub'ı ve temayı birbirine daha yakın dizer.
*   **Uyarlanabilir tur:** ana sayfa bir tarif uygulamasını altı pencerede gezdirir; bir telefon, açık ve masaya konmuş bir katlanabilir cihaz, bir tablet, bir masaüstü ve bir tarayıcı sekmesi; her birini Pantrix kendi platformunun alışkanlıklarıyla çizer: gezinme çubuktan raya ve kenar çubuğuna geçer, bir tarif listesinin yanında açılır, katlanabilir cihaz düğmeleri katın altında pişirir, büyük bir pencere haftanın alışverişini tarifin yanında gösterir. Görünürken kendiliğinden ilerler, okuyan bir adım seçince durur, sistem daha az hareket isteyince hiç ilerlemez. Yemeklerin fotoğrafları Unsplash'ten ve Pexels'ten, altında fotoğrafçılarıyla.
*   **Sunucuya soran sayfa:** `/ai/ask` Pantrix UI'ın MCP sunucusuna kendisi sorar, modelsiz ve anahtarsız: İngilizce ya da Türkçe bir soru, kodu ve ondan söz eden belgeleriyle bir bileşeni, kütüphanenin bildirimlerini, ikonlarını ya da belgelerini bulur; çağırdığı araçlar cevabın yanında durur. Okurun kendi asistanını da kurar ve okurun kendi anahtarıyla neyin geleceğini söyler. Kodlama asistanlarının sayfası ona başından ve konuşmasının sonundan götürür.
*   **Sitenin kaynağı GitHub'da:** header'da tema düğmesinin yanında GitHub'ın işareti var ve sitenin deposunu açar; tasarımdan dar bir telefonda orada ona yer yoktur, menü de ona götürür.

### Değişenler

*   **Pantrix UI 0.1.0:** kütüphaneyi kuran satırlar, ana sayfada ve Kurulum'da, ve kodlama asistanları için dosyalar onun altı önizlemeden sonraki ilk sürümünü anar; o sürümün Claude Code eklentisi `pantrixui.com/mcp`'deki MCP sunucusunu da getirir, sunucusu Türkçe de arar ve cevaplarını veri olarak da verir; rehberleri sitededir, her tarayıcıda açılan uygulamanın rehberi canlı çizimiyle birlikte, web yazı tiplerinin rehberi de kod için web'e eş aralıklı bir yazı tipi verilmesini söyler.
*   **Kodlama asistanları sayfası:** Claude Code yalnızca Pantrix UI'ın eklentisiyle kurulur; eklenti MCP sunucusunu ve beceriyi birlikte getirir, sunucu artık iki kez eklenmez.
*   **Kodlama asistanları sayfası, dosyaları:** kurulumun altında, andığı her dosya bulunduğu yerden açılır ya da iner: MCP sunucusunun jar'ı ve becerinin zip'i GitHub'daki sürümden, her birini kullanan satırla; `SKILL.md`, `llms.txt`, `llms-full.txt`, `pantrix-ui.json` ve şeması siteden; adımlardaki ve kartlardaki adları da bağlantıdır. Cursor sunucunun adresiyle kurulur, jar komut başlatan bir istemciye kalır.
*   **Tablet ile masaüstü arasında ana sayfa:** 840 ile yaklaşık 1100 dp arası genişlikte bir pencerede, yan yatırılmış açık bir katlanabilir cihazda olduğu gibi, ana sayfanın yazıları tabletin tek sütununda genişliği doldurur; önceden sağ tarafı boş bırakıyorlardı.

### Düzeltilenler

*   **Tabletin ve telefonun menüsü:** Pantrix UI başlığı altında header'ın andığı her bölümü header'ın sırasıyla listeler; hiçbir menünün götürmediği kodlama asistanları sayfası da aralarında.
*   **Beceri ve API başvuruları tarayıcıda:** sitenin Markdown dosyaları metin olarak gösterilir; önceden tarayıcı onları dosya olarak kaydediyordu. Tek bir bileşen için ne verileceği, sitede olmayan bir dosyanın yerine bileşenin sitedeki sayfasını, `/components/data-table`'ı anar.
*   **Eski tarayıcılar ve telefonlar:** sitenin Wasm sürümünü çalıştıramayan bir tarayıcıya, 119'dan önceki Chrome ve 18.2'den önceki Safari gibi, ya da onu başlatamayan bir tarayıcıya aynı uygulamanın JavaScript ile derlenmiş sürümü verilir: önceden sayfanın düz metnini ya da beyaz bir sayfa gösteriyordu.

### Güvenlik

*   **Yalnızca HTTPS:** site, tarayıcıdan ona yalnızca HTTPS'le dönmesini ister (`Strict-Transport-Security`), başta kısa bir süre için; sayfalarının yüklediğini bir içerik güvenliği politikasına göre raporlar, onu henüz uygulamaz.
*   **Google'dan yazı tipi yok:** site, Mac tuşlarının işaretlerini, kendi işaretlerini ve Arapçasını kendi taşıdığı fontlarla çizer, Pantrix UI 0.1.0-alpha04 ile yapılır; bir ziyaretçinin tarayıcısı artık Google'dan yazı tipi istemez.
*   **Derleyen makinenin yolu yok:** sitenin betikleri artık derlendikleri makinenin yolunu, kullanıcı adı ve klasörleriyle, taşımıyor; Skia'nın kodu onu betiklere yazıyordu.

## [26.10.31] - 2026-10-31

Sitenin ilk sürümü, Pantrix UI 0.1.0-alpha03 ile yapıldı; `pantrixui.com`'da yayında. Masaüstü uygulamasının da ilk sürümü.

### Eklenenler

*   **İngilizce ve Türkçe sayfalar,** her biri kendi adresinde (`/docs/installation`, `/tr/docs/installation`):
    *   ana sayfa;
    *   bileşenler: kütüphanenin 100 bileşeninin ızgarası ve her birinin sayfası; örnekleri canlı çalışır, API'si, her platformdaki stilleri ve renkleri, erişilebilirliği, etiketleri ve değişiklikleriyle;
    *   belgeler: başlarken, canlı çizimli 24 rehber ve kütüphanenin değişiklik günlüğü;
    *   temeller, temadan canlı okunur;
    *   tema oluşturucu, bir `DesignPreset`'i Kotlin olarak yazar;
    *   uyarlanabilir arayüz simülatörü, kütüphanenin iskelelerini her boyuttaki pencerelerde çizer;
    *   kodlama asistanları sayfası, yol haritası, ⌘K araması ve sayfası olmayan bir adresin sayfası;
    *   masaüstü uygulamasının indirme sayfası; son sürümün sürüm numarası, boyutu ve SHA-256'sıyla.
*   **Her sayfa statik HTML olarak** uygulamanın yanında; arama motorları ve Wasm çalıştıramayan bir tarayıcı için: başlığı, açıklaması, asıl adresi, paylaşım görseli ve metni. Onlarla birlikte `sitemap.xml`, `robots.txt`, `llms.txt` ve `llms-full.txt`.
*   **Pantrix UI'ın MCP sunucusu** `/mcp`'de, Streamable HTTP üzerinden: bir kodlama asistanına yalnızca adresi verilir.
*   **Aynı site uygulama olarak:** Android, iOS ve masaüstü için. Bir Developer ID ile imzalı ve Apple'ın onayından geçmiş macOS uygulaması, sitenin GitHub'daki sürümlerinden indirilir.
*   **Yayına alma:** nginx'in ardındaki site ve sunucusu, tek bir Compose yığınında iki Docker imajı olarak. `deploy/deploy.sh local` onu `http://localhost:4004`'te çalıştırır; bir yığının sunulduğu adres bir ayardır, `SITE_URL`. `deploy/deploy.sh web` onu Coolify üzerinden sunucuya koyar, `deploy/deploy.sh desktop` macOS uygulamasını yayımlar, `deploy/deploy.sh release` ikisini birden yapar.
