---
title: Kodlama asistanları için
description: Bir kodlama asistanının Pantrix'i nasıl okuduğu: bir MCP sunucusu, bir beceri, llms.txt ve JSON olarak kütüphane.
---

## Sayfanın kodu {#code}

Sayfadaki sohbetin sonunda yazılan.

### Asistanın yazdığı {#answer}

```kotlin
import com.pantrixui.components.inputs.textarea.PantrixTextArea

val comment = rememberTextFieldState()
PantrixTextArea(
    state = comment,
    label = stringResource(Res.string.order_comment),
    maxLength = 280,
    tagKey = "orderComment"
)
```

### Tablette {#answer-tablet}

```kotlin
PantrixTextArea(state = comment, label = stringResource(Res.string.order_comment),
    maxLength = 280, tagKey = "orderComment")
```

### Telefonda {#answer-phone}

```kotlin
PantrixTextArea(
    state = comment,
    label = stringResource(Res.string.order_comment),
    maxLength = 280,
    tagKey = "orderComment"
)
```
