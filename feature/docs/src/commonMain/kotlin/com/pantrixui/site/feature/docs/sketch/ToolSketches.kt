@file:Suppress("MagicNumber") // A sketch's sample values, its times, amounts and sizes, are what it shows.

package com.pantrixui.site.feature.docs.sketch

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.chart.export.ChartExport
import com.pantrixui.components.chart.export.ChartExportFile
import com.pantrixui.components.chart.pie.PantrixPieChart
import com.pantrixui.components.chart.pie.PieSlice
import com.pantrixui.components.container.card.CardVariant
import com.pantrixui.components.container.card.PantrixCard
import com.pantrixui.components.elements.button.ButtonVariant
import com.pantrixui.components.elements.button.PantrixButton
import com.pantrixui.components.elements.switchbutton.PantrixSwitchButton
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.components.inputs.textfield.PantrixTextField
import com.pantrixui.foundation.analytics.PantrixAnalytics
import com.pantrixui.foundation.analytics.PantrixUiEvent
import com.pantrixui.foundation.testing.TestTagPolicy
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.content.CodeView
import com.pantrixui.site.core.designsystem.content.PhoneFrame
import com.pantrixui.site.core.designsystem.content.SketchChoice
import com.pantrixui.site.core.designsystem.content.SketchControls
import com.pantrixui.site.core.designsystem.content.SketchFigure
import com.pantrixui.site.core.designsystem.content.SketchNote
import com.pantrixui.site.core.designsystem.content.SketchSwitch
import com.pantrixui.site.core.designsystem.content.WindowFrame
import com.pantrixui.site.core.designsystem.inCapitals
import com.pantrixui.site.core.designsystem.sized
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.model.Block
import com.pantrixui.site.core.model.SiteAddress
import com.pantrixui.site.core.model.Token
import com.pantrixui.site.core.model.Words
import kotlinx.coroutines.delay

/**
 * The web fonts guide's sketch: the first moment of a page in a browser, played again on a press. Without
 * `PantrixTypefaceGate` the text is drawn at once in the browser's fallback face and jumps when the app's own arrives;
 * with it the page waits for the face and is drawn once, in it.
 */
@Composable
internal fun WebFontsSketch() {
    val language = LocalLanguage.current
    var fix by rememberSaveable { mutableStateOf(true) }
    var load by remember { mutableIntStateOf(0) }
    var phase by remember { mutableStateOf(Phase.Loaded) }
    LaunchedEffect(load, fix) {
        if (load == 0) return@LaunchedEffect
        phase = if (fix) Phase.Waiting else Phase.Fallback
        delay(FONT_ARRIVES)
        phase = Phase.Loaded
    }
    SketchFigure(caption = ToolSay.FontsCaption.of(language)) {
        WindowFrame(
            title = SiteAddress.host,
            modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth(),
            height = 180.dp
        ) {
            if (phase != Phase.Waiting) {
                val family = if (phase == Phase.Fallback) FontFamily.Serif else null
                Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PantrixText(
                        text = ToolSay.Heading.of(language),
                        style = PantrixTheme.typography.headline.let {
                            if (family !=
                                null
                            ) {
                                it.copy(fontFamily = family)
                            } else {
                                it
                            }
                        }
                    )
                    PantrixText(
                        text = ToolSay.Body.of(language),
                        style = PantrixTheme.typography.body.let {
                            if (family !=
                                null
                            ) {
                                it.copy(fontFamily = family)
                            } else {
                                it
                            }
                        },
                        color = PantrixTheme.colors.textSecondary
                    )
                }
            }
        }
        SketchControls {
            PantrixButton(
                text = ToolSay.Reload.of(language),
                onClick = { load++ },
                variant = ButtonVariant.Outline,
                tagKey = "sketch_reload"
            )
            SketchSwitch(code = "PantrixTypefaceGate", checked = fix, onToggle = { fix = !fix }, tag = "sketch_gate")
            SketchNote(
                when (phase) {
                    Phase.Waiting -> ToolSay.Waiting

                    Phase.Fallback -> ToolSay.Fallback

                    Phase.Loaded -> if (load == 0) {
                        ToolSay.FontsStart
                    } else if (fix) {
                        ToolSay.Once
                    } else {
                        ToolSay.Jumped
                    }
                }.of(language)
            )
        }
    }
}

/** Where a page's first moment is: waiting for its face, drawn in the fallback, or drawn in its own. */
private enum class Phase { Waiting, Fallback, Loaded }

