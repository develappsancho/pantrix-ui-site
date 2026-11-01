---
title: For coding assistants
description: How a coding assistant reads Pantrix: an MCP server, a skill, llms.txt and the library as JSON.
---

## The page's code {#code}

What the conversation on the page ends with.

### What the assistant writes {#answer}

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

### On a tablet {#answer-tablet}

```kotlin
PantrixTextArea(state = comment, label = stringResource(Res.string.order_comment),
    maxLength = 280, tagKey = "orderComment")
```

### On a phone {#answer-phone}

```kotlin
PantrixTextArea(
    state = comment,
    label = stringResource(Res.string.order_comment),
    maxLength = 280,
    tagKey = "orderComment"
)
```
