---
title: Connecting events to analytics
description: Which events Pantrix's components report, what an event carries and what it never carries, and how an app connects these events to its own analytics product (Firebase, Amplitude, PostHog…). The problem is not Pantrix's own: every Compose Multiplatform app with analytics writes a class that turns the interface's events into the events that go to the product, and the real decisions are in that class.
---

Everything said about the components and the sample was seen in tests that run in this repository, or on devices. The products' calls were read in their own documentation and were not run here. Which was seen where is written in section 7.

## 1. The problem {#problem}

A component library says "this button was pressed". What goes to the product is the app's decision:

*   **Under which name:** does the product see `Button click`, or `checkout_pay_tapped`? Names live in the product's reports for years, and must not change when a component's name changes.
*   **Never carrying what:** a text the user typed, a table row keyed by a person's name. Personal data once sent cannot be taken back.
*   **When:** a network request at every press costs battery and data. When the app goes to the background, the events waiting can be lost.
*   **With no network:** are the events thrown away, is their order kept, how large does the queue grow?
*   **With consent:** nothing must be collected before the user says yes. When they say no, the events waiting must not be sent either.
*   **In the release:** automation tags are taken out of the semantics tree in the release build, but the events must go on telling the components apart.

If these decisions are not made in one place, every screen sends its own event under its own name, personal keys leak into the product, and a network outage leaves gaps in the reports.

:::sketch analytics
:::

## 2. Pantrix's events {#pantrix-s-events}

### The event

Every component gives what the user did to the theme's analytics as a `PantrixUiEvent`:

:::table
| Field | What | Example |
| :--- | :--- | :--- |
| `component` | The name the component reports under | `Button`, `SegmentButton`, `Chart` |
| `action` | What happened | `click`, `select`, `toggle`, `open` |
| `testTag` | The component's automation tag | `Checkout.Button_pay` |
| `screen` | The `TestScope` chain, that is, the screen's name | `Checkout` |
| `props` | Values from a closed set | `variant=Solid`, `segment=express` |
:::

```kotlin
PantrixTheme(analytics = PantrixAnalytics { event -> /* give it to the connector */ }) {
    App()
}
```

*   `track` is called on the UI thread the moment the event happens. The event has no time field: the connector stamps the time. `track` takes the event and returns; the sending is done off the UI thread.
*   If no analytics is given to the theme, the events are thrown away (`PantrixAnalytics.NoOp`).
*   The app's own event goes to the same place: `LocalPantrixAnalytics.current.track(PantrixUiEvent(…))`.

### Identity: the tag and the screen

*   `testTag` is built from the component's `tagKey`: `PantrixButton(tagKey = "pay")` inside `TestScope("Checkout")` becomes `Checkout.Button_pay`. A test or a plan finds the same tag without the screen being set up, with `pantrixTestTagOf("Button", "pay", scope = "Checkout")`.
*   The tag of a component without a `tagKey` is built from the place of the call (`Button#1k3z9`), and changes when the code changes. A component the product counts is given a `tagKey`.
*   **The release build:** `TestTagPolicy.Off` does not put the tags into the semantics tree. The component still knows its tag, and its events go with that tag. A `Modifier.testTag` the app puts on itself does not change the event.
*   `screen` comes from the `TestScope` chain, and does not depend on the tag policy.
*   **A page is a screen.** A `PantrixPageScaffold` given a `tagKey` puts what it holds inside `TestScope(tagKey)`: the events of the components on it are on the screen `orders`, and their tags start with `orders.`.

### What never goes into an event

*   **Text typed and read.** No text typed into a field, standing in a field or shown by a component goes into an event. This promise is held by a regression test in every component that takes text (`TypedTextIsNeverReportedTest`). The components it covers: `PantrixTextField`, `PantrixFilledTextField`, `PantrixAutoResizeTextField`, `PantrixFilledAutoResizeTextField`, `PantrixSecureTextField`, `PantrixFilledSecureTextField`, `PantrixDateTextField`, `PantrixDateFilledTextField`, `PantrixTimeTextField`, `PantrixTimeFilledTextField`, the search field of `PantrixSelect` and of `PantrixMultiSelect`, `PantrixCommandPalette`, the fields of `PantrixElasticDatePicker`, and the document and the search of `PantrixJsonView`. The test types a word into every field, then a number in its place, and leaves the field by the keyboard action and by giving the focus elsewhere. At the end it verifies that neither is in any field of any event.
*   **A free value.** An event's `props` carries only values of the three kinds below. The one value the user writes themselves is `PantrixPageJump`'s page number, and that is a number within the pagination's range.

