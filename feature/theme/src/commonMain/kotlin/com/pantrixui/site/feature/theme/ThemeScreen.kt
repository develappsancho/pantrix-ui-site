package com.pantrixui.site.feature.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.elements.button.ButtonSize
import com.pantrixui.components.elements.button.ButtonSizeStyle
import com.pantrixui.components.elements.button.ButtonStyle
import com.pantrixui.components.elements.button.ButtonVariant
import com.pantrixui.components.elements.button.PantrixButton
import com.pantrixui.components.elements.icon.PantrixIcon
import com.pantrixui.components.elements.scrollbar.pantrixVerticalScroll
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.components.overlay.sheet.PantrixSheet
import com.pantrixui.foundation.files.PantrixFile
import com.pantrixui.foundation.files.rememberFileSaver
import com.pantrixui.foundation.icons.PantrixIcons
import com.pantrixui.foundation.platform.PantrixWindowTitle
import com.pantrixui.foundation.platform.rememberPantrixClipboard
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.base.BaseScreen
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.content.CodeView
import com.pantrixui.site.core.i18n.LandingHeads
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.model.Block
import org.koin.compose.viewmodel.koinViewModel

/** The theme builder, with its model: a brand's preset made by choosing, shown live, and taken away as Kotlin. */
@Composable
fun ThemeRoute() {
    val viewModel = koinViewModel<ThemeViewModel>()
    val clipboard = rememberPantrixClipboard()
    val saver = rememberFileSaver()
    BaseScreen(
        viewModel = viewModel,
        onEffect = { effect ->
            when (effect) {
                is ThemeEffect.Copy -> viewModel.onAction(ThemeAction.Copied(clipboard.copyText(effect.text)))

                // What became of the file is the platform's to show: its window, or the browser's downloads.
                is ThemeEffect.Save -> saver.save(PantrixFile(effect.name, effect.text.encodeToByteArray(), KOTLIN)) {}
            }
        }
    ) { state, onAction -> ThemeScreen(state, onAction) }
}

/**
 * The theme builder, drawn from its [state]: what the preset is made of beside its live preview and its Kotlin on a
 * desktop; the choices beside the preview and the Kotlin under them on a tablet; the preview over the choices, and
 * the Kotlin in a sheet, on a phone.
 *
 * @param state what the builder's model holds.
 * @param onAction where what the user does goes.
 * @param modifier the screen's modifier.
 */
@Composable
fun ThemeScreen(
    state: ThemeUiState,
    onAction: (ThemeAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val language = LocalLanguage.current
    PantrixWindowTitle(LandingHeads.Theme.title.of(language))
    Column(modifier.fillMaxSize().background(PantrixTheme.colors.background)) {
        PageBar(state, onAction)
        when (SiteTheme.layout) {
            SiteLayout.Desktop -> Row(Modifier.weight(1f).fillMaxWidth()) {
                Controls(
                    state,
                    onAction,
                    Modifier.width(CONTROLS_WIDTH).fillMaxHeight().pantrixVerticalScroll(rememberScrollState())
                )
                Preview(state, onAction, Modifier.weight(1f).fillMaxHeight())
                CodePanel(state, Modifier.width(CODE_WIDTH).fillMaxHeight())
            }

            SiteLayout.Tablet -> Column(
                Modifier.weight(1f).fillMaxWidth().pantrixVerticalScroll(rememberScrollState())
            ) {
                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Max)) {
                    Controls(state, onAction, Modifier.width(TABLET_CONTROLS_WIDTH).fillMaxHeight())
                    Preview(state, onAction, Modifier.weight(1f).fillMaxHeight())
                }
                CodePanel(state, Modifier.fillMaxWidth())
            }

            SiteLayout.Phone -> Box(Modifier.weight(1f).fillMaxWidth()) {
                Column(Modifier.fillMaxSize().pantrixVerticalScroll(rememberScrollState())) {
                    Preview(state, onAction, Modifier.fillMaxWidth())
                    Controls(state, onAction, Modifier.fillMaxWidth())
                    // Room under the choices for the bar that stands over the page's foot.
                    Spacer(Modifier.height(PHONE_BAR_ROOM))
                }
                PhoneBar(state, onAction, Modifier.align(Alignment.BottomCenter))
                CodeSheet(state, onAction)
            }
        }
    }
}

