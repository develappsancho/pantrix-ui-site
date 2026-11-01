# Güvenlik Politikası

English: [SECURITY.md](./SECURITY.md)

## Desteklenen Sürümler

`pantrixui.com`'da çalıştığı hâliyle site ve yayına alındığı ana dal. Daha önceki bir yayın kendine ait bir düzeltme almaz: düzeltmeyi bir sonraki yayın getirir.

## Bir Açığı Bildirmek

Bir açığı issue, pull request ya da tartışma olarak bildirmeyin: onları herkes okuyabilir.

Açığı GitHub'ın özel güvenlik açığı bildirimiyle bildirin: deponun **Security** sekmesi, ardından **Report a vulnerability**. Bu size açık değilse doğrudan proje sahibine, [@developersancho](https://github.com/developersancho)'ya bildirin.

Bir bildirim şunları söyler:

*   adresi ya da commit'i ve açığın görüldüğü tarayıcıyı, uygulamayı ya da platformu;
*   bir saldırganın ne yapabileceğini ve bunun için neye ihtiyacı olduğunu;
*   açığı gösteren adımları; varsa bir kavram kanıtıyla.

## Sonra Ne Olur

*   **5 iş günü içinde:** bildirimin ulaştığına dair bir cevap.
*   **30 gün içinde:** bir değerlendirme ve bir plan. Bunların bir açık olup olmadığını, ne kadar ciddi olduğunu ve bir düzeltmenin ne zaman gelebileceğini söyler.
*   **Düzeltme:** bir yayınla gelir. Açık ardından değişiklik günlüğünün `Güvenlik` bölümünde ve bir GitHub güvenlik bildiriminde anlatılır.
*   **Teşekkür:** bildiren, isterse, bulduğu açıkla birlikte adıyla anılır.

Lütfen düzeltme yayınlanana kadar açığı kendinize saklayın.

## Kapsamdakiler

*   **`/mcp`'deki sunucu.** Beklemediği bir mesaj, büyük, bozuk ya da başka bir sitenin sayfasından gönderilmiş, reddedilir: sunucu bu yüzden çökmemeli ya da donmamalı ve yalnızca kütüphanenin belgelerinden cevap verir.
*   **nginx'in ayarları.** Yönlendirmeleri ve başlıkları ve neyi sunduğu: sitenin kendi klasörünün dışındaki bir dosyayı asla.
*   **Sayfalar,** uygulamada ve statik HTML'lerinde: bir sayfa sitenin yazmadığı hiçbir betiği çalıştırmamalı.
*   **Yayına alma.** İmajlar ve onları yapan betikler: bir sır, örneğin bir token, asla bir imaja ya da bir günlüğe girmez.
*   **Uygulamalar:** Android, iOS ve masaüstü için.

## Kapsam Dışındakiler

*   **Pantrix UI'ın kendisi.** Kütüphanedeki bir açığı aynı proje sahibine, kütüphanenin olduğunu söyleyerek bildirin.
*   **Bir bağımlılıktaki açık.** O projeye bildirin. Site onu açığa çıkarıyorsa burada kapsamdadır.
*   **Sunucunun kendi kurulumu:** sitenin çalıştığı makine, Coolify ve onun vekili. Coolify'daki ya da Traefik'teki bir açığı o projelere bildirin.