### The three kinds of properties

:::table
| Kind | Keys | Personal? |
| :--- | :--- | :--- |
| Pantrix's words and true/false values (20) | `action`, `checked`, `collapsed`, `copied`, `destructive`, `direction`, `duration`, `external`, `folded`, `format`, `part`, `reselected`, `revealed`, `selected`, `shown`, `state`, `to`, `tone`, `variant`, `via` | No: their values are the library's own words (`Solid`, `back`, `true`) |
| Numbers (8) | `block`, `cell`, `count`, `index`, `level`, `rejected`, `rows`, `size` | No |
| The app's keys (14) | `row`, `column`, `option`, `node`, `series`, `slice`, `stage`, `task`, `tile`, `destination`, `section`, `page`, `command`, `segment` | **They can be:** each is the key the app gives a thing |
:::

`page` is of two kinds. In `PantrixCarousel`, `PantrixPagination` and `PantrixPageJump` it is a number, while in `PantrixBreadcrumb` it is the key the app gives the page. The sample therefore does not count it among Pantrix's values.

The components that send the app's keys:

:::table
| Key | Component | Its value |
| :--- | :--- | :--- |
| `row`, `column` | `PantrixDataTable` | The row's and the column's key |
| `option` | `PantrixSelect`, `PantrixMultiSelect` | The option's key |
| `node` | `PantrixTreeView`, `PantrixLazyTreeView`, `PantrixSankeyChart` | The node's key; in a Sankey, a flow is `source>target` |
| `series` | The charts with axes, `PantrixScatterChart`, `PantrixRadarChart`, `PantrixPolarChart` | The series' key |
| `slice` | `PantrixPieChart` | The slice's key |
| `stage` | `PantrixFunnelChart` | The stage's key |
| `task` | `PantrixGanttChart` | The task's key |
| `tile` | `PantrixTreemap` | The tile's key |
| `destination` | `PantrixNavigationBar`, `PantrixNavigationRail`, `PantrixSidebar` | The destination's key |
| `section` | `PantrixAccordion`, `PantrixSidebar` | The section's key |
| `page` | `PantrixBreadcrumb` | The page's key |
| `command` | `PantrixCommandPalette` | The command's key |
| `segment` | `PantrixSegmentButton` | The option's `tagKey`, or else its index |
:::

The library always gives these keys to the theme's analytics; whether they go to the product is the connector's decision. In the components' tests the values of `row` were people's names (`ada`, `grace`): every event of a table that keys its users by name carries that name.

### Events that come without the user doing anything

:::table
| Component | Event | When |
| :--- | :--- | :--- |
| `PantrixCarousel` | `change`, `via=auto` | When it turns by itself |
| `PantrixSnackbarHost` | `dismiss`, `via=timeout` | When its time runs out |
| `PantrixElasticDatePicker` | `autoRefresh` | When it refreshes by itself |
| `PantrixDialog`, `PantrixSheet`, `PantrixMenu`, `PantrixPopover`, `PantrixCommandPalette` | `open` | When the program opens it, too |
| `PantrixPageScaffold`, `PantrixLazyPageScaffold` | `show` | Each time a page with a `tagKey` comes into the composition: a screen's view |
:::

A connector that does not want to count these as the user's interaction looks at `via`, or gives them a separate name in its plan.

### The components' events

The table was written from the events seen in the components' tests and from their sources. The "Name" column is the event's `component`. The components not in the table do not report events themselves; the components inside them report their own: `PantrixTopBar`'s back button (`PantrixBackButton`) is a `PantrixIconButton`, and reports that one's event.