/** The page's own bar under the site's header: what it is, and starting again and copying the Kotlin. */
@Composable
private fun PageBar(
    state: ThemeUiState,
    onAction: (ThemeAction) -> Unit
) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val phone = layout == SiteLayout.Phone
    val line = SiteTheme.colors.hairline
    Row(
        Modifier
            .fillMaxWidth()
            .drawBehind { drawLine(line, Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx()) }
            .padding(horizontal = if (phone) 16.dp else 20.dp, vertical = if (phone) 10.dp else 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            PantrixText(
                text = ThemeSay.Title.of(language),
                style = SiteTheme.type.cardTitle,
                color = PantrixTheme.colors.textPrimary,
                modifier = Modifier.semantics { heading() }
            )
            if (!phone) {
                PantrixText(
                    text = buildAnnotatedString {
                        append(ThemeSay.SubtitleStart.of(language))
                        withStyle(SpanStyle(fontFamily = SiteTheme.type.inlineCode.fontFamily)) {
                            append("DesignPreset")
                        }
                        append(ThemeSay.SubtitleEnd.of(language))
                    },
                    style = SiteTheme.type.bodySmall.copy(fontSize = 12.5.sp),
                    color = SiteTheme.colors.muted
                )
            }
        }
        if (phone) {
            PantrixButton(
                onClick = { onAction(ThemeAction.Reset) },
                contentDescription = ThemeSay.StartAgain.of(language),
                variant = ButtonVariant.Text,
                tagKey = "themeReset"
            ) {
                PantrixIcon(imageVector = PantrixIcons.Refresh, contentDescription = null, size = 20.dp)
            }
        } else {
            BarButton(ThemeSay.StartAgain.of(language), "themeReset", ButtonVariant.Text) {
                onAction(ThemeAction.Reset)
            }
            BarButton(
                text = (if (state.copied) ThemeSay.Copied else ThemeSay.CopyKotlin).of(language),
                tag = "themeCopy",
                variant = ButtonVariant.Outline,
                icon = PantrixIcons.Copy
            ) { onAction(ThemeAction.CopyKotlin) }
            BarButton(
                text = ThemeSay.download(state.fileName).of(language),
                tag = "themeDownload",
                variant = ButtonVariant.Solid,
                icon = PantrixIcons.Download
            ) { onAction(ThemeAction.Download) }
        }
    }
}

@Composable
private fun BarButton(
    text: String,
    tag: String,
    variant: ButtonVariant,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    PantrixButton(
        onClick = onClick,
        contentDescription = text,
        variant = variant,
        size = ButtonSize.Medium,
        style = ButtonStyle(medium = ButtonSizeStyle(height = 36.dp, horizontalPadding = 14.dp)),
        shape = RoundedCornerShape(10.dp),
        tagKey = tag
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) PantrixIcon(imageVector = icon, contentDescription = null, size = 15.dp)
            PantrixText(text = text, style = SiteTheme.type.button)
        }
    }
}

/** The preset's Kotlin on its dark panel: the file's name over it, and the call that wears it under it. */
@Composable
private fun CodePanel(
    state: ThemeUiState,
    modifier: Modifier
) {
    val code = SiteTheme.colors.code
    val desktop = SiteTheme.layout == SiteLayout.Desktop
    Column(modifier.background(code.background)) {
        val block = Block.Code("kotlin", file = SOURCE_SET, lines = presetCode(state))
        val scrolled = if (desktop) Modifier.weight(1f).pantrixVerticalScroll(rememberScrollState()) else Modifier
        Box(scrolled) {
            CodeView(code = block, tabs = listOf(state.fileName), shape = RectangleShape)
        }
        Then(state, Modifier.fillMaxWidth())
    }
    // A tablet's panel ends the page: it keeps clear of the system's bottom bar.
    if (!desktop) Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
}

