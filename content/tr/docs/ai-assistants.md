---
title: Yapay zekâ asistanları için
description: Pantrix, kodlama asistanları için kendi belgelerini her derlemede kendi kaynaklarından yazar: bir MCP sunucusu, bir beceri, llms.txt ve bütün kütüphane JSON olarak. Bir asistan ihtiyacı olanı sorar ve Material'dan bir tahmini değil, kütüphanenin bugünkü hâlini alır.
section: Başlarken
---

## Bir asistana verilenler {#documents}

:::table Belgeler
- `{{site}}/mcp`: Bu sitenin HTTP üzerinden sunduğu MCP sunucusu: bir asistana yalnızca adresi verilir
- `mcp-0.1.0-alpha05-all.jar`: Aynı sunucu, kendi başına çalışan tek bir jar olarak; her sürüme ekli, kendi makinen için
- `pantrix-ui` becerisi: Pantrix ile yazılan kodun uyduğu kurallar, Material'ın adları ve Pantrix'inkiler, parçaların birbirine nasıl oturduğu
- `llms.txt`, `llms-full.txt`: Belgelerin dizini ve hepsi tek bir dosyada, bu sitenin kökünde
- `pantrix-ui.json`: Veri olarak kütüphane: her bileşen, parametreleri, örnekleri ve etiketleri
:::

## Kurmak {#setup}

:::steps
## Sunucuyu asistana ver {#server toc="Sunucu"}

Sunucu `{{site}}/mcp` adresinde cevap verir; asistana adresinden başkası gerekmez.

:::tabs
### Claude Code

```bash
claude mcp add -t http pantrix-ui {{site}}/mcp
```

### Bir projenin .mcp.json'u

```json .mcp.json
{
  "mcpServers": {
    "pantrix-ui": {
      "type": "http",
      "url": "{{site}}/mcp"
    }
  }
}
```
:::

## Beceriyi kur {#skill toc="Beceri" optional}

Beceri ne yapılacağını söyler; sunucu kütüphanenin bugün ne olduğunu söyler. İkisi birlikte en iyisidir. Claude Code'da beceriyi, sunucuyu da bu sitenin adresiyle, Pantrix UI'ın eklentisi getirir, kütüphanenin dağıtıldığı depodan: onunla yukarıdaki adım gerekmez.

```bash
claude plugin marketplace add develappsancho/pantrix-ui-cmp-package
claude plugin install pantrix-ui@pantrix-ui
```

## Ya da sunucuyu kendi makinende çalıştır {#jar toc="Jar" optional}

Jar [her sürüme](https://github.com/develappsancho/pantrix-ui-cmp-package/releases) eklidir; kütüphanenin deposunda `./gradlew publishToMavenLocal`'dan sonra yerel Maven deposunda da durur: `~/.m2/repository/com/pantrixui/mcp/<sürüm>/`. JDK 17 ya da üstünde çalışır. Beceriyi bir projeye de yazar; ekip onu git'ten alır, becerileri okuyan her asistan da kullanır:

```bash
curl -fL --create-dirs -o ~/.pantrix/mcp-0.1.0-alpha05-all.jar https://github.com/develappsancho/pantrix-ui-cmp-package/releases/download/v0.1.0-alpha05/mcp-0.1.0-alpha05-all.jar
claude mcp add pantrix-ui -- java -jar ~/.pantrix/mcp-0.1.0-alpha05-all.jar
java -jar ~/.pantrix/mcp-0.1.0-alpha05-all.jar skill .claude/skills
```
:::

## Dosyalar nerede {#files}

:::table Nerede bulunur
| Ne | Nerede |
| :--- | :--- |
| MCP sunucusu | `{{site}}/mcp`, [sürüme](https://github.com/develappsancho/pantrix-ui-cmp-package/releases/tag/v0.1.0-alpha05) ekli `mcp-0.1.0-alpha05-all.jar` ya da yerel yayından sonra yerel Maven deposu |
| Beceri | Claude Code'un `pantrix-ui@pantrix-ui` eklentisi, sürüme ekli `pantrix-ui-skill-0.1.0-alpha05.zip` ya da jar'ın `skill` komutu |
| `llms.txt` | [{{site}}/llms.txt]({{site}}/llms.txt): bağlantıları bu sitenin sayfalarına gider, sayfası olmayanlar `/llms/` altındadır |
| Bütün belgeler, kendi makinende | `java -jar mcp-0.1.0-alpha05-all.jar docs <klasör>` |
:::

## Neleri sorabilir {#tools}

:::table Sunucunun araçları
- `search_components`, `get_component`: Ne için olduğuna göre bir bileşen ve derlenen örnekleriyle sayfası
- `get_component_metadata`: İçe aktarması, yayın adı, etiketleri ve her parametresi
- `search_api`, `get_api`: Adıyla ya da yaptığıyla herhangi bir bildirim
- `get_example`, `list_examples`: Bir örneğin içe aktarmalarıyla kodu
- `search_documentation`, `get_documentation_page`: Sayfalar, rehberler ve becerinin referansları
- `get_changelog`: Sürüm sürüm, neyin değiştiği
- `get_rules`: Becerinin kendisi, onu kurmamış bir asistan için
:::

:::callout
**Jar JDK 17 ya da üstünü ister; adres hiçbir şey istemez.** İstemci jar'ı kendisi başlatır ve onunla stdio üzerinden konuşur; standart çıktısına protokolden başka bir şey yazılmaz. HTTP üzerinden her istek kendi başına, oturumsuz cevaplanır.
:::

## Sırada ne var {#next}

:::cards
- [Pantrix'i bir asistana öğretmek](/docs/guides/ai): Belgelerin nasıl yazıldığı ve derlemeyi neyin durdurduğu.
- [Kurulum](/docs/installation): Kütüphanenin kendi bağımlılık satırları.
- [Uygulamanızda detekt kuralları](/docs/guides/detekt): Asistandan sonra bir makinenin tuttuğu kurallar.
- [Değişiklik günlüğü](/changelog): `get_changelog`'un verdiği.
:::
