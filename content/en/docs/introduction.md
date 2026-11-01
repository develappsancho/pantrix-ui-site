---
title: Introduction
description: Pantrix UI is a design system and an adaptive UI library for Compose Multiplatform: one Kotlin codebase for Android, iOS, the desktop and the web, built on Compose Foundation with no Material dependency.
section: Getting started
---

## What it is {#what}

:::facts
- Components: 98, in eight groups
- Targets: Android, iOS, JVM, Wasm, JS
- Dependency: Compose Foundation alone
- Languages: English, Turkish, Arabic
- Licence: Apache 2.0
:::

One theme follows each platform's own conventions: its touch targets, its focus rings, the geometry of its components. A button is 48dp tall to a finger on Android, 44pt on iOS and 32dp under a desktop's pointer, and nothing in the screen's code says so.

```kotlin App.kt
import com.pantrixui.components.elements.button.PantrixButton
import com.pantrixui.foundation.theme.PantrixTheme

@Composable
fun App() {
    PantrixTheme {
        PantrixButton(text = stringResource(Res.string.save), onClick = { /* … */ }, tagKey = "save")
    }
}
```

## What it holds {#modules}

:::table The modules
- `com.pantrixui:components`: The 98 components: elements, inputs, dates, containers, navigation, overlays, data and charts. It brings `foundation` and `annotations` with it
- `com.pantrixui:foundation`: The theme and its presets, the tokens, the window and its folds, languages, and the platform's services: the clipboard, files, shortcuts, haptics
- `com.pantrixui:testing`: For an app's tests: finding by tag, a theme that is the same on every machine, doubles of the platform's services
- `com.pantrixui:material3`: The bridge to Material 3, both ways, for an app that moves a screen at a time
- `com.pantrixui:detekt-rules`: The rules an app keeps, for its own detekt
- `com.pantrixui:mcp`: The MCP server and the documents that teach Pantrix to a coding assistant
:::

## What it keeps to {#principles}

:::rows
- **No Material**: The library depends on Compose Foundation alone. An app that keeps Material 3 bridges the two themes with `com.pantrixui:material3`.
- **Windows, not devices**: A layout reads the window it is drawn in, its size class, its posture and its folds, not the device it runs on.
- **A tag on every control**: Each control is tagged `Component_key` from its `tagKey`, the same in every language: a test and a Maestro flow find it by that.
- **Read aloud**: Each component says to a screen reader what it is, what it holds and what it does.
- **Start and end**: Every component turns round in a language read from right to left.
:::

## Where next {#next}

:::cards
- [Installation](/docs/installation): The repository, the dependency and the theme.
- [Theming](/docs/theming): Colour roles, type, shapes and a brand's preset.
- [Platforms](/docs/platforms): What each platform's entry point draws, and what differs.
- [Guides](/docs/guides): Answers to problems every app meets.
:::
