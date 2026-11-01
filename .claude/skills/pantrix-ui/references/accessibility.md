# Accessibility

What a screen built with Pantrix's components already has, and what the screen itself still owes a screen reader, a
keyboard and a finger. The pages `accessibility`, `text`, `image`, `icon-button` and `diagnostics` have the examples.

## What the components do by themselves

- **Touch targets.** A control reserves the platform's touch target even where it is drawn smaller: 48dp on Android,
  44pt on iOS, 32dp on a desktop and the web used with a mouse. The spacious density makes it larger; the compact
  density never smaller.
- **Focus.** A control focused by the keyboard shows a ring in `PantrixTheme.colors.focusRing`; on a platform that
  shows it for the keyboard alone, a press shows none. Under a dialog or a sheet, nothing shows the focus.
- **Names and states.** A control tells a screen reader its label, its role, and its state: checked, selected,
  expanded, disabled. The components' own words ("Close", "Loading") are in the theme's language.
- **Overlays.** A dialog's and a sheet's title is announced as it opens, and Tab stays inside them; a snackbar is
  announced without interrupting, but for the `Danger` tone.
- **Motion.** Every transition takes its duration from `PantrixTheme.motion`, which is nought while the system asks for
  less motion.
- **Reading direction.** In a language read from right to left, the components turn round by themselves.

## What the screen owes

- **An image says what it shows:** its description names what is in it,
  `PantrixImage(painter, contentDescription = stringResource(Res.string.chart_of_sales))`, or
  `contentDescription = null` says it is decoration; never an empty string. An icon beside a label is decoration.
- **An icon-only control names its action.** `PantrixIconButton(imageVector = PantrixIcons.Close, contentDescription =
  stringResource(Res.string.close_dialog), onClick = …, tagKey = "close")`. A `PantrixTooltip` around it shows the same
  words to a pointer.
- **Headings.** Text set in a heading's style is a heading only with `PantrixText(role = TextRole.Heading(level))`:
  a screen reader's user moves between headings. `TextRole.Decorative` hides a text that repeats what is next to it.
- **A chart says what it shows** with its `description`: a screen reader reads it first, then each place's values.
- **Start and end** ([SKILL.md](../SKILL.md), rule 6): `absolutePadding`, `absoluteOffset`, `AbsoluteAlignment` and
  `Arrangement.Absolute` do not turn round.
- **Colour is not the only mark.** Where a colour says something (an error, a status), a word or an icon says it too.
  The components do so; the screen's own parts follow them. A chart draws patterns as well as colours with
  `ChartStyle(patterns = true)`, and by itself where the system asks to tell things apart without colour.
- **The system's text size.** Sizes are in sp and controls grow with their text: a screen does not fix a text's height.

## A control of the app's own

An app's own control built on Compose Foundation uses the helpers the components use:

- `Modifier.minTouchTarget(PantrixTheme.platform.minTouchTarget)` reserves the touch target without changing what is
  drawn.
- `Modifier.focusRing(interactionSource, shape, color, width)` draws the ring, and `rememberFocusRingVisible
  (interactionSource)` says whether it is to be seen now.
- `Modifier.checkName(…)` and `Modifier.checkContrast(…)` bring it into the diagnostics' checks.
- `rememberPantrixTestTag(component, tagKey)` and `Modifier.pantrixTestTag(tag)` tag it by the same rule as the
  components.

## Checking

- In a debug build, `PantrixTheme(diagnostics = PantrixDiagnostics.Log)` reports a blank name, colours that do not
  read and a squeezed touch target while the screen runs ([platform.md](platform.md)).
- In a test, `assertNoFindings(findings)`, `assertHasName(name)` and `assertTouchTargetOf(platform)`
  ([testing.md](testing.md)).
- The app's detekt keeps `ImageWithoutDescription` and `FixedSideLayout` ([setup.md](setup.md)).