/** How long the app's own face takes to come, as the guide measured it on a fast line. */
private const val FONT_ARRIVES = 900L

/**
 * The AI guide's sketch: an assistant asked for a sign-up screen. Without Pantrix's MCP server it guesses, and writes
 * Material with words in the code and no tags; with it, it asks the library and writes Pantrix's components.
 */
@Composable
internal fun AiSketch() {
    val language = LocalLanguage.current
    var fix by rememberSaveable { mutableStateOf(true) }
    SketchFigure(caption = ToolSay.AiCaption.of(language)) {
        WindowFrame(title = "Claude Code", modifier = Modifier.widthIn(max = 640.dp).fillMaxWidth(), height = null) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PantrixText(
                    text = ToolSay.Ask.of(language),
                    style = SiteTheme.type.bodySmall,
                    color = PantrixTheme.colors.primary.onContainer,
                    modifier = Modifier
                        .align(Alignment.End)
                        .background(PantrixTheme.colors.primary.container, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                )
                CodeView(plainCode(if (fix) WITH_SERVER else GUESSED), compact = true)
            }
        }
        SketchControls {
            SketchSwitch(code = "pantrix-ui MCP", checked = fix, onToggle = { fix = !fix }, tag = "sketch_mcp")
            SketchNote((if (fix) ToolSay.Asked else ToolSay.Guessed).of(language))
        }
    }
}

/** [code] as a block the site's code view draws, every line plain. */
private fun plainCode(code: String) = Block.Code(
    language = "kotlin",
    lines = code.trimIndent().lines().map {
        listOf(Token(it))
    }
)

private const val WITH_SERVER = """
    @Composable
    fun SignUpScreen(onSignUp: (String) -> Unit) {
        val email = rememberTextFieldState()
        Column(verticalArrangement = Arrangement.spacedBy(PantrixTheme.spacing.md)) {
            PantrixTextField(state = email, label = stringResource(Res.string.email), tagKey = "email")
            PantrixButton(
                text = stringResource(Res.string.sign_up),
                onClick = { onSignUp(email.text.toString()) },
                tagKey = "sign_up"
            )
        }
    }
"""

private const val GUESSED = """
    @Composable
    fun SignUpScreen(onSignUp: (String) -> Unit) {
        var email by remember { mutableStateOf("") }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("E-mail") })
            Button(onClick = { onSignUp(email) }) { Text("Sign up") }
        }
    }
"""

/**
 * The analytics guide's sketch: a checkout whose components report what the reader does to the app's sink, listed
 * beside it as the product would get them. A release build's `TestTagPolicy.Off` takes the tags out of the screen's
 * semantics; the events go on carrying them.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AnalyticsSketch() {
    val language = LocalLanguage.current
    var release by rememberSaveable { mutableStateOf(false) }
    var gift by rememberSaveable { mutableStateOf(false) }
    val events = remember { mutableStateListOf<PantrixUiEvent>() }
    val sink = remember { PantrixAnalytics { event -> events.add(0, event) } }
    val code = rememberTextFieldState()
    SketchFigure(caption = ToolSay.AnalyticsCaption.of(language)) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PhoneFrame {
                PantrixTheme(
                    analytics = sink,
                    testTagPolicy = if (release) TestTagPolicy.Off else TestTagPolicy.Stable
                ) {
                    SketchPhoneScreen(app = ToolSay.Shop.of(language), title = ToolSay.Checkout.of(language)) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            PantrixTextField(
                                state = code,
                                label = ToolSay.Promo.of(language),
                                modifier = Modifier.fillMaxWidth(),
                                tagKey = "promo"
                            )
                            PantrixSwitchButton(
                                checked = gift,
                                onCheckedChange = { gift = it },
                                label = ToolSay.Gift.of(language),
                                tagKey = "gift_wrap"
                            )
                            PantrixButton(
                                text = ToolSay.Pay.of(language),
                                onClick = {},
                                fullWidth = true,
                                tagKey = "pay"
                            )
                        }
                    }
                }
            }
            EventLog(events)
        }
        SketchControls {
            SketchSwitch(code = "TestTagPolicy.Off", checked = release, onToggle = {
                release = !release
            }, tag = "sketch_release")
            SketchNote((if (release) ToolSay.Release else ToolSay.Debug).of(language))
        }
    }
}

@Composable
private fun EventLog(events: List<PantrixUiEvent>) {
    val language = LocalLanguage.current
    val shape = RoundedCornerShape(14.dp)
    Column(
        Modifier
            .width(EVENT_LOG)
            .height(if (SiteTheme.layout == SiteLayout.Phone) 220.dp else 452.dp)
            .border(1.dp, PantrixTheme.colors.border, shape)
            .background(SiteTheme.colors.code.background, shape)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PantrixText(
            text = ToolSay.Events.of(language).inCapitals(),
            style = SiteTheme.type.overline,
            color = SiteTheme.colors.code.comment
        )
        if (events.isEmpty()) {
            PantrixText(
                text = ToolSay.NoEvents.of(language),
                style = SiteTheme.type.bodySmall,
                color = SiteTheme.colors.code.comment
            )
        }
        events.take(MAX_EVENTS).forEach { event ->
            PantrixText(
                text = "${event.component} · ${event.action} · ${event.testTag ?: "–"}",
                style = SiteTheme.type.inlineCode.sized(11.5.sp),
                color = SiteTheme.colors.code.text
            )
        }
    }
}

private val EVENT_LOG = 260.dp
private const val MAX_EVENTS = 12

/**
 * The chart export guide's sketch: Pantrix's own pie chart with its export menu, and the file the reader asked for:
 * its name, its type and its size, made on this page. What happens to the file then is the app's: here, nothing.
 */
