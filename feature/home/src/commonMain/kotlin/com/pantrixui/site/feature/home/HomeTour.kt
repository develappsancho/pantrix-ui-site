package com.pantrixui.site.feature.home

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.pantrixui.components.elements.button.ButtonVariant
import com.pantrixui.components.elements.button.PantrixButton
import com.pantrixui.components.elements.icon.PantrixIcon
import com.pantrixui.components.elements.link.LinkUnderline
import com.pantrixui.components.elements.link.PantrixLink
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.icons.PantrixIcons
import com.pantrixui.foundation.platform.PlatformType
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.content.LandingBand
import com.pantrixui.site.core.designsystem.content.OnPlatform
import com.pantrixui.site.core.designsystem.content.dotted
import com.pantrixui.site.core.designsystem.device.DeviceChrome
import com.pantrixui.site.core.designsystem.device.DeviceFrame
import com.pantrixui.site.core.designsystem.device.SimDevice
import com.pantrixui.site.core.designsystem.device.outerSize
import com.pantrixui.site.core.designsystem.device.windowOf
import com.pantrixui.site.core.designsystem.siteClickable
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.model.Route
import com.pantrixui.site.core.model.Words
import kotlin.math.max
import kotlin.math.min

/**
 * A step of the home page's tour: a device, the platform whose conventions the app is drawn in there, and what the
 * step says of it.
 *
 * @property device the device and its window.
 * @property chrome what the window is drawn in.
 * @property platform the platform the app follows there.
 * @property title what the step is called.
 * @property sub the platform and the window's class.
 * @property line what changes there, in a sentence.
 * @property apis the parts of the library that make the change, each with the page about it.
 */
enum class TourStep(
    val device: SimDevice,
    val chrome: DeviceChrome,
    val platform: PlatformType,
    val title: Words,
    val sub: Words,
    val line: Words,
    val apis: List<Pair<String, Route>>
) {
    Phone(
        SimDevice.Phone,
        DeviceChrome.Device,
        PlatformType.Ios,
        Words("Phone", "Telefon"),
        Words("iOS · Compact", "iOS · Compact"),
        Words(
            "A tab bar below and one list; a recipe opens on a screen of its own.",
            "Altta bir sekme çubuğu ve tek bir liste; bir tarif kendi ekranında açılır."
        ),
        listOf(
            "PantrixNavigationBar" to Route.Component("navigation-bar"),
            "PantrixListDetailScaffold" to Route.Component("list-detail-scaffold")
        )
    ),
    Book(
        SimDevice.Book,
        DeviceChrome.Device,
        PlatformType.Android,
        Words("Foldable, open", "Katlanabilir, açık"),
        Words("Android · Expanded", "Android · Expanded"),
        Words(
            "A rail at the side, and the list and its recipe on either side of the fold, never across it.",
            "Yanda bir ray; liste ve tarifi katın iki yanında, hiçbir zaman üstünde değil."
        ),
        listOf(
            "PantrixNavigationRail" to Route.Component("navigation-rail"),
            "PantrixListDetailScaffold" to Route.Component("list-detail-scaffold")
        )
    ),
    Table(
        SimDevice.Table,
        DeviceChrome.Device,
        PlatformType.Android,
        Words("Foldable on a table", "Masadaki katlanabilir"),
        Words("Android · Tabletop", "Android · Tabletop"),
        Words(
            "Cooking mode: the step above the fold, the timer and its buttons below it, where a finger reaches.",
            "Pişirme modu: adım katın üstünde, zamanlayıcı ve düğmeleri altında, parmağın yetiştiği yerde."
        ),
        listOf("PantrixTabletopLayout" to Route.Component("tabletop-layout"))
    ),
    Tablet(
        SimDevice.Tablet,
        DeviceChrome.Device,
        PlatformType.Ios,
        Words("Tablet", "Tablet"),
        Words("iPadOS · Medium", "iPadOS · Medium"),
        Words(
            "A rail beside a grid of recipes, two to a row, in a Medium window.",
            "Medium bir pencerede bir rayın yanında tariflerin ızgarası, her satırda iki tane."
        ),
        listOf(
            "PantrixNavigationRail" to Route.Component("navigation-rail"),
            "PantrixLayout" to Route.Guide("adaptive")
        )
    ),
    Desktop(
        SimDevice.Desktop,
        DeviceChrome.MacWindow,
        PlatformType.Desktop,
        Words("Desktop", "Masaüstü"),
        Words("macOS · ExtraLarge", "macOS · ExtraLarge"),
        Words(
            "A sidebar, the list, the recipe and the week's shopping beside them: room for three panes.",
            "Bir kenar çubuğu, liste, tarif ve yanlarında haftanın alışverişi: üç panele yer var."
        ),
        listOf(
            "PantrixSidebar" to Route.Component("sidebar"),
            "PantrixListDetailScaffold" to Route.Component("list-detail-scaffold")
        )
    ),
    Web(
        SimDevice.Laptop,
        DeviceChrome.Browser,
        PlatformType.Web,
        Words("Web", "Web"),
        Words("A browser · Large", "Bir tarayıcı · Large"),
        Words(
            "A browser tab at 1280: room for two panes, so the shopping list waits, and the web's own preset.",
            "1280 genişliğinde bir tarayıcı sekmesi: iki panele yer var, alışveriş listesi bekler; web'in kendi ön " +
                "ayarı."
        ),
        listOf("PantrixTheme(platform = …)" to Route.Doc("platforms"))
    )
}

