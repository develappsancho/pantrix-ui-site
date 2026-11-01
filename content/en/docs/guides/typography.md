---
title: Typography
description: Pantrix's type scale, what text given no style is set in, and the size of what is typed into a text field.
---

## 1. The scale {#the-scale}

`PantrixTypography` holds nine roles. Sizes and line heights are in sp, and grow with the system's text size.

:::table
| Role | Size / line | Weight | Its job |
| :--- | :--- | :--- | :--- |
| `display` | 36 / 42 | Bold | A page's title |
| `headline` | 28 / 34 | Bold | A section's title |
| `title` | 22 / 28 | SemiBold | The title of a card, a panel, a dialog |
| `callout` | 18 / 26 | Medium | Text that stands out |
| `body` | 16 / 24 | Medium | A paragraph; what is typed into a text field on Android and iOS |
| `label` | 14 / 20 | Medium | A control's label; what is typed into a text field on the desktop and the web; **the base text** |
| `footnote` | 12 / 16 | Medium | Supporting text, an error's explanation |
| `caption` | 11 / 14 | Medium | A timestamp, a counter |
| `code` | 14 / 20 | Medium, monospace | An identifier, a piece of code |
:::

:::sketch typography
:::

## 2. Naming it by its role: `TypeRole` {#naming-it-by-its-role-typerole}

A component's style holds no typeface; it only says which role it is set in. The typeface is the theme's:

```kotlin
val style: TextStyle = PantrixTheme.typography[TypeRole.Body]
```

`TypeRole` has a value for each role (`Display` … `Code`). Where a style's `TypeRole?` field is left empty, the
preset's holds.

## 3. The base text: `LocalPantrixTextStyle` {#the-base-text-localpantrixtextstyle}

The style of text given no style. `PantrixText`, `PantrixLink` and `PantrixAutoText` take it when they are given no
`style`.

*   `PantrixTheme` starts it at its scale's `label`. A theme inside another starts it again at its own scale's `label`.
*   A section gives a base text of its own. The fields it gives win, and the ones it does not stay those around it:

```kotlin
ProvidePantrixTextStyle(PantrixTheme.typography.body) {
    PantrixText("The paragraphs of this section are set in body.")
    ProvidePantrixTextStyle(TextStyle(fontWeight = FontWeight.SemiBold)) {
        PantrixText("Only the weight changes; the size stays body's.")
    }
}
```

*   A `style` given outright stands above the base text.
*   Outside a `PantrixTheme` the value is `TextStyle.Default`.

### Compose foundation's `BasicText` does not read this value

A `BasicText` given no style is set in `TextStyle.Default`: the system's typeface, 14sp. Material's `LocalTextStyle`,
too, is read by Material's own components alone. An app's own `BasicText` has to take the base text itself:

```kotlin
BasicText(text = words, style = LocalPantrixTextStyle.current)
```

The library itself has no `BasicText` without a style (searched on 2026-10-18): all of its text goes through
`PantrixText`.

## 4. The size of what is typed into a text field {#the-size-of-what-is-typed-into-a-text-field}

A text field's style says which role it types in with `textRole`: `TextFieldStyle.textRole` and
`FilledTextFieldStyle.textRole`.

:::table
| Platform | Role | Size |
| :--- | :--- | :--- |
| Android | `Body` | 16sp |
| iOS | `Body` | 16sp |
| Desktop | `Label` | 14sp |
| Web | `Label` | 14sp |
:::

What is set in that role:
*   What is typed and the placeholder, in every kind of field: outlined, filled, secure and self-sizing fields.
*   A filled field's label inside it, while it rests. Once it rises it is `footnote`.
*   Select's box and the search field in its list. Select's "as wide as its options" is measured in it as well.
*   The pagination's field that jumps to a page.

The label above the box (the `label` role) and the supporting text (`footnote`) do not change.

### Compared with Material 3

:::table
| Part | Material 3 | Pantrix |
| :--- | :--- | :--- |
| What is typed | `bodyLarge` (16/24, Regular) | Android and iOS: `body` (16/24, Medium); desktop and web: `label` (14/20, Medium) |
| Placeholder | `bodyLarge` | the same as what is typed |
| The label inside | `bodyLarge` at rest, `bodySmall` (12/16) once risen | from the role of what is typed to `footnote` (12/16) |
| Supporting text | `bodySmall` | `footnote` |
:::

In Material 3 these come with `OutlinedTextFieldTokens.InputFont = BodyLarge`; `MaterialTheme` also gives
`LocalTextStyle` `bodyLarge`. Pantrix's `label` matches Material 3's `labelLarge` (14/20, Medium), but M3 uses that for
the text of buttons and tabs.

### Changing it for the whole app

```kotlin
PantrixTheme(
    styles = pantrixStyles(
        TextFieldStyle.override(all = TextFieldStyle(textRole = TypeRole.Body)),
        FilledTextFieldStyle.override(all = FilledTextFieldStyle(textRole = TypeRole.Body))
    )
) { App() }
```

For a single field, `PantrixTextField(style = TextFieldStyle(textRole = TypeRole.Callout))` is enough.

## 5. Pitfalls {#pitfalls}

*   **On the desktop the label and the text are the same size.** The label above the box and what is typed are both
    14sp: they are told apart by colour and weight.
*   **The field's size does not change.** The box's height is a least height. A single-line field stays the same
    size; the lines of a multi-line field come down to 20sp on the desktop.
*   **Safari on an iPhone zooms the page in when a field set smaller than 16px takes the focus.** On an iPhone the web
    target types in `label` (14sp). Whether Compose for the web's hidden input field behind it is caught by this was
    not tried.
*   **A style is a look, not a role.** Text set in a heading's style is no heading to a screen reader: `PantrixText`'s
    `role` parameter says that.

## 6. Files {#files}

:::table
| File | What is in it |
| :--- | :--- |
| `ui/foundation/.../token/typography/PantrixTypography.kt` | The scale, `buildPantrixTypography`, `rememberPantrixTypography` |
| `ui/foundation/.../token/typography/TypeRole.kt` | `TypeRole`, `PantrixTypography[role]` |
| `ui/foundation/.../token/typography/PantrixTextStyle.kt` | `LocalPantrixTextStyle`, `ProvidePantrixTextStyle` |
| `ui/components/.../inputs/textfield/TextFieldStyle.kt` | `textRole` and its presets |
| `ui/components/.../inputs/filledtextfield/FilledTextFieldStyle.kt` | `textRole` and its presets |
| `app/shared/.../samples/TypographySamples.kt` | The demo: "The base text of a part" |
:::