:::table
| Component | Name | `action` | `props` |
| :--- | :--- | :--- | :--- |
| `PantrixAccordion` | `Accordion` | `expand`, `collapse` | `section` |
| `PantrixAlert` | `Alert` | `dismiss` | `tone`, `variant` |
| `PantrixBreadcrumb` | `Breadcrumb` | `select` | `page` (a key), `folded` |
| `PantrixButton` | `Button` | `click` | `variant` |
| `PantrixCard` | `Card` | `click` | `variant` |
| `PantrixCarousel` | `Carousel` | `change`, `pause`, `play` | `page` (a number), `via` |
| `PantrixChart`, `PantrixLineChart`, `PantrixBarChart`, `PantrixAreaChart`, `PantrixRangeChart`, `PantrixWaterfallChart`, `PantrixCandlestickChart` | `Chart` | `select`, `toggle`, `zoom`, `export` | `series`, `shown`, `to`, `format` |
| `PantrixCheckbox`, `PantrixTriStateCheckbox` | `Checkbox` | `toggle` | `state` |
| `PantrixChip` | `Chip` | `click`, `toggle`, `remove` | `selected` |
| `PantrixCodeBlock` | `CodeBlock` | `copy` | `copied` |
| `PantrixCommandPalette` | `CommandPalette` | `open`, `run`, `dismiss` | `command`, `via` |
| `PantrixDataTable` | `DataTable` | `select`, `selectAll`, `sort`, `expand`, `collapse`, `open` | `row`, `column`, `selected`, `rows`, `direction` |
| `PantrixDatePicker` | `DatePicker` | `select`, `years` | `part` |
| `PantrixDateTextField`, `PantrixDateFilledTextField`, `PantrixTimeTextField`, `PantrixTimeFilledTextField` | The component's name (`DateTextField`…) | `focus`, `open`, `select` | — |
| `PantrixDialog` | `Dialog` | `open`, `dismiss` | `via` |
| `PantrixElasticDatePicker` | `ElasticDatePicker` | `open`, `select`, `refresh`, `autoRefresh` | — |
| `PantrixFileDrop` | `FileDrop` | `browse`, `drop`, `files` | `count`, `rejected`, `via` |
| `PantrixFunnelChart` | `Funnel` | `select`, `export` | `stage`, `format` |
| `PantrixGanttChart` | `Gantt` | `select`, `zoom`, `export` | `task`, `to`, `format` |
| `PantrixGauge`, `PantrixMeterGroup`, `PantrixProgressCircle`, `PantrixSparkline` | The component's name (`Gauge`…) | `export` | `format` |
| `PantrixHeatmap` | `Heatmap` | `select`, `export` | `cell`, `format` |
| `PantrixIconButton` | `IconButton` | `click` | `variant` |
| `PantrixJsonView` | `JsonView` | `expand`, `collapse`, `copy` | `level`, `copied` |
| `PantrixLazyTreeView` | `LazyTreeView` | `select`, `expand`, `collapse`, `check`, `uncheck`, `action` | `node` |
| `PantrixLink` | `Link` | `click` | `external` |
| `PantrixMenu` | `Menu`; its item `MenuItem` | `open`, `dismiss`; the item: `click` | the item: `destructive` |
| `PantrixMultiSelect` | `MultiSelect`; its search field `MultiSelectSearch` | `open`, `toggle`; the search: `focus`, `clear` | `option`, `selected` |
| `PantrixNavigationBar`, `PantrixNavigationRail` | The component's name | `select` | `destination`, `reselected` |
| `PantrixPageJump` | `PageJump` | `focus`, `select` | `page` (a number) |
| `PantrixPageScaffold`, `PantrixLazyPageScaffold` | `PageScaffold` | `show` | — |
| `PantrixPageSize` | `PageSize` | `open`, `select` | `size` |
| `PantrixPagination` | `Pagination` | `select` | `page` (a number), `via` |
| `PantrixPieChart` | `PieChart` | `select`, `toggle`, `export` | `slice`, `shown`, `format` |
| `PantrixPolarChart`, `PantrixRadarChart` | The component's name | `select`, `toggle`, `export` | `series`, `shown`, `format` |
| `PantrixPopover` | `Popover` | `open`, `dismiss` | — |
| `PantrixRadioButton` | `RadioButton` | `select` | — |
| `PantrixRadioGroup` | `RadioGroup` | `select` | `index` |
| `PantrixSankeyChart` | `Sankey` | `select`, `export` | `node`, `format` |
| `PantrixScatterChart` | `ScatterChart` | `select`, `toggle`, `zoom`, `export` | `series`, `shown`, `to`, `format` |
| `PantrixSecureTextField`, `PantrixFilledSecureTextField` | The component's name | `focus`, `clear`, `toggle` | `revealed` |
| `PantrixSegmentButton` | `SegmentButton` | `select` | `segment` |
| `PantrixSelect` | `Select`; its search field `SelectSearch` | `open`, `select`; the search: `focus`, `clear` | `option` |
| `PantrixSheet` | `Sheet` | `open`, `dismiss` | `via` |
| `PantrixSidebar` | `Sidebar` | `select`, `expand`, `collapse` | `destination`, `section`, `collapsed` |
| `PantrixSnackbarHost` | `Snackbar` | `open`, `action`, `dismiss` | `tone`, `duration`, `action`, `via` |
| `PantrixStatusTracker` | `StatusTracker` | `select`, `export` | `block`, `format` |
| `PantrixSwitchButton` | `SwitchButton` | `toggle` | `checked` |
| `PantrixTabs` | `Tabs` | `select` | `index`, `via` |
| `PantrixTextField`, `PantrixFilledTextField`, `PantrixAutoResizeTextField`, `PantrixFilledAutoResizeTextField` | The component's name | `focus`, `clear` | — |
| `PantrixTimePicker` | `TimePicker` | `select` | `part` |
| `PantrixTreemap` | `Treemap` | `select`, `export` | `tile`, `format` |
| `PantrixTreeView` | `TreeView` | `select`, `expand`, `collapse`, `check`, `uncheck`, `action` | `node` |
:::