/**
 * The adaptive tour: the recipe app as its window grows from a phone to a foldable, a tablet, a desktop and a browser
 * tab, each step a device on a dotted stage. It turns by itself while it is in view, stops for a reader who picks a
 * step or presses its button, and never turns where the system asks for less motion; the dishes' pictures are fetched
 * once it has been seen.
 *
 * @param state the page's state: the step on show and whether it turns.
 * @param onAction where what the reader does goes.
 * @param open opens one of the site's pages.
 */
@Composable
internal fun AdaptiveTour(
    state: HomeUiState,
    onAction: (HomeAction) -> Unit,
    open: (Route) -> Unit
) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val colors = PantrixTheme.colors
    val still = PantrixTheme.motion.reduceMotion
    var inView by remember { mutableStateOf(false) }
    var seen by remember { mutableStateOf(false) }
    val window = LocalWindowInfo.current.containerSize
    val act by rememberUpdatedState(onAction)
    LaunchedEffect(still) { if (still && state.tourPlaying) act(HomeAction.ToggleTour) }
    LaunchedEffect(state.tourPlaying, state.tourStep, inView, still) {
        if (state.tourPlaying && inView && !still) {
            // Counted in frames: a tab a browser hides draws none, and the tour waits with it.
            val start = withFrameMillis { it }
            while (withFrameMillis { it } - start < TOUR_INTERVAL) Unit
            act(HomeAction.NextTourStep)
        }
    }
    val top = when (layout) {
        SiteLayout.Desktop -> 96.dp
        SiteLayout.Tablet -> 64.dp
        SiteLayout.Phone -> 48.dp
    }
    LandingBand(
        PaddingValues(start = layout.gutter, end = layout.gutter, top = top),
        Modifier.onGloballyPositioned { coordinates ->
            // What of the band the window shows: its photos come once any of it does, and it turns once a third
            // of it, or of a window it does not fit, does, so a reader who comes to it finds its first step.
            val shown = coordinates.boundsInWindow().height
            if (shown > 0f) seen = true
            inView = shown >= min(coordinates.size.height, window.height) * IN_VIEW
        },
        gap = if (layout == SiteLayout.Desktop) 32.dp else 20.dp
    ) {
        val simulator = @Composable {
            PantrixLink(
                text = TourSay.Simulator.of(language),
                onClick = { open(Route.Adaptive) },
                style = SiteTheme.type.body.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                tagKey = "tourSimulator",
                underline = LinkUnderline.OnHover
            )
        }
        if (layout == SiteLayout.Desktop) {
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(40.dp)) {
                Column(Modifier.weight(1f).widthIn(max = 720.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TourWords()
                }
                simulator()
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { TourWords() }
        }
        TourBox(state, seen, onAction, open)
        if (layout != SiteLayout.Desktop) simulator()
        PantrixText(
            text = RecipeSay.Credits.of(language),
            style = SiteTheme.type.bodySmall.copy(fontSize = 12.5.sp, lineHeight = 18.sp),
            color = SiteTheme.colors.muted
        )
    }
}

