---
name: pantrix-ui
description: Builds Compose Multiplatform screens with Pantrix UI, a design system with no Material dependency, for Android, iOS, the desktop and the web. Finds the component a screen needs, its parameters and examples that compile with their imports, and keeps to the library's rules for the theme, automation tags, images, layout direction, windows and UI tests. Covers setting up an app, theming and a brand's preset, adaptive layouts and foldables, forms, overlays, charts, languages and platform services, accessibility, tests, and apps that keep Material 3. Use when writing, reviewing or testing Kotlin code that uses com.pantrixui, PantrixTheme or a Pantrix… composable.
---

# Pantrix UI

Pantrix UI is a design system and adaptive UI library for Compose Multiplatform: one Kotlin codebase for Android, iOS,
the desktop (JVM) and the web (Wasm and JS), built on Compose Foundation with no Material dependency. One theme
follows each platform's own conventions: its touch targets, its focus rings, its component geometry.

An app depends on `com.pantrixui:components`, which brings `com.pantrixui:foundation` and `com.pantrixui:annotations`
with it. Its tests use `com.pantrixui:testing`; an app that keeps Material 3 bridges the two themes with
`com.pantrixui:material3`.

## Where to look things up

Every name, parameter and example of the library is in its documents; nothing about it needs to be guessed.

- **With the `pantrix-ui` MCP server:** `search_components` finds a component by what it is for, `get_component` gives
  its page with examples that compile, `get_component_metadata` its parameters, `get_api` any declaration,
  `get_example` one example's code with its imports, and `get_rules` these rules.
- **Without it:** the same documents as files, which the server's jar writes with `docs <folder>`: `llms.txt` is their
  index, `pages/<key>.md` a page with its examples, `api/<module>.md` a module's whole API, and `llms-full.txt` all of
  them in one file.
- **In this skill:** [references/components.md](references/components.md) lists every component with its import, what
  it is for, the words it is also known by and its automation tags, and the references below say how the parts fit.

## Core rules

1. **No Material.** Write Pantrix's components (`PantrixButton`, `PantrixText`, `PantrixTextField`…), never Material's
   (`Button`, `Text`, `TextField`), and wrap the app in `PantrixTheme`, not `MaterialTheme`. The library has no
   Material dependency; an app that also keeps Material 3 bridges the two themes with `com.pantrixui:material3`.
2. **Text through Pantrix.** Text is drawn by `PantrixText` and typed into Pantrix's text fields, never by
   `BasicText`, `BasicTextField` or `BasicSecureTextField`: those miss the theme's type, its colours and what a screen
   reader is told.
3. **Values from the theme.** Colours come from `PantrixTheme.colors`, type from `PantrixTheme.typography`, gaps from
   `PantrixTheme.spacing` and corners from `PantrixTheme.shapes`, not from a `Color(0xFF…)` or a `dp` written into a
   screen. A component's own look changes through its `style` and `colors` parameters.
4. **Every control a `tagKey`.** A button, a field, a switch, a select gets a `tagKey`, the same in every language:
   `PantrixButton(tagKey = "save")` is tagged `Button_save`, and inside `TestScope("Checkout") { … }` it is
   `Checkout.Button_save`. A control bound to a form's field takes the field's `id` instead. A tag is never built from
   visible text.
5. **An image says what it shows** with its `contentDescription`, or says it is decoration with
   `contentDescription = null`; a description is never empty. An icon button's `contentDescription` names its action.
6. **Start and end, not left and right.** Lay out with `padding(start = …)` and `Alignment.Start`, never with
   `absolutePadding`, `absoluteOffset`, `AbsoluteAlignment` or `Arrangement.Absolute`, so that a screen turns round in
   a language read from right to left.
7. **Imports from the documents.** Every component lives in a package of its own:
   `com.pantrixui.components.elements.button.PantrixButton`. Take the imports the documents give with each example
   rather than guessing them.
8. **Parameters from the documents.** Check a component's parameters, what each takes and its default before calling
   it (`get_component_metadata`, or the API of its page), and give arguments by name. Never call a component with a
   parameter the documents do not list.
9. **Words from resources.** What an app shows comes from its resources (`stringResource(Res.string.save)`), as the
   components' short examples do. Pantrix's components say their own words (`Close`, `Loading`) in the theme's
   language by themselves.
10. **Windows, not devices.** A layout reads the window it is drawn in (`PantrixTheme.window`), not the device it runs
    on. The adaptive scaffolds `PantrixListDetailScaffold`, `PantrixSupportingPaneScaffold` and `PantrixTwoPane`
    already arrange their panes by the window's size and its folds.
11. **Tests on Pantrix's helpers.** A UI test draws the screen in `PantrixTestTheme`, which is the same on every
    machine, and finds a component by its tag: `onPantrixNode("Button", "save")`, from `com.pantrixui:testing`.

## Material's names, Pantrix's components