## 3. The connector {#the-connector}

The connector is the `PantrixAnalytics` given to the theme: it takes Pantrix's events and builds the events that go to the product itself. The sample's connector is `ProductAnalytics`, and all the decisions are gathered in its constructor:

```kotlin
@Composable
fun App() {
    val scope = rememberCoroutineScope()
    val analytics = remember(scope) {
        ProductAnalytics(
            plan = CHECKOUT_PLAN,            // which event under which name, with which values
            transport = productTransport,    // the one call that gives to the product (section 4)
            clock = Clock.System,            // the moment the event happened
            scope = scope,                   // the timer and the sending stop with the screen
            policy = SendPolicy()            // batches of 20, 30 seconds, a queue of 500 events
        )
    }
    // On going to the background the waiting events are sent: there may be no other chance.
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { scope.launch { analytics.flush() } }
    PantrixTheme(analytics = analytics, testTagPolicy = TestTagPolicy.Off) {
        CheckoutScreen()
    }
}
```

### The tracking plan

```kotlin
val CHECKOUT_PLAN = TrackingPlan(
    listOf(
        PlannedEvent(pantrixTestTagOf("IconButton", "more", "Checkout"), "click", "quantity_increased"),
        PlannedEvent(
            tag = pantrixTestTagOf("SegmentButton", "shipping", "Checkout"),
            action = "select",
            name = "shipping_chosen",
            notPersonal = setOf("segment")
        ),
        PlannedEvent(
            tag = pantrixTestTagOf("Button", "pay", "Checkout"),
            action = "click",
            name = "checkout_pay_tapped",
            keep = setOf("variant")
        )
    )
)
```

*   The plan recognises an event by its component's tag and its action. The tag is built by `pantrixTestTagOf`, and it is the same as the tag the component on the screen carries.
*   `keep` says which of Pantrix's values go with the event. `notPersonal` says which of the app's keys are not personal. A planned event carries only what these two count.
*   While it is being built, the plan checks three things: no two events are given to the same tag and action; no tag built from the place of the call (one carrying `#`) is in the plan; `keep` counts only Pantrix's values. Writing one of the app's keys into `keep` is an error in the constructor: the key passes only with `notPersonal`, that is, by saying it is not personal.

### The general event

Every event the plan does not name goes as a single `ui_interaction`, and carries these: `ui_component`, `ui_action`, `ui_screen`, `ui_tag` (only if it was built from a key), and Pantrix's words and numbers. The app's keys and the tags built from the place of the call do not go in.

There are two reasons for a single general event. Products limit the separate event names (500 in Firebase), and a name for each component would share that limit with the plan's names. And in the product's reports only the plan's names show in the product's language; the rest is filtered in one place.

### Consent

`Consent` has three values: `Unknown`, `Granted`, `Denied`. Until it is `Granted` no event enters the queue, and each is entered in the log as "not collected". When consent is taken back, the waiting events are thrown away and the timers stop. A batch on its way at that moment, if it fails, is not tried again but thrown away. The product's own SDK has its consent too, and the two must change together (section 4).

### Sending and time

*   `track` stamps the event with its time, puts it in the queue and returns. There is no network on the UI thread.
*   A batch goes in three cases: when `SendPolicy.batch` events have gathered, when the first event has waited `SendPolicy.every`, and when the app says `flush()`. While nothing waits, nothing wakes up.
*   The sending is done on `sendOn` (`Dispatchers.Default`), and only one batch is on its way at a time. The dispatcher of `scope` is the UI's: between `track` and the sending, the events change hands on that single thread.