@Composable
internal fun ChartExportSketch() {
    val language = LocalLanguage.current
    var file by remember { mutableStateOf<ChartExportFile?>(null) }
    val export = remember { ChartExport(fileName = "orders") { file = it } }
    SketchFigure(caption = ToolSay.ChartCaption.of(language)) {
        PantrixCard(variant = CardVariant.Outlined, modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth()) {
            PantrixPieChart(
                slices = listOf(
                    PieSlice("web", ToolSay.Web.of(language), 42),
                    PieSlice("ios", "iOS", 31),
                    PieSlice("android", "Android", 27)
                ),
                hole = 0.55f,
                modifier = Modifier.fillMaxWidth().height(220.dp),
                export = export,
                tagKey = "sketch_orders"
            )
        }
        SketchControls {
            SketchNote(
                file?.let { made ->
                    "${made.name} · ${made.mimeType} · ${PantrixTheme.strings.fileSize(made.bytes.size.toLong())}"
                } ?: ToolSay.ChartStart.of(language)
            )
        }
    }
}

/**
 * The detekt guide's sketch: a screen's code and what Pantrix's rules find in it, `NoMaterialImport` and
 * `FixedSideLayout`, and the same code once each finding is put right.
 */
@Composable
internal fun DetektSketch() {
    val language = LocalLanguage.current
    var fixed by rememberSaveable { mutableStateOf(false) }
    SketchFigure(caption = ToolSay.DetektCaption.of(language)) {
        Column(Modifier.widthIn(max = 640.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            CodeView(plainCode(if (fixed) DETEKT_AFTER else DETEKT_BEFORE), compact = true)
            if (!fixed) {
                Finding("NoMaterialImport", ToolSay.Material.of(language))
                Finding("FixedSideLayout", ToolSay.Side.of(language))
            }
        }
        SketchControls {
            SketchSwitch(code = "detekt", checked = fixed, onToggle = { fixed = !fixed }, tag = "sketch_detekt")
            SketchNote((if (fixed) ToolSay.Clean else ToolSay.Found).of(language))
        }
    }
}

@Composable
private fun Finding(
    rule: String,
    text: String
) {
    val warning = PantrixTheme.colors.warning
    Row(
        Modifier.fillMaxWidth().background(warning.container, RoundedCornerShape(10.dp)).padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        PantrixText(text = rule, style = SiteTheme.type.inlineCode.sized(12.5.sp), color = warning.onContainer)
        PantrixText(
            text = text,
            style = SiteTheme.type.bodySmall,
            color = warning.onContainer,
            modifier = Modifier.weight(1f)
        )
    }
}

/** The shape of a colour's sample in the Material 3 sketch. */
private val SWATCH = RoundedCornerShape(6.dp)

private const val DETEKT_BEFORE = """
    import androidx.compose.material3.Text

    @Composable
    fun Total(amount: String) {
        Text(text = amount, modifier = Modifier.absolutePadding(left = 16.dp))
    }
"""

private const val DETEKT_AFTER = """
    import com.pantrixui.components.elements.text.PantrixText

    @Composable
    fun Total(amount: String) {
        PantrixText(text = amount, modifier = Modifier.padding(start = 16.dp))
    }
"""

/**
 * The Material 3 guide's sketch: this page's own theme as the bridge hands it to Material, each of Pantrix's roles
 * beside the colour scheme's name it becomes, in the page's light or dark (`toColorScheme()`). Turned the other way, a
 * Material scheme's names and the roles they become (`toPantrixColors()`), each in the colour it takes here; the two
 * are not each other's mirror, since a scheme has no name for some of Pantrix's roles.
 */
@Composable
internal fun Material3Sketch() {
    val language = LocalLanguage.current
    var toMaterial by rememberSaveable { mutableStateOf(true) }
    val colors = PantrixTheme.colors
    val pairs = if (toMaterial) {
        listOf(
            Triple("primary.color", "primary", colors.primary.color),
            Triple("primary.container", "primaryContainer", colors.primary.container),
            Triple("danger.color", "error", colors.danger.color),
            Triple("neutral.color", "secondary", colors.neutral.color),
            Triple("info.color", "tertiary", colors.info.color),
            Triple("background", "background, surface", colors.background),
            Triple("surface", "surfaceContainerLow", colors.surface),
            Triple("textPrimary", "onBackground", colors.textPrimary),
            Triple("textSecondary", "onSurfaceVariant", colors.textSecondary),
            Triple("borderStrong", "outline", colors.borderStrong),
            Triple("border", "outlineVariant", colors.border)
        )
    } else {
        listOf(
            Triple("primary", "primary.color", colors.primary.color),
            Triple("primaryContainer", "primary.container", colors.primary.container),
            Triple("error", "danger.color", colors.danger.color),
            Triple("secondary", "neutral.color", colors.neutral.color),
            Triple("background", "background", colors.background),
            Triple("surfaceContainerLow", "surface", colors.surface),
            Triple("onBackground", "textPrimary", colors.textPrimary),
            Triple("onSurfaceVariant", "textSecondary", colors.textSecondary),
            Triple("outline", "border", colors.border),
            Triple("outline", "borderStrong", colors.borderStrong),
            Triple("outlineVariant", "divider", colors.divider)
        )
    }
    SketchFigure(caption = ToolSay.BridgeCaption.of(language)) {
        Column(Modifier.widthIn(max = 520.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            pairs.forEach { (from, to, color) ->
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(Modifier.size(22.dp).background(color, SWATCH).border(1.dp, colors.border, SWATCH))
                    PantrixText(
                        text = from,
                        style = SiteTheme.type.inlineCode.sized(12.5.sp),
                        color = colors.textPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    PantrixText(text = "→", color = SiteTheme.colors.muted)
                    PantrixText(
                        text = to,
                        style = SiteTheme.type.inlineCode.sized(12.5.sp),
                        color = colors.textSecondary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        SketchControls {
            SketchChoice(
                options = listOf(true to "toColorScheme()", false to "toPantrixColors()"),
                selected = toMaterial,
                onSelect = { toMaterial = it },
                tag = "sketch_direction"
            )
            SketchNote((if (toMaterial) ToolSay.ToMaterial else ToolSay.ToPantrix).of(language))
        }
    }
}

/** The tool sketches' words, in each language. */
private object ToolSay {
    val Heading = Words("Your order is on its way", "Siparişin yolda")
    val Body = Words(
        "It left the store at 14:05 and arrives between 15:30 and 16:00.",
        "Mağazadan 14:05'te çıktı, 15:30 ile 16:00 arasında gelir."
    )
    val Reload = Words("Load the page again", "Sayfayı yeniden yükle")
    val FontsStart =
        Words("Load the page again to see its first moment.", "İlk anını görmek için sayfayı yeniden yükle.")
    val Waiting =
        Words(
            "The gate waits for the face: nothing is drawn yet.",
            "Kapı yazı tipini bekliyor: henüz bir şey çizilmedi."
        )
    val Fallback =
        Words("Drawn at once in the browser’s fallback face…", "Hemen tarayıcının yedek yazı tipiyle çizildi…")
    val Jumped = Words(
        "…and drawn again when the app’s face came: the text jumped before the reader’s eyes.",
        "…ve uygulamanın yazı tipi gelince yeniden çizildi: yazı okurun gözü önünde sıçradı."
    )
    val Once =
        Words(
            "Drawn once, in the app’s own face: nothing moved.",
            "Bir kez, uygulamanın kendi yazı tipiyle çizildi: hiçbir şey kımıldamadı."
        )
    val FontsCaption = Words(
        "Pantrix’s own text, with a page’s first moment played again; the browser’s fallback face is drawn as a " +
            "serif.",
        "Pantrix'in kendi yazısı, bir sayfanın ilk anı yeniden oynatılıyor; tarayıcının yedek yazı tipi tırnaklı " +
            "çizildi."
    )
    val Ask =
        Words(
            "Write a sign-up screen with an e-mail field and a button.",
            "E-posta alanı ve bir düğmesi olan bir kayıt ekranı yaz."
        )
    val Asked = Words(
        "The assistant asked the server: Pantrix’s components, words from resources, a tag on every control.",
        "Asistan sunucuya sordu: Pantrix'in bileşenleri, kaynaklardan sözcükler, her kontrolde bir etiket."
    )
    val Guessed = Words(
        "Without the server it guessed: Material’s components, words in the code, no tags.",
        "Sunucu olmadan tahmin etti: Material'ın bileşenleri, kodun içinde sözcükler, etiket yok."
    )
    val AiCaption =
        Words(
            "Two answers, as an assistant writes them; nothing is sent from this page.",
            "Bir asistanın yazdığı iki yanıt; bu sayfadan hiçbir şey gönderilmiyor."
        )
    val Shop = Words("Shop", "Mağaza")
    val Checkout = Words("Checkout", "Ödeme")
    val Promo = Words("Promo code", "İndirim kodu")
    val Gift = Words("Gift wrap", "Hediye paketi")
    val Pay = Words("Pay", "Öde")
    val Events = Words("Events", "Olaylar")
    val NoEvents = Words("Press, type or switch on the phone.", "Telefonda bas, yaz ya da anahtarı çevir.")
    val Debug = Words(
        "Each event names its component, what was done and its tag. What was typed is never in it.",
        "Her olay bileşenini, yapılanı ve etiketini anar. Yazılan hiçbir zaman içinde değildir."
    )
    val Release = Words(
        "A release build takes the tags out of the screen; the events still carry them to the product.",
        "Bir yayın derlemesi etiketleri ekrandan çıkarır; olaylar onları ürüne taşımaya devam eder."
    )
    val AnalyticsCaption = Words(
        "Pantrix’s own components and analytics sink, running on this page; nothing leaves it.",
        "Pantrix'in kendi bileşenleri ve analitik alıcısı bu sayfada çalışıyor; hiçbir şey dışarı çıkmıyor."
    )
    val Web = Words("Web", "Web")
    val ChartStart = Words(
        "Open the chart’s menu and export it: the file is made here.",
        "Grafiğin menüsünü aç ve dışa aktar: dosya burada yapılır."
    )
    val ChartCaption = Words(
        "Pantrix’s own chart and its export, running on this page; the file stays here.",
        "Pantrix'in kendi grafiği ve dışa aktarması bu sayfada çalışıyor; dosya burada kalıyor."
    )
    val Material = Words("Material’s Text, in place of PantrixText.", "PantrixText yerine Material'ın Text'i.")
    val Side = Words(
        "A padding on the left stays on the left in a language read from right to left: start.",
        "Soldaki bir boşluk sağdan sola okunan bir dilde de solda kalır: start."
    )
    val Found =
        Words(
            "Two findings: each is put right, or suppressed with its reason.",
            "İki bulgu: her biri düzeltilir ya da gerekçesiyle bastırılır."
        )
    val Clean = Words("Put right: PantrixText, and padding at the start.", "Düzeltildi: PantrixText ve baştaki boşluk.")
    val DetektCaption =
        Words(
            "The rules of com.pantrixui:detekt-rules, as they find and as they are put right.",
            "com.pantrixui:detekt-rules'un kuralları, buldukları ve düzeltilişleriyle."
        )
    val ToMaterial = Words(
        "This page’s own colours, as the bridge hands them to a Material screen inside it.",
        "Bu sayfanın kendi renkleri, köprünün içindeki bir Material ekranına verdiği gibi."
    )
    val ToPantrix = Words(
        "A Material scheme’s names, and the roles of Pantrix they become in a Material app; success, info and " +
            "warning have no name there and stay Pantrix’s own.",
        "Bir Material şemasının adları ve bir Material uygulamasında Pantrix'in hangi rolleri oldukları; başarı, " +
            "bilgi ve uyarının orada adı yok, Pantrix'in kendileri kalıyor."
    )
    val BridgeCaption =
        Words("This page’s own theme, read by its roles.", "Bu sayfanın kendi teması, rolleriyle okunuyor.")
}
