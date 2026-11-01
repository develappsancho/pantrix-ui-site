# Değişiklik Günlüğü

English: [CHANGELOG.md](./CHANGELOG.md)

Pantrix UI'ın sitesinde, `pantrixui.com`'da, ve uygulamalarında nelerin değiştiği; onu okuyan ya da derleyen biri için.

*   **Biçim:** bu dosya [Keep a Changelog](https://keepachangelog.com/en/1.1.0/)'u izler. Bir sürüm, bir ziyaretçinin gördüğünü değiştiren bir yayındır ve tarihiyle, üç sayı olarak adlandırılır: `YY.A.G` (`26.11.2`); masaüstü uygulaması da bu sürümü taşır.
*   **Hangi değişiklik satır alır:** hangilerinin aldığını ve satırın nasıl yazıldığını [CONTRIBUTING.tr.md](./CONTRIBUTING.tr.md#değişiklik-günlüğü) anlatır.

## [Yayınlanmamış]

### Güvenlik

*   **MCP sunucusunda bir sınır:** bir adres `/mcp`'ye saniyede on kez, bir kerede yirmilik bir taşmayla sorabilir; ötesine site 429 (Too Many Requests) döner, böylece tek bir ziyaretçi sunucuyu herkes için meşgul edemez.
*   **Web uygulamasında derleme makinesinin yolları yok:** sitenin Wasm'ı kendi kaynak dosyalarını deponun köküne göre adlandırır; önce onları derleyen makinedeki tam yollarıyla, o kullanıcının adı ve klasörleriyle anıyordu. Pantrix UI'dan taşıdığı yollar kütüphanenin 0.1.1'iyle gider.

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