/**
 * The stage and what stands beside it, a desktop's, or under it elsewhere: the steps, what the step on show is, and
 * the button that stops the tour.
 *
 * A desktop's box is as tall as the stage's [TOUR_HEIGHT], or as its column where that is taller, as the design's box
 * grows with its column: at a fixed height a column taller than the stage lost the button at its foot under the box's
 * edge. The stage is measured after the column, to the box's size. On every window the caption keeps the room of the
 * tallest step's, so the box holds one height as the tour turns and the page under it stands still.
 */
@Composable
private fun TourBox(
    state: HomeUiState,
    photos: Boolean,
    onAction: (HomeAction) -> Unit,
    open: (Route) -> Unit
) {
    val layout = SiteTheme.layout
    val colors = PantrixTheme.colors
    val shape = RoundedCornerShape(if (layout == SiteLayout.Phone) 20.dp else 24.dp)
    val box = Modifier
        .fillMaxWidth()
        .clip(shape)
        .background(SiteTheme.colors.canvas)
        .border(1.dp, SiteTheme.colors.hairline, shape)
    if (layout == SiteLayout.Desktop) {
        Box(box) {
            Stage(state.tourStep, photos, Modifier.matchParentSize().padding(end = TOUR_COLUMN).padding(32.dp))
            Row(Modifier.fillMaxWidth()) {
                Spacer(Modifier.weight(1f))
                Column(
                    Modifier
                        .width(TOUR_COLUMN)
                        .heightIn(min = TOUR_HEIGHT)
                        .background(colors.surface)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    StepList(state.tourStep, onAction)
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Box(Modifier.fillMaxWidth().height(1.dp).background(SiteTheme.colors.hairline))
                        InTallestRoom(state.tourStep) { step -> Caption(step, open) }
                    }
                    Spacer(Modifier.weight(1f))
                    PlayRow(state, onAction)
                }
            }
        }
    } else {
        Column(box) {
            Stage(
                state.tourStep,
                photos,
                Modifier.fillMaxWidth().height(if (layout == SiteLayout.Phone) 420.dp else 560.dp).padding(20.dp)
            )
            Column(
                Modifier.fillMaxWidth().background(colors.surface).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StepChips(state.tourStep, onAction, scrolls = layout == SiteLayout.Phone)
                InTallestRoom(state.tourStep) { step -> Caption(step, open) }
                PlayRow(state, onAction)
            }
        }
    }
}

/** The tour's kicker, its heading and its lead. */
@Composable
private fun TourWords() {
    val language = LocalLanguage.current
    Kicker(TourSay.Kicker.of(language))
    SectionHeading(TourSay.Title.of(language))
    SectionLead(start = TourSay.Lead.of(language))
}

/**
 * The dotted stage with [step]'s device on it, scaled to fit and in the middle; one step gives way to the next with
 * the theme's motion.
 */
@Composable
private fun Stage(
    step: TourStep,
    photos: Boolean,
    modifier: Modifier
) {
    val density = LocalDensity.current
    val motion = PantrixTheme.motion
    Box(modifier.dotted(PantrixTheme.colors.border, step = 18.dp)) {
        Crossfade(step, animationSpec = tween(motion.duration(motion.slow)), label = "TourStep") { shown ->
            BoxWithConstraints(Modifier.fillMaxWidthAndHeight(), contentAlignment = Alignment.Center) {
                val outer = shown.chrome.outerSize(shown.device)
                val scale = min(min(maxWidth / outer.width, maxHeight / outer.height), 1f)
                DeviceFrame(
                    shown.device,
                    windowOf(shown.device, density),
                    scale,
                    chrome = shown.chrome,
                    address = TOUR_ADDRESS
                ) {
                    OnPlatform(shown.platform) { RecipeApp(photos) }
                }
            }
        }
    }
}

