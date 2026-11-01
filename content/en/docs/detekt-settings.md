---
title: detekt's settings
description: The two settings files of Pantrix's detekt: the library's own, and the demo's, which checks the demo as an app written with Pantrix is checked.
section: Guides
---

Pantrix checks its code with detekt and with its own rule set, `PantrixUIRule` ([the detekt guide](/docs/guides/detekt)). Two settings files hold which rule runs where: the library's modules are checked with the first, the demo with the second. An app that takes up the rules starts from the demo's.

The files are copies of the library's `config/detekt/demo.yml` and `config/detekt/detekt.yml`, with their comments in English.

## The demo's settings, as an app's {#demo-yml}

The demo is checked as an app that uses the library is: only the rules of Pantrix's that an app keeps too run, and detekt's own rules are off. `UntaggedControl` and `ImageWithoutDescription` report their findings but do not stop the build.

```yaml demo.yml
# The demo's detekt settings. The demo is checked as an app that uses the library is: only the rules of Pantrix's
# that an app keeps too run, and detekt's own rules are off. NoMaterialImport holds the rule that the demo keeps
# Material as a dependency and uses nothing of it (CONTRIBUTING.md).

build:
  # These two report, but do not stop the build: a finding of weight zero does not count towards maxIssues.
  weights:
    UntaggedControl: 0
    ImageWithoutDescription: 0

comments:
  active: false
complexity:
  active: false
coroutines:
  active: false
empty-blocks:
  active: false
exceptions:
  active: false
naming:
  active: false
performance:
  active: false
potential-bugs:
  active: false
style:
  active: false

PantrixUIRule:
  active: true
  NoMaterialImport:
    active: true
    # The Material 3 sample (samples/pantrix-material3) is a Material app: it uses Material directly.
    excludes: [ '**/samples/pantrix-material3/**' ]
  FixedSideLayout:
    active: true
  ImageWithoutDescription:
    active: true
    excludes: &tests
      - '**/commonTest/**'
      - '**/jvmTest/**'
      - '**/iosTest/**'
      - '**/jsTest/**'
      - '**/wasmJsTest/**'
      - '**/webTest/**'
      - '**/androidHostTest/**'
      - '**/androidDeviceTest/**'
  UntaggedControl:
    active: true
    excludes: *tests
```

## The library's settings {#detekt-yml}

The library's modules are checked with detekt's rules and the project's own; every finding stops the build, and a finding is either fixed or becomes an `@Suppress` with its reason written on the same line. The two rules an app keeps, `ImageWithoutDescription` and `UntaggedControl`, are off here.

