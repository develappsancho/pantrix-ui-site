---
title: Pantrix UI
description: A design system and adaptive UI library for Compose Multiplatform: {{components}} components on Compose Foundation, with no Material dependency, and one theme that follows the conventions of Android, iOS, the desktop and the web.
---

## The code of the landing page {#code}

The landing page's code, as its sections show it.

### The form, as a desktop shows it {#review-form}

```kotlin ReviewForm.kt
@Composable
fun ReviewForm(onSend: (String) -> Unit) {
    val comment = rememberTextFieldState()
    PantrixTheme(preset = PantrixPreset) {
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

### The form, as a tablet shows it {#review-form-wide}

```kotlin ReviewForm.kt
@Composable
fun ReviewForm(onSend: (String) -> Unit) {
    val comment = rememberTextFieldState()
    PantrixTheme(preset = PantrixPreset) {
        Column(Modifier.clearFocusOnTap(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            PantrixTextArea(state = comment, label = stringResource(Res.string.comment), maxLength = 280, tagKey = "comment")
            PantrixButton(text = stringResource(Res.string.send), onClick = { onSend(comment.text.toString()) }, tagKey = "send")
        }
    }
}
```

### The form, as a phone shows it {#review-form-short}

```kotlin ReviewForm.kt
val comment = rememberTextFieldState()
PantrixTheme(preset = PantrixPreset) {
    PantrixTextArea(
        state = comment,
        label = stringResource(Res.string.comment),
        maxLength = 280,
        tagKey = "comment"
    )
}
```

### The preset {#preset}

```kotlin PantrixPreset.kt
object PantrixPreset : DesignPreset {
    override val name: String = "Pantrix"

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