/** The steps one under another, a desktop's: a number, a name and what it is, the one on show marked. */
@Composable
private fun StepList(
    current: TourStep,
    onAction: (HomeAction) -> Unit
) {
    val language = LocalLanguage.current
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        TourStep.entries.forEach { step ->
            val on = step == current
            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .siteClickable("tour_${step.name.lowercase()}", Role.Button, RoundedCornerShape(12.dp)) {
                        onAction(HomeAction.PickTourStep(step))
                    }
                    .semantics { selected = on }
                    .background(if (on) PantrixTheme.colors.primary.container else PantrixTheme.colors.surface)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Number(step, on)
                Column {
                    PantrixText(
                        text = step.title.of(language),
                        style = SiteTheme.type.body.copy(
                            fontSize = 14.5.sp,
                            lineHeight = STEP_LINE_HEIGHT,
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (on) PantrixTheme.colors.primary.onContainer else PantrixTheme.colors.textSecondary
                    )
                    PantrixText(
                        text = step.sub.of(language),
                        style = SiteTheme.type.bodySmall.copy(fontSize = 12.5.sp, lineHeight = STEP_LINE_HEIGHT),
                        color = SiteTheme.colors.muted
                    )
                }
            }
        }
    }
}

/** The steps as chips, a tablet's wrapping and a phone's in a row it scrolls. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StepChips(
    current: TourStep,
    onAction: (HomeAction) -> Unit,
    scrolls: Boolean
) {
    val language = LocalLanguage.current
    val chip = @Composable { step: TourStep ->
        val on = step == current
        val shape = CircleShape
        Row(
            Modifier
                .height(44.dp)
                .siteClickable("tour_${step.name.lowercase()}", Role.Button, shape) {
                    onAction(HomeAction.PickTourStep(step))
                }
                .semantics { selected = on }
                .background(if (on) PantrixTheme.colors.primary.container else PantrixTheme.colors.surface)
                .border(1.dp, if (on) SiteTheme.colors.accentLine else PantrixTheme.colors.border, shape)
                .padding(start = 8.dp, end = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Number(step, on)
            PantrixText(
                text = step.title.of(language),
                style = SiteTheme.type.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                color = if (on) PantrixTheme.colors.primary.onContainer else PantrixTheme.colors.textSecondary
            )
        }
    }
    if (scrolls) {
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TourStep.entries.forEach { chip(it) }
        }
    } else {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TourStep.entries.forEach { chip(it) }
        }
    }
}

/** A step's number in its circle, filled while it is on show. */
@Composable
private fun Number(
    step: TourStep,
    on: Boolean
) {
    val colors = PantrixTheme.colors
    Box(
        Modifier.size(26.dp).background(if (on) colors.primary.color else SiteTheme.colors.hover, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        PantrixText(
            text = (step.ordinal + 1).toString(),
            style = SiteTheme.type.body.copy(fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold),
            color = if (on) colors.primary.onColor else SiteTheme.colors.muted
        )
    }
}

/** What the step on show is: its platform, its window's size, what changes there and the parts that change it. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Caption(
    step: TourStep,
    open: (Route) -> Unit
) {
    val language = LocalLanguage.current
    val colors = PantrixTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        PantrixText(
            text = "${step.sub.of(language)} · ${step.device.width} × ${step.device.height}".uppercase(),
            style = SiteTheme.type.body.copy(
                fontSize = 12.sp,
                lineHeight = CAPTION_LINE_HEIGHT,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            ),
            color = colors.primary.onContainer
        )
        PantrixText(
            text = step.line.of(language),
            style = SiteTheme.type.body.copy(fontSize = 14.5.sp, lineHeight = CAPTION_LINE_HEIGHT),
            color = colors.textSecondary,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            step.apis.forEach { (name, route) ->
                PantrixLink(
                    text = name,
                    onClick = { open(route) },
                    style = SiteTheme.type.inlineCode.copy(fontSize = 12.5.sp),
                    tagKey = "tourApi_${name.substringBefore('(').lowercase()}",
                    underline = LinkUnderline.OnHover
                )
            }
        }
    }
}

/**
 * [content] of [step], in the room the tallest step's takes: what stands under it holds still as the tour turns. The
 * other steps' are measured and never placed, and say nothing: only [step]'s is drawn, pressed and read, and it stays
 * one part whose words change, which a screen reader reads out as they change.
 */
@Composable
private fun InTallestRoom(
    step: TourStep,
    content: @Composable (TourStep) -> Unit
) {
    Layout(
        content = {
            content(step)
            TourStep.entries.forEach { other -> Box(Modifier.clearAndSetSemantics {}) { content(other) } }
        }
    ) { measurables, constraints ->
        val loose = constraints.copy(minHeight = 0)
        val shown = measurables.first().measure(loose)
        val tallest = measurables.drop(1).maxOf { it.measure(loose).height }
        layout(shown.width, constraints.constrainHeight(max(shown.height, tallest))) { shown.place(0, 0) }
    }
}

/** The button that stops the tour or starts it again, and which step is on show. */
@Composable
private fun PlayRow(
    state: HomeUiState,
    onAction: (HomeAction) -> Unit
) {
    val language = LocalLanguage.current
    val playing = state.tourPlaying
    Row(verticalAlignment = Alignment.CenterVertically) {
        PantrixButton(
            text = (if (playing) TourSay.Pause else TourSay.Play).of(language),
            onClick = { onAction(HomeAction.ToggleTour) },
            contentDescription = (if (playing) TourSay.PauseTour else TourSay.PlayTour).of(language),
            variant = ButtonVariant.Outline,
            leadingIcon = {
                PantrixIcon(
                    if (playing) PantrixIcons.Pause else PantrixIcons.Play,
                    contentDescription = null,
                    size = 16.dp
                )
            },
            tagKey = "tourPlay"
        )
        Spacer(Modifier.weight(1f))
        PantrixText(
            text = TourSay.of(state.tourStep.ordinal + 1, TourStep.entries.size).of(language),
            style = SiteTheme.type.bodySmall.copy(fontSize = 13.sp),
            color = SiteTheme.colors.muted
        )
    }
}

/** Fills the room it is given, across and down. */
private fun Modifier.fillMaxWidthAndHeight(): Modifier = fillMaxWidth().fillMaxHeight()

/** The tour's words. */
private object TourSay {
    val Kicker = Words("One app, every window", "Tek uygulama, her pencere")
    val Title = Words("Written once. At home on every screen.", "Bir kez yazıldı. Her ekranda evinde.")
    val Lead = Words(
        "A recipe app made with Pantrix, as its window grows from a phone to a foldable, a tablet, a desktop and a " +
            "browser tab. Navigation moves from a bar to a rail and a sidebar, a recipe opens beside its list, and a " +
            "foldable set on a table keeps its buttons below the fold.",
        "Pantrix'le yapılmış bir tarif uygulaması, penceresi bir telefondan bir katlanabilir cihaza, bir " +
            "tablete, bir masaüstüne ve bir tarayıcı sekmesine büyürken. Gezinme çubuktan raya ve kenar çubuğuna " +
            "geçer, bir tarif listesinin yanında açılır, masaya konan bir katlanabilir cihaz düğmelerini katın " +
            "altında tutar."
    )
    val Simulator = Words("Try your own window in the simulator →", "Kendi pencereni simülatörde dene →")
    val Pause = Words("Pause", "Duraklat")
    val Play = Words("Play", "Oynat")
    val PauseTour = Words("Pause the tour", "Turu duraklat")
    val PlayTour = Words("Play the tour", "Turu oynat")

    /** Which step is on show. */
    fun of(at: Int, count: Int): Words = Words("Step $at of $count", "Adım $at / $count")
}

/** What the browser's bar says on the web's step. */
private const val TOUR_ADDRESS = "recipes.example.com"

/** How long a step stays on show while the tour turns by itself, in milliseconds. */
private const val TOUR_INTERVAL = 4_500L

/** How much of the band, or of a window it does not fit, shows before the tour turns. */
private const val IN_VIEW = 1f / 3f

/** How tall a desktop's tour is: its stage's height, and the least its column beside it is. */
private val TOUR_HEIGHT = 600.dp

/** How wide the column beside a desktop's stage is. */
private val TOUR_COLUMN = 320.dp

/** A step's line in the desktop's list of steps, as many times its size as the design sets it. */
private val STEP_LINE_HEIGHT = 1.3.em

/** A line of what the step on show is, as many times its size as the design's running text. */
private val CAPTION_LINE_HEIGHT = 1.55.em