| Material | Pantrix |
| :--- | :--- |
| `MaterialTheme`, `MaterialTheme.colorScheme.primary` | `PantrixTheme`, `PantrixTheme.colors.primary.color` |
| `Scaffold` | `PantrixScaffold` |
| A screen's own `Scaffold` with its `TopAppBar` | `PantrixPageScaffold`, inside the app's one `PantrixScaffold` |
| `TopAppBar`, `CenterAlignedTopAppBar` | `PantrixTopBar` (centred on iOS by itself) |
| `NavigationBar`, `NavigationRail` | `PantrixNavigationBar`, `PantrixNavigationRail` |
| `PermanentNavigationDrawer` | `PantrixSidebar`; a narrow window takes a `PantrixNavigationBar`, not a modal drawer |
| `Button`, `OutlinedButton`, `TextButton`, `FilledTonalButton` | `PantrixButton` with `variant = ButtonVariant.Solid`, `Outline`, `Text`, `Soft` |
| `IconButton` | `PantrixIconButton` |
| `Text`, `Icon`, `Image` | `PantrixText`, `PantrixIcon`, `PantrixImage` |
| `OutlinedTextField`, `TextField` | `PantrixTextField`, `PantrixFilledTextField` |
| `OutlinedTextField`, `TextField` of several lines (`minLines`) | `PantrixTextArea`, `PantrixFilledTextArea` |
| `Checkbox`, `TriStateCheckbox`, `RadioButton`, `Switch` | `PantrixCheckbox`, `PantrixTriStateCheckbox`, `PantrixRadioButton` (in a group, `PantrixRadioGroup`), `PantrixSwitchButton` |
| `SegmentedButton` | `PantrixSegmentButton` |
| `ExposedDropdownMenuBox` | `PantrixSelect`, `PantrixMultiSelect` |
| `DropdownMenu` | `PantrixMenu` |
| `AlertDialog`, `ModalBottomSheet` | `PantrixDialog`, `PantrixSheet` |
| `Dialog` with content of the app's own | `PantrixModal` |
| `ListItem` | `PantrixListItem` |
| `Snackbar`, `SnackbarHost` | `PantrixSnackbarState.show`, `PantrixSnackbarHost` |
| `TooltipBox` | `PantrixTooltip` |
| `TabRow` | `PantrixTabs` |
| `Card`, `Surface`, `HorizontalDivider` | `PantrixCard`, `PantrixSurface`, `PantrixHorizontalDivider` |
| `AssistChip`, `FilterChip`, `InputChip` | `PantrixChip` |
| `Badge`, `BadgedBox` | `PantrixBadge`, `PantrixBadgedBox` |
| `CircularProgressIndicator` | `PantrixSpinner`, and `PantrixProgressCircle` for a known amount |
| `DatePicker`, `TimePicker` | `PantrixDatePicker`, `PantrixTimePicker`; in a field, `PantrixDateTextField`, `PantrixTimeTextField` |

Pantrix has no slider, no linear progress bar, no floating action button and no search bar. A search
field is a `PantrixTextField` with a search icon, and a known amount of work is a `PantrixProgressCircle`. The rest a
screen builds on Compose Foundation with the theme's values and the helpers of
[references/accessibility.md](references/accessibility.md); an app that keeps Material 3 draws Material's inside
`MaterialThemeFromPantrix`.

## Building a screen

1. **Choose the components.** `search_components` with what each part of the screen is for, or
   [references/components.md](references/components.md). A Material name is looked up in the table above.
2. **Read each one** before calling it: its parameters (`get_component_metadata`) and an example of the same use
   (`get_component`, `get_example`).
3. **Write the screen** as a composable drawn inside the app's `PantrixTheme`: the examples' imports, arguments by
   name, values from the theme, a `tagKey` on every control, the words from the app's resources.
4. **Hold the state in the screen.** What is selected, what is open, which overlay is `visible`: the components report
   a change and the screen keeps it. What has to outlive an Android activity is kept with `rememberSaveable`.
5. **Lay it out by the window,** and a component by its own room: inside the app's shell a screen is a
   `PantrixPageScaffold`, and outside it, signing in, a `PantrixShowcaseScaffold`
   ([references/layout.md](references/layout.md)).
6. **Check every name** against the documents: `get_api` looks any of them up.
7. **Test it** with `com.pantrixui:testing` ([references/testing.md](references/testing.md)).

## Reviewing code

- Read it against the core rules in order. Four of them an app's detekt can keep with `com.pantrixui:detekt-rules`:
  `NoMaterialImport`, `UntaggedControl`, `ImageWithoutDescription` and `FixedSideLayout`
  ([references/setup.md](references/setup.md)).
- An overlay is kept in the tree and shown by `visible`; a component in a form is bound by its `field` and takes no
  `tagKey`; a value written in a screen (`16.dp`, `Color(0xFF…)`) is a theme's value or a style's.
- A name or a parameter that the documents do not have is a mistake, whatever compiled before: the documents are
  written from the library's sources at every build.

## References

| Reference | Read it for |
| :--- | :--- |
| [components.md](references/components.md) | Every component by its group: its import, what it is for, its other names, its tags |
| [setup.md](references/setup.md) | The repository and the dependencies, the theme at the root, each platform's entry point, the app's detekt |
| [theming.md](references/theming.md) | Reading the theme, colours and type, a brand's `DesignPreset`, light and dark, component styles, a part in another theme |
| [layout.md](references/layout.md) | The scaffold and its bars, a screen's page, navigation by width, window classes, panes, foldables |
| [forms.md](references/forms.md) | `FormState`, fields and rules, submitting, what binds to a field, text fields, text areas, dates and times |
| [overlays.md](references/overlays.md) | Which overlay a task takes, the contract they share, snackbars |
| [charts.md](references/charts.md) | Which chart a set of data takes, series from the app's rows, what every chart shares |
| [platform.md](references/platform.md) | Language and direction, numbers and dates, the platform and the system, haptics, clipboard, files, shortcuts, analytics, diagnostics |
| [accessibility.md](references/accessibility.md) | What the components do by themselves, what a screen owes, a control of the app's own |
| [testing.md](references/testing.md) | Automation tags, a test, the test theme and its doubles, each platform, Maestro |
| [material3.md](references/material3.md) | An app that keeps Material 3: the bridge both ways, mixed screens, conversions |
