package com.pantrixui.site.feature.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.container.alert.AlertTone
import com.pantrixui.components.container.alert.PantrixAlert
import com.pantrixui.components.data.tag.PantrixTag
import com.pantrixui.components.data.tag.TagStyle
import com.pantrixui.components.data.tag.TagTone
import com.pantrixui.components.elements.button.ButtonVariant
import com.pantrixui.components.elements.button.PantrixButton
import com.pantrixui.components.elements.chip.PantrixChip
import com.pantrixui.components.elements.icon.PantrixIcon
import com.pantrixui.components.elements.link.PantrixLink
import com.pantrixui.components.elements.segmentbutton.PantrixSegmentButton
import com.pantrixui.components.elements.segmentbutton.SegmentOption
import com.pantrixui.components.elements.switchbutton.PantrixSwitchButton
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.components.inputs.textfield.PantrixTextField
import com.pantrixui.foundation.a11y.PantrixPlatform
import com.pantrixui.foundation.icons.PantrixIcons
import com.pantrixui.foundation.platform.PlatformType
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.foundation.token.shape.ShapeRole
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.content.live
import com.pantrixui.site.core.designsystem.content.siteName
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.model.Language

/**
 * The preview: the platform and the theme it is drawn in over a checkout drawn by Pantrix's own components in a theme
 * of the preset being made, as an app would wear it.
 */
@Composable
internal fun Preview(
    state: ThemeUiState,
    onAction: (ThemeAction) -> Unit,
    modifier: Modifier
) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val phone = layout == SiteLayout.Phone
    val line = SiteTheme.colors.hairline
    Column(modifier.live().background(SiteTheme.colors.canvas)) {
        val platforms: @Composable (Modifier) -> Unit = { segmentModifier ->
            PantrixSegmentButton(
                options = PlatformType.entries.map { SegmentOption(it, it.siteName, tagKey = it.name.lowercase()) },
                selected = state.platform,
                onSelect = { onAction(ThemeAction.PickPlatform(it)) },
                modifier = segmentModifier,
                fullWidth = phone,
                tagKey = "previewPlatform"
            )
        }
        val modes: @Composable () -> Unit = {
            PantrixSegmentButton(
                options = listOf(
                    SegmentOption(false, ThemeSay.Light.of(language), tagKey = "light"),
                    SegmentOption(true, ThemeSay.Dark.of(language), tagKey = "dark")
                ),
                selected = state.dark,
                onSelect = { onAction(ThemeAction.ShowDark(it)) },
                fullWidth = false,
                tagKey = "previewMode"
            )
        }
        if (phone) {
            Column(
                Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                platforms(Modifier.fillMaxWidth())
                modes()
            }
        } else {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(PantrixTheme.colors.surface)
                    .drawBehind {
                        drawLine(line, Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx())
                    }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                platforms(Modifier)
                modes()
            }
        }
        Box(
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = if (phone) 16.dp else 20.dp,
                    vertical = when (layout) {
                        SiteLayout.Desktop -> 28.dp
                        SiteLayout.Tablet -> 24.dp
                        SiteLayout.Phone -> 14.dp
                    }
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            val host = PantrixTheme.platform
            val preset = remember(state) { BuiltPreset(state) }
            PantrixTheme(
                preset = preset,
                colors = preset.colors(state.dark),
                density = state.density,
                platform = PantrixPlatform.forType(state.platform, isTouch = host.isTouch),
                followSystemBars = false
            ) {
                Checkout(language, Modifier.widthIn(max = if (layout == SiteLayout.Desktop) 440.dp else 420.dp))
            }
        }
    }
}