### With no network

*   A failed batch is put back at the head of the queue, in its order, and the events reach the product in the order they happened.
*   The retry is made after `firstRetry` (1 second), doubles at every failure, and is at most `longestRetry` (5 minutes). A sending that succeeds starts the waits from the beginning.
*   The queue holds `capacity` (500) events. When it is full the oldest is thrown away and counted in the log.
*   The sample's queue is in memory: if the process dies, the waiting events are lost. If there is an SDK that writes the events to disk, leaving the queue to it is sturdier (section 4).

## 4. Connecting to the product {#connecting-to-the-product}

The connector's one point of contact with the product is `Transport`:

```kotlin
fun interface Transport {
    /** Gives [batch] to the product: true if it arrived. */
    suspend fun send(batch: List<ProductEvent>): Boolean
}
```

A `ProductEvent` has a name (`name`), properties (`properties`, all `String`) and a time (`at`). If the sender returns false or throws an error, the batch waits and is tried again.

### SDKs with a queue of their own

The three SDKs below all gather the events themselves and send them batch by batch. The connector's queue is then a second queue. The recommended setup: keep the plan and the consent in the connector, give every event to the SDK at once (`SendPolicy(batch = 1)`), and leave the gathering, the retrying and the sending in the background to the SDK. The sender returns true when the SDK has taken the event.

### Firebase (Android)

```kotlin
val transport = Transport { batch ->
    batch.forEach { event ->
        Firebase.analytics.logEvent(event.name) {
            event.properties.forEach { (key, value) -> param(key, value) }
        }
    }
    true
}
```

*   Setup: `implementation(platform("com.google.firebase:firebase-bom:…"))` and `implementation("com.google.firebase:firebase-analytics")`.
*   Limits: an event carries at most 25 parameters. Parameter names are at most 40 characters, start with a letter and hold only letters, digits and underscores. Text values are at most 100 characters (500 in Google Analytics 360). At most 500 separate event names can be used. The prefixes `firebase_`, `google_` and `ga_` are reserved. The general event's `ui_` names keep these rules. The `ui_tag` of a component under many nested `TestScope`s can come close to 100 characters: such a component that the product counts is written into the plan under its own name.
*   Consent: `setAnalyticsCollectionEnabled(false)` turns collecting off. With `setConsent`, `ANALYTICS_STORAGE` is denied. They are called together with the connector's `consent`.

### Amplitude (Android, Kotlin SDK)

```kotlin
val amplitude = Amplitude(AMPLITUDE_API_KEY, applicationContext) {
    flushQueueSize = 20          // its default is 30
    flushIntervalMillis = 30000  // its default is 30000
}
val transport = Transport { batch ->
    batch.forEach { event ->
        amplitude.track(event.name, mutableMapOf<String, Any?>().apply { putAll(event.properties) })
    }
    true
}
```

*   Consent: while `amplitude.optOut = true`, the SDK neither keeps nor sends events.
*   `amplitude.flush()` sends the waiting events at once: the connector's `flush()` can call it too.

### PostHog (Android)

```kotlin
PostHogAndroid.setup(context, PostHogAndroidConfig(apiKey = POSTHOG_API_KEY, host = POSTHOG_HOST))
val transport = Transport { batch ->
    batch.forEach { event -> PostHog.capture(event = event.name, properties = event.properties) }
    true
}
```

*   Gathering: `flushAt` (by default 20 events) and `flushIntervalSeconds` (by default 30). `PostHog.flush()` sends the waiting events at once, but by its documentation does not guarantee the sending.
*   Consent: `PostHog.optOut()`, or `config.optOut = true` before the setup.

### The other platforms

The products' iOS and web SDKs are written in Swift and in JavaScript. Common code calls them in two ways:

*   **`expect`/`actual`:** `expect fun productTransport(): Transport` in common code, and on each platform an `actual` that calls that platform's SDK. On the web the SDK's JavaScript interface is called from Kotlin with `external` declarations.
*   **A function from the shell:** the platform's shell gives common code a plain function, and the sender calls it. On iOS, something like `MainViewController(onEvent: { name, properties in Analytics.logEvent(name, parameters: properties) })`. Kotlin's type `(String, Map<String, String>) -> Unit` is a closure in Swift. There is no need to implement a `suspend` interface in Swift.

## 5. Testing {#testing}