/** "Then: PantrixTheme(preset = …, density = …)": how an app wears the preset. */
@Composable
internal fun Then(
    state: ThemeUiState,
    modifier: Modifier
) {
    val language = LocalLanguage.current
    val code = SiteTheme.colors.code
    val line = code.line
    PantrixText(
        text = buildAnnotatedString {
            append(ThemeSay.Then.of(language))
            withStyle(SpanStyle(fontFamily = SiteTheme.type.inlineCode.fontFamily, color = code.text)) {
                append("PantrixTheme(preset = ${state.objectName}, density = PantrixDensity.${state.density.name})")
            }
        },
        style = SiteTheme.type.bodySmall.copy(fontSize = 12.5.sp),
        color = code.comment,
        modifier = modifier
            .drawBehind { drawLine(line, Offset.Zero, Offset(size.width, 0f), 1.dp.toPx()) }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

/** A phone's bar over the page's foot: the Kotlin to see, and to copy. */
@Composable
private fun PhoneBar(
    state: ThemeUiState,
    onAction: (ThemeAction) -> Unit,
    modifier: Modifier
) {
    val language = LocalLanguage.current
    val line = SiteTheme.colors.hairline
    Row(
        modifier
            .fillMaxWidth()
            .background(PantrixTheme.colors.background)
            .drawBehind { drawLine(line, Offset.Zero, Offset(size.width, 0f), 1.dp.toPx()) }
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        PhoneButton(ThemeSay.SeeTheKotlin.of(language), "themeSeeCode", primary = false, Modifier.weight(1f)) {
            onAction(ThemeAction.ShowCode(true))
        }
        PhoneButton(
            text = (if (state.copied) ThemeSay.Copied else ThemeSay.CopyKotlin).of(language),
            tag = "themeCopy",
            primary = true,
            modifier = Modifier.weight(1f)
        ) { onAction(ThemeAction.CopyKotlin) }
    }
}

@Composable
private fun PhoneButton(
    text: String,
    tag: String,
    primary: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    PantrixButton(
        onClick = onClick,
        contentDescription = text,
        modifier = modifier,
        variant = if (primary) ButtonVariant.Solid else ButtonVariant.Outline,
        size = ButtonSize.Medium,
        style = ButtonStyle(medium = ButtonSizeStyle(height = 50.dp)),
        shape = RoundedCornerShape(12.dp),
        tagKey = tag
    ) {
        PantrixText(text = text, style = SiteTheme.type.button.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold))
    }
}

/** A phone's sheet of the Kotlin, from the page's foot. */
@Composable
private fun CodeSheet(
    state: ThemeUiState,
    onAction: (ThemeAction) -> Unit
) {
    val language = LocalLanguage.current
    PantrixSheet(
        visible = state.codeOpen,
        onDismissRequest = { onAction(ThemeAction.ShowCode(false)) },
        title = state.fileName,
        tagKey = "themeCode"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            CodeView(code = Block.Code("kotlin", lines = presetCode(state)))
            Then(state, Modifier.fillMaxWidth().background(SiteTheme.colors.code.background, RoundedCornerShape(12.dp)))
            PhoneButton(
                text = (if (state.copied) ThemeSay.Copied else ThemeSay.CopyKotlin).of(language),
                tag = "themeSheetCopy",
                primary = true,
                modifier = Modifier.fillMaxWidth()
            ) { onAction(ThemeAction.CopyKotlin) }
        }
    }
}

private val CONTROLS_WIDTH = 340.dp
private val TABLET_CONTROLS_WIDTH = 300.dp
private val CODE_WIDTH = 440.dp
private val PHONE_BAR_ROOM = 96.dp
private const val SOURCE_SET = "commonMain"

/** What a file of Kotlin is, for the platform's window: text, which every platform shows and opens. */
private const val KOTLIN = "text/plain"
