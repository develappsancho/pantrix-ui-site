# Değişiklik Günlüğü

English: [CHANGELOG.md](./CHANGELOG.md)

Pantrix UI'ın sitesinde, `pantrixui.com`'da, ve uygulamalarında nelerin değiştiği; onu okuyan ya da derleyen biri için.

*   **Biçim:** bu dosya [Keep a Changelog](https://keepachangelog.com/en/1.1.0/)'u izler. Bir sürüm, bir ziyaretçinin gördüğünü değiştiren bir yayındır ve tarihiyle, üç sayı olarak adlandırılır: `YY.A.G` (`26.11.2`); masaüstü uygulaması da bu sürümü taşır.
*   **Hangi değişiklik satır alır:** hangilerinin aldığını ve satırın nasıl yazıldığını [CONTRIBUTING.tr.md](./CONTRIBUTING.tr.md#değişiklik-günlüğü) anlatır.

## [Yayınlanmamış]

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