```yaml detekt.yml
# Pantrix UI's detekt settings

comments:
  active: true
  UndocumentedPublicClass:
    active: true
    excludes: &notPublished
      - '**/app/**'
      - '**/test/**'
      - '**/androidTest/**'
      - '**/commonTest/**'
      - '**/jsTest/**'
      - '**/jvmTest/**'
      - '**/iosTest/**'
      - '**/wasmJsTest/**'
      - '**/androidUnitTest/**'
      - '**/androidInstrumentedTest/**'
      # The test source sets of the KMP Android library plugin (today's names of the two above).
      - '**/androidHostTest/**'
      - '**/androidDeviceTest/**'
      # The components' screenshot scenes: code the tests draw, never published.
      - '**/screenshotScenes/**'
  UndocumentedPublicFunction:
    active: true
    excludes: *notPublished
  UndocumentedPublicProperty:
    active: true
    excludes: *notPublished


naming:
  FunctionNaming:
    active: true
    # @Composable functions are in PascalCase: the official Compose API guidelines.
    # Not an exemption: the tool learning the code base's convention.
    ignoreAnnotated: [ 'Composable', 'Preview' ]
    # Test names are in snake_case (`a_theme_told_it_is_dark_is_dark`). detekt's default exclusion list does not
    # know androidHostTest; the list of the code that is never published stands in for it.
    excludes: *notPublished
  # Off. A file carries its component together with its types: PantrixText with TextRole in Text.kt.
  # The rule asks either to rename the file, which on the JVM changes the name of the file's class (PlatformKt)
  # and the ABI, or to split code that is read together into separate files. It also reported KMP actual files
  # (`Platform.jvm.kt`) wrongly.
  MatchingDeclarationName:
    active: false

style:
  # @Preview functions are ENTRY POINTS: the IDE calls them by reflection,
  # and nothing in the code calls them. "Unused" is true to the letter,
  # and wrong in what it means.
  #
  # Without this exemption previews could not be `private` and were written
  # `internal`: their visibility was chosen to get past the rule, not as a
  # design decision. With the exemption in the right place, previews go back
  # to the visibility they deserve (private).
  UnusedPrivateMember:
    active: true
    ignoreAnnotated: [ 'Preview' ]

  MagicNumber:
    active: true
    excludes:
      - '**/ui/foundation/**'
      # In a test, a number is not a detail to hide: it IS THE CLAIM ITSELF.
      # Turning the 37 of `assertEquals(37.sp, style.fontSize)` into
      # EXPECTED_FONT_SIZE hides from the reader what the test claims:
      # the opposite of what the rule means to protect.
      #
      # KEPT NARROW: *Test.kt alone. Leaving out the whole test source set
      # would also miss the real magic numbers in the test HELPERS (fixtures,
      # builders).
      - '**/*Test.kt'

complexity:
  # In a catalogue or a screen file, @Composable functions are not SEPARATE
  # RESPONSIBILITIES but THE PARTS OF ONE PAGE. The rule is against god objects;
  # a page split into its parts is the very opposite. Between one long body
  # (LongMethod) and many small parts, the rule should not punish the second,
  # which is the right one.
  TooManyFunctions:
    active: true
    ignoreAnnotatedFunctions: [ 'Composable', 'Preview' ]
    # In a test class every test is a function: one for each component in the screenshot test. detekt's default
    # exclusion list leaves the test source sets out but does not know androidHostTest; as in FunctionNaming, the
    # list of the code that is never published stands in for it.
    excludes: *notPublished

  LongParameterList:
    active: true
    # Style classes (ButtonStyle…) carry a value for each size or role: the style of a component with
    # five sizes starts with five parameters. The count of parameters is the design itself, not clutter.
    excludes: [ '**/ui/foundation/**', '**/*Style.kt' ]
    # Compose components have many parameters by their nature: content, style,
    # modifier and slots of behaviour. A threshold of 6 is the wrong measure for
    # a Composable: PantrixText's parameters (text, style, modifier, color,
    # maxLines, overflow, textAlign, …) are not too many, they are the API itself.
    ignoreAnnotated: [ 'Composable' ]
    # APIs that are not Composable also often grow with parameters that have defaults, as the snackbar's
    # show() does (message, action, duration, close button, tone, tagKey). The default thresholds (6 for a
    # function, 7 for a constructor) caught them at once; they were loosened a little. The threshold is the
    # count the rule fires at: up to 7 parameters in a function and 8 in a constructor are free.
    functionThreshold: 8
    constructorThreshold: 9

PantrixUIRule:
  # The project's own rules (tooling/detekt-rules): the library's rules of CONTRIBUTING.md, and the rules of
  # accessibility, language and tokens. In the library every finding stops the build; a finding is either fixed or
  # becomes an @Suppress with its reason written. No baseline. The tests and the demo stand outside these rules: the
  # demo's own settings are in demo.yml.
  NoMaterialImport:
    active: true
    # The Material 3 bridge (ui/material3) is the one module of the library that uses Material: its work is to
    # turn the two themes into each other. No module of the library depends on it.
    excludes: [ '**/ui/material3/src/**' ]
  NoFoundationPrimitive:
    active: true
    # The places the wrappers live are exempt: touching the primitive there is
    # the work itself: ui/foundation, the file of PantrixText, which wraps BasicText,
    # the file of PantrixTextField, which wraps BasicTextField, and the file of
    # PantrixSecureTextField, which wraps BasicSecureTextField.
    allowedPathPattern: '/ui/foundation/|/components/elements/text/Text\.kt$|/components/inputs/textfield/TextField\.kt$|/components/inputs/securetextfield/SecureTextField\.kt$'
  UnmarkedComponent:
    active: true
    includes: [ '**/ui/components/src/**' ]
    excludes: *notPublished
  SettingInStrings:
    active: true
    excludes: *notPublished
  TagFromVisibleText:
    active: true
    excludes: *notPublished
  RawTestTag:
    active: true
    excludes: *notPublished
    # The tag functions are here: pantrixTestTag, the one place that calls Compose's testTag.
    allowedPathPattern: '/foundation/testing/'
  EmptyComposableDefault:
    active: true
    excludes: *notPublished
  KDocNotASentence:
    active: true
    excludes: *notPublished
  UndocumentedParameter:
    active: true
    excludes: *notPublished
  LiteralTextInLibrary:
    active: true
    # The code that is never published, and the strings packs themselves (l10n): a pack's words are written there.
    excludes:
      - '**/app/**'
      - '**/test/**'
      - '**/androidTest/**'
      - '**/commonTest/**'
      - '**/jsTest/**'
      - '**/jvmTest/**'
      - '**/iosTest/**'
      - '**/wasmJsTest/**'
      - '**/androidUnitTest/**'
      - '**/androidInstrumentedTest/**'
      - '**/androidHostTest/**'
      - '**/androidDeviceTest/**'
      - '**/screenshotScenes/**'
      - '**/foundation/l10n/**'
  FixedSideLayout:
    active: true
    excludes: *notPublished
  SizeOutsideStyle:
    active: true
    # The components: foundation itself is the layer of tokens. Sizes are written in the style files.
    includes: [ '**/ui/components/src/**' ]
    excludes: *notPublished
    allowedPathPattern: 'Style[^/]*\.kt$'
  ColorOutsideTokens:
    active: true
    includes: [ '**/ui/components/src/**' ]
    excludes: *notPublished
    allowedPathPattern: 'Style[^/]*\.kt$'
  # These two are for an app that uses the library: they are on in demo.yml and in the settings of an app.
  ImageWithoutDescription:
    active: false
  UntaggedControl:
    active: false
```
