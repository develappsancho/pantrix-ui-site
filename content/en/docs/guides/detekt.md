---
title: detekt rules in your app
description: What detekt checks in the project, where it runs, the project's own rules, how a finding is dealt with, and how an app that uses Pantrix UI turns these rules on.
---

## 1. What is Detekt? {#what-is-detekt}

Detekt is a static code analysis tool for Kotlin. Kotlinter looks at how code looks; detekt looks at what code does and how it is built: a function that is too complex, code that is not needed, a place where a rule is broken. Two kinds of rules run in this project:

*   **detekt's own rules:** complexity, naming, likely bugs, style.
*   **The project's own rule set, `PantrixUIRule`:** in the module `tooling/detekt-rules`. The machine's form of the library's rules in CONTRIBUTING.md, and of the rules of accessibility, language and tokens.

:::sketch detekt
:::

## 2. Where does it run? {#where-does-it-run}

:::table
| Where | Configuration | What runs |
| :--- | :--- | :--- |
| The library's modules (`ui/foundation`, `ui/components`, `ui/annotations`, `ui/testing`, `ui/material3`) | [`config/detekt/detekt.yml`](/docs/detekt-settings#detekt-yml) | detekt's rules and the project's rules for the library. Every finding stops the build. |
| The demo (`app/*`) | [`config/detekt/demo.yml`](/docs/detekt-settings#demo-yml) | Only the four rules of the project that an app keeps too: `NoMaterialImport`, `FixedSideLayout`, `ImageWithoutDescription`, `UntaggedControl`. The last two report but do not stop the build. |
:::

Of detekt's own rules, `UndocumentedPublicClass`, `UndocumentedPublicFunction` and `UndocumentedPublicProperty` are on in the library: no class, function or property is left without KDoc. An enum's entry counts as a property too. Parameters are held by the project's `UndocumentedParameter` (section 3).

The setup is in two plugins of `build-logic`: `DetektSetupPlugin`, which `pantrix.codequality` applies, for the library, and `pantrix.demo.detekt` for the demo. Tests are outside the project's rules.

Running it:

*   **`./gradlew detekt`:** in every module, runs each target's own task. In Kotlin Multiplatform the analysis is made target by target (`detektMetadataCommonMain`, `detektJvmMain`, `detektAndroidMain`, `detektIosArm64Main`…); the plain `detekt` task is wired to all of them, or it would pass green without looking at anything.
*   **`./gradlew :ui:components:detekt`:** in a single module.
*   **After the rules change** (`tooling/detekt-rules`): `./gradlew detekt --no-daemon`. A daemon that is running keeps using the old rule classes. `./gradlew --stop` is not used: it stops the IDE's daemon too.

The reports are in each module's `build/reports/detekt/` folder: HTML (for reading) and SARIF (for tools).

## 3. The project's rules {#the-project-s-rules}

:::table
| Rule | What it holds | Where it is on |
| :--- | :--- | :--- |
| `NoMaterialImport` | There is no Material in the library: no import of Material, and no name of Material written in full | The library (but the bridge to Material 3, `ui/material3`), the demo |
| `NoFoundationPrimitive` | Text goes through Pantrix's wrappers: `BasicText`, `BasicTextField` and `BasicSecureTextField` only in the wrappers' own files | The library |
| `UnmarkedComponent` | Every public `Pantrix…` composable is either a component (`@PantrixComponent` and two `@PantrixExample`s) or a part of a component (`@PantrixComponentPart`) | `ui/components` |
| `SettingInStrings` | `PantrixStrings` carries words only: a setting is a parameter of the component or of the locale | The library |
| `TagFromVisibleText` | An automation tag and its key are not built from visible text | The library |
| `RawTestTag` | A component puts its tag on with `Modifier.pantrixTestTag`, which keeps to the tag policy and the test scope, not with `Modifier.testTag` | The library |
| `EmptyComposableDefault` | A composable lambda's default is `null`, not an empty lambda; an empty lambda adds a public `ComposableSingletons` class to the ABI | The library |
| `KDocNotASentence` | A KDoc's summary is a sentence: it starts with a capital and ends with a full stop (names written in lower case, such as `iOS`, may come first) | The library |
| `UndocumentedParameter` | Every parameter of a public function or constructor is described: by a link `[name]` in the KDoc's text, by a `@param` or `@property` tag, or by the parameter's own KDoc. A declaration with no KDoc at all is left to detekt's `UndocumentedPublicFunction` and `UndocumentedPublicClass`. An override's description (a constructor's `override val` too) is in what it overrides, and an `actual`'s in its `expect` | The library |
| `LiteralTextInLibrary` | What a component says comes from `PantrixStrings`; no fixed text goes to the screen or to a screen reader. Names given to tools, such as an animation's label and an inspector's name, do not count | The library (but its packs of words) |
| `FixedSideLayout` | Layout follows the reading direction: start and end in place of `absolutePadding`, `absoluteOffset`, `AbsoluteAlignment`, `Arrangement.Absolute` | The library, the demo |
| `SizeOutsideStyle` | A component's size is in its style or in a named constant; no `12.dp` is written in the code. 0 and 1 are free | `ui/components` (but the style files) |
| `ColorOutsideTokens` | A component's colour comes from the theme or from its own colours; no `Color(0xFF…)` is written | `ui/components` (but the style files) |
| `ImageWithoutDescription` | An image says what it shows, or says it is decoration with `contentDescription = null`; an empty description is not given | The demo (reports) |
| `UntaggedControl` | A control carries a `tagKey` (or is bound to a form's field, or puts its tag on in its `modifier` itself). A chip given `onClick = null` that shows only a word, and a list's row given no `onClick` at all, which is only read, do not count as controls | The demo (reports) |
:::

Where the rules can be configured (`allowedPathPattern`, `includes`, `excludes`, lists of names) is written in each rule's KDoc.

## 4. Dealing with a finding {#dealing-with-a-finding}

A finding is closed in one of two ways:

*   **It is fixed.** This is the answer to most findings.
*   **It is suppressed where it stands, with its reason.** Where a rule is broken on purpose, the suppression goes in the narrowest place (a constant, a small function) and its reason is written on the same line:

```kotlin
@Suppress("FixedSideLayout") // A JSON document is laid out from the left in every language.
private val COPY_CORNER: Alignment = AbsoluteAlignment.TopRight
```

**No baseline is used.** A baseline file puts the finding where nobody looks; while the finding is there, the rule seems to be kept.

## 5. Pantrix's rules in your own app {#pantrix-s-rules-in-your-own-app}

The rules are published as `com.pantrixui:detekt-rules`. An app gives them to detekt as a plugin:

```kotlin
dependencies {
    detektPlugins("com.pantrixui:detekt-rules:0.1.0-alpha03-SNAPSHOT")
}
```

Where detekt is built upon its own default configuration (`buildUponDefaultConfig = true`), the rule set's own configuration turns three rules on: `FixedSideLayout`, `ImageWithoutDescription`, `UntaggedControl`. The rules of the library alone start off; an app turns them on in its own configuration if it wants to. The recommended configuration, with the last two reporting and not stopping the build:

```yaml
build:
  weights:
    UntaggedControl: 0
    ImageWithoutDescription: 0

PantrixUIRule:
  # If the app uses no Material:
  NoMaterialImport:
    active: true
```

The rule classes are compiled for JVM 17: the JDK that runs detekt has to be 17 or newer.

## 6. Detekt and Kotlinter {#detekt-and-kotlinter}

:::table
| | Detekt | Kotlinter |
| :--- | :--- | :--- |
| **What it looks at** | The code's quality and its rules | The code's form |
| **A finding, for example** | "This size belongs in the component's style" | "A space is missing here" |
| **The answer** | Fixing the code, or an exception with its reason | `./gradlew formatKotlin` |
:::
