---
title: Pantrix UI
description: Compose Multiplatform için bir tasarım sistemi ve uyarlanabilir arayüz kütüphanesi: Compose Foundation üstünde 98 bileşen, Material'a hiçbir bağımlılık olmadan, ve Android'in, iOS'un, masaüstünün ve web'in alışkanlıklarını izleyen tek bir tema.
---

## Ana sayfanın kodu {#code}

Ana sayfanın kodu, bölümlerinin gösterdiği gibi.

### Form, masaüstünde gösterildiği gibi {#review-form}

```kotlin ReviewForm.kt
@Composable
fun ReviewForm(onSend: (String) -> Unit) {
    val comment = rememberTextFieldState()
    PantrixTheme(preset = ShopeersPreset) {
        Column(Modifier.clearFocusOnTap(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            PantrixTextArea(
                state = comment,
                label = stringResource(Res.string.comment),
                maxLength = 280,
                tagKey = "comment"
            )
            PantrixButton(
                text = stringResource(Res.string.send),
                onClick = { onSend(comment.text.toString()) },
                tagKey = "send"
            )
        }
    }
}
```

### Form, tablette gösterildiği gibi {#review-form-wide}

```kotlin ReviewForm.kt
@Composable
fun ReviewForm(onSend: (String) -> Unit) {
    val comment = rememberTextFieldState()
    PantrixTheme(preset = ShopeersPreset) {
        Column(Modifier.clearFocusOnTap(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            PantrixTextArea(state = comment, label = stringResource(Res.string.comment), maxLength = 280, tagKey = "comment")
            PantrixButton(text = stringResource(Res.string.send), onClick = { onSend(comment.text.toString()) }, tagKey = "send")
        }
    }
}
```

### Form, telefonda gösterildiği gibi {#review-form-short}

```kotlin ReviewForm.kt
val comment = rememberTextFieldState()
PantrixTheme(preset = ShopeersPreset) {
    PantrixTextArea(
        state = comment,
        label = stringResource(Res.string.comment),
        maxLength = 280,
        tagKey = "comment"
    )
}
```

### Hazır ayar {#preset}

```kotlin ShopeersPreset.kt
object ShopeersPreset : DesignPreset {
    override val name: String = "Shopeers"

    override fun colors(dark: Boolean): PantrixColors = if (dark) Dark else Light

    @Composable
    override fun typography(): PantrixTypography =
        rememberPantrixTypography(SiteTypefaces.Sans, codeTypeface = SiteTypefaces.Mono)

    @Composable
    override fun shapes(): PantrixShapes = Shapes

    private val Light = PantrixColors(
        primary = PantrixColor(
            color = Color(0xFF2264FF),
            onColor = Color.White,
            container = Color(0xFFECF3FE),
            onContainer = Color(0xFF1A52D6)
        ),
        // The other roles, the grounds, the text and the lines, each a colour of the brand.
    )
}
```