/** A checkout, in the preset: its bar, a note, an address, where to deliver, the extras, the total and the pay. */
@Composable
private fun Checkout(
    language: Language,
    modifier: Modifier
) {
    val colors = PantrixTheme.colors
    val shape = PantrixTheme.shapes.xl
    val type = PantrixTheme.typography
    var place by rememberSaveable { mutableStateOf(0) }
    var neighbour by rememberSaveable { mutableStateOf(true) }
    var gift by rememberSaveable { mutableStateOf(true) }
    var note by rememberSaveable { mutableStateOf(false) }
    var address by remember(language) { mutableStateOf(ThemeSay.AddressValue.of(language)) }
    val line = colors.divider
    Column(
        modifier
            .fillMaxWidth()
            .shadow(CARD_SHADOW, shape, clip = false)
            .clip(shape)
            .background(colors.surface)
            .border(1.dp, colors.divider, shape)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .drawBehind { drawLine(line, Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx()) }
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PantrixIcon(imageVector = PantrixIcons.ChevronBack, contentDescription = null, size = 22.dp)
            PantrixText(
                text = ThemeSay.Checkout.of(language),
                style = type.title.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                color = colors.textPrimary,
                modifier = Modifier.weight(1f)
            )
            PantrixTag(
                text = ThemeSay.Secure.of(language),
                tone = TagTone.Success,
                style = TagStyle(shape = ShapeRole.Small),
                tagKey = "previewSecure"
            )
        }
        Column(
            Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(PantrixTheme.spacing.lg)
        ) {
            PantrixAlert(
                message = ThemeSay.Leaves.of(language),
                tone = AlertTone.Primary,
                icon = PantrixIcons.Info,
                announce = false,
                tagKey = "previewNote"
            )
            PantrixTextField(
                value = address,
                onValueChange = { address = it },
                label = ThemeSay.Address.of(language),
                modifier = Modifier.fillMaxWidth(),
                tagKey = "previewAddress"
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                PantrixText(
                    text = ThemeSay.DeliverTo.of(language),
                    style = type.label.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.textPrimary
                )
                PantrixSegmentButton(
                    options = ThemeSay.Places.mapIndexed { index, words ->
                        SegmentOption(index, words.of(language), tagKey = "place$index")
                    },
                    selected = place,
                    onSelect = { place = it },
                    modifier = Modifier.fillMaxWidth(),
                    tagKey = "previewPlace"
                )
            }
            PantrixSwitchButton(
                checked = neighbour,
                onCheckedChange = { neighbour = it },
                label = ThemeSay.Neighbour.of(language),
                supportingText = ThemeSay.NobodyAnswers.of(language),
                tagKey = "previewNeighbour"
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PantrixChip(
                    label = ThemeSay.GiftWrap.of(language),
                    selected = gift,
                    onSelectedChange = { gift = it },
                    tagKey = "previewGift"
                )
                PantrixChip(
                    label = ThemeSay.NoteInside.of(language),
                    selected = note,
                    onSelectedChange = { note = it },
                    tagKey = "previewNoteInside"
                )
            }
            val amount = price(language)
            Row(
                Modifier
                    .fillMaxWidth()
                    .drawBehind { drawLine(line, Offset.Zero, Offset(size.width, 0f), 1.dp.toPx()) }
                    .padding(top = 6.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                PantrixText(
                    text = ThemeSay.Total.of(language),
                    style = type.body,
                    color = colors.textSecondary,
                    modifier = Modifier.weight(1f)
                )
                PantrixText(
                    text = amount,
                    style = type.title.copy(fontSize = 22.sp, fontWeight = FontWeight.ExtraBold),
                    color = colors.textPrimary
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PantrixButton(text = ThemeSay.Back.of(language), onClick = {
                }, variant = ButtonVariant.Outline, tagKey = "previewBack")
                PantrixButton(
                    text = "${ThemeSay.Pay.of(language)} $amount",
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    tagKey = "previewPay"
                )
            }
            PantrixLink(
                text = ThemeSay.Terms.of(language),
                onClick = {},
                modifier = Modifier.align(Alignment.CenterHorizontally),
                tagKey = "previewTerms"
            )
        }
    }
}

/** €84.00, written as [language] writes an amount of euros: 84,00 € in Turkish. */
@Composable
private fun price(language: Language): String {
    val amount = PantrixTheme.strings.decimal(PRICE, fractionDigits = 2, minFractionDigits = 2)
    return if (language == Language.Turkish) "$amount €" else "€$amount"
}

private val CARD_SHADOW = 12.dp
private const val PRICE = 84