*   **The connector's decisions:** inside `runTest`, with `scope = backgroundScope` and `sendOn = StandardTestDispatcher(testScheduler)`. The timer and the retry run in the test's virtual time: with `advanceTimeBy(policy.every - 1.milliseconds)` one says "it did not go before its time", and with 2 milliseconds more, "it went". The clock is `TestClock` (`com.pantrixui.testing`). The sample's `ProductAnalyticsTest` (15 tests) holds each decision in a test of its own.
*   **The plan:** the way from a Pantrix event to the product's event is a pure function (`TrackingPlan.productEventOf`), and is tested without a screen (`TrackingPlanTest`).
*   **A screen's events:** `PantrixTestTheme(analytics = RecordingAnalytics())` keeps what the components report, in order: `eventsOf("Button")` ([the testing guide](/docs/guides/testing)).
*   **The screen and the connector together:** in a UI test the connector is given `sendOn = Dispatchers.Unconfined`. In the browser there is one thread, and while `waitUntil` waits, `Dispatchers.Default` gets no turn: the sending never happens, and the test runs into its timeout. The sample's `SampleScreenTest` also verifies that what was typed into the promotion code is in no batch.
*   **Like a release build:** on a screen set up with `TestTagPolicy.Off`, it is tested that the semantics tree has no Pantrix tag and that the plan's event still comes.
*   The sample's tests: `./gradlew :samples:pantrix-analytics:shared:jvmTest`, `iosSimulatorArm64Test`, `jsBrowserTest`, `wasmJsBrowserTest`, and `connectedAndroidDeviceTest` while a device or an emulator is connected.

## 6. Without the library {#without}

The one part of the connector bound to Pantrix is `track(event: PantrixUiEvent)`, which takes the event, and the four fields the plan reads from that event (`component`, `action`, `testTag`, `screen`), with `props`. An app that takes events from another source writes `productEventOf` with its own event type. The queue, the consent, the sending and `Transport` stay as they are: the sample's files `ProductAnalytics.kt`, `SendPolicy.kt`, `Transport.kt` and `Consent.kt` are not bound to Compose either.

## 7. What was verified and what was not {#verification}

### Verified

*   **The inventory of events:** the components' JVM tests (4896 tests, `c808f18b`) were run in a trial lane, with every event that passed through the test theme written to a file: 2537 events, 134 (name, action) pairs from 65 names. Section 2's table was written from these events and from the sources. The `select` of the date and time fields was read in `date/common/PickerField.kt`, and the search fields' `clear` in the shared search field of `SelectList.kt`. `autoRefresh` later took the place of `auto_refresh`.
*   **No text goes in:** `TypedTextIsNeverReportedTest` (15 tests) was tried with two fake fields that leak the text at the keyboard action and on leaving the field, and caught both.
*   **The tag under `TestTagPolicy.Off`:** `TestTagTest` and `OwnTestTagTest`. The test that checks the event's tag failed with empty tags on the old code.
*   **The connector:** the sample's 22 tests passed on five targets: the JVM, Android (an emulator, API 33), the iOS simulator, JS and Wasm. The connector was broken on purpose in 12 separate places, and every break was caught by at least one test: the consent, the doubling wait, the queue's limit, the timer, `flush`, the order, one batch at a time, the reset of the waits, the general event's values and the call-site tag.
*   **The app:** it was opened in the iPhone 18 Pro Max simulator, the Pixel 7 Pro emulator (API 33), the browser (Wasm) and on the desktop. The flow of consent, quantity, shipping, promotion code, payment and "Send now" was tried. The promotion code typed was not in the JSON sent.

### Read, not run

*   **Firebase:** the `logEvent` builder and the setup from the Android documentation ("Log events", "Get started"); the limits from Firebase's Swift reference (the `Analytics` class); the consent calls from the Kotlin reference (`FirebaseAnalytics.ConsentType`). A length limit for an event's name could not be found on Firebase's own pages; the names are kept short and in `snake_case` in the plan.
*   **Amplitude:** the Android Kotlin SDK page (setup, `track`, `optOut`, `flushQueueSize`, `flushIntervalMillis`, `flush`). The type of `track`'s property parameter was taken from the documentation's example (`MutableMap<String, Any?>`).
*   **PostHog:** the Android page (setup, `capture`, `optOut`, `flush`, `flushAt`, `flushIntervalSeconds`).
*   None of the three products is a dependency in this repository: the calls were not compiled and not run. Giving a function from the shell on iOS and `external` declarations on the web were not tried either.
*   The documents were read on 2026-10-24.
