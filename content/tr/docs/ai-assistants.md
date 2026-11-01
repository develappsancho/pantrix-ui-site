---
title: Yapay zekâ asistanları için
description: Pantrix, kodlama asistanları için kendi belgelerini her derlemede kendi kaynaklarından yazar: bir MCP sunucusu, bir beceri, llms.txt ve bütün kütüphane JSON olarak. Bir asistan ihtiyacı olanı sorar ve Material'dan bir tahmini değil, kütüphanenin bugünkü hâlini alır.
section: Başlarken
---

## Bir asistana verilenler {#documents}

:::table Belgeler
- `com.pantrixui:mcp`: MCP sunucusu: kendi başına çalışan ve bir asistanın sorularını stdio üzerinden yanıtlayan tek bir jar
- `pantrix-ui` becerisi: Pantrix ile yazılan kodun uyduğu kurallar, Material'ın adları ve Pantrix'inkiler, parçaların birbirine nasıl oturduğu
- `llms.txt`, `llms-full.txt`: Belgelerin dizini ve hepsi tek bir dosyada
- `pantrix-ui.json`: Veri olarak kütüphane: her bileşen, parametreleri, örnekleri ve etiketleri
:::

## Kurmak {#setup}

:::steps
## Kütüphaneyi ve sunucuyu yayımla {#publish toc="Sunucu"}

Sunucunun jar'ı kütüphaneyle birlikte, `all` sınıflandırıcısıyla `com.pantrixui:mcp` olarak yayımlanır. Her makine onun yerini kendi değişkeninde tutar:

```bash ~/.zshrc
export PANTRIX_MCP_JAR=$HOME/.m2/repository/com/pantrixui/mcp/0.1.0/mcp-0.1.0-all.jar
```

## Sunucuyu asistana ver {#server toc="Asistan"}

:::tabs
### Claude Code

```bash
claude mcp add pantrix-ui -- java -jar "$PANTRIX_MCP_JAR"
```

### Bir projenin .mcp.json'u

```json .mcp.json
{
  "mcpServers": {
    "pantrix-ui": {
      "command": "java",
      "args": ["-jar", "${PANTRIX_MCP_JAR}"]
    }
  }
}
```
:::

## Beceriyi kur {#skill toc="Beceri" optional}

Beceri ne yapılacağını söyler; sunucu kütüphanenin bugün ne olduğunu söyler. İkisi birlikte en iyisidir.

```bash
java -jar "$PANTRIX_MCP_JAR" skill .claude/skills
```
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
**Sunucu JDK 17 ya da üstünü ister.** İstemci jar'ı kendisi başlatır ve onunla stdio üzerinden konuşur: standart çıktısına protokolden başka bir şey yazılmaz.
:::

## Sırada ne var {#next}

:::cards
- [Pantrix'i bir asistana öğretmek](/docs/guides/ai): Belgelerin nasıl yazıldığı ve derlemeyi neyin durdurduğu.
- [Kurulum](/docs/installation): Kütüphanenin kendi bağımlılık satırları.
- [Uygulamanızda detekt kuralları](/docs/guides/detekt): Asistandan sonra bir makinenin tuttuğu kurallar.
- [Değişiklik günlüğü](/changelog): `get_changelog`'un verdiği.
:::
