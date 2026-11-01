# Değişiklik Günlüğü

English: [CHANGELOG.md](./CHANGELOG.md)

Pantrix UI'ın sitesinde, `pantrixui.com`'da, ve uygulamalarında nelerin değiştiği; onu okuyan ya da derleyen biri için.

*   **Biçim:** bu dosya [Keep a Changelog](https://keepachangelog.com/en/1.1.0/)'u izler. Bir sürüm, bir ziyaretçinin gördüğünü değiştiren bir yayındır ve tarihiyle, üç sayı olarak adlandırılır: `YY.A.G` (`26.11.2`); masaüstü uygulaması da bu sürümü taşır.
*   **Hangi değişiklik satır alır:** hangilerinin aldığını ve satırın nasıl yazıldığını [CONTRIBUTING.tr.md](./CONTRIBUTING.tr.md#değişiklik-günlüğü) anlatır.

## [Yayınlanmamış]

### Değişenler

*   **Pantrix UI 0.1.0-alpha05:** kütüphaneyi kuran satırlar, ana sayfada ve Kurulum'da, ve kodlama asistanları için dosyalar onun yeni sürümünü anar; o sürümün Claude Code eklentisi `pantrixui.com/mcp`'deki MCP sunucusunu da getirir; rehberleri sitededir, yenisi, her tarayıcıda açılan uygulama, canlı çizimiyle birlikte.
*   **Kodlama asistanları sayfası:** Claude Code yalnızca Pantrix UI'ın eklentisiyle kurulur; eklenti MCP sunucusunu ve beceriyi birlikte getirir, sunucu artık iki kez eklenmez.
*   **Tablet ile masaüstü arasında ana sayfa:** 840 ile yaklaşık 1100 dp arası genişlikte bir pencerede, yan yatırılmış açık bir katlanabilir cihazda olduğu gibi, ana sayfanın yazıları tabletin tek sütununda genişliği doldurur; önceden sağ tarafı boş bırakıyorlardı.

### Düzeltilenler

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
