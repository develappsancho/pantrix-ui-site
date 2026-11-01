package com.pantrixui.site.feature.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.container.alert.AlertTone
import com.pantrixui.components.container.alert.PantrixAlert
import com.pantrixui.components.elements.segmentbutton.PantrixSegmentButton
import com.pantrixui.components.elements.segmentbutton.SegmentButtonColors
import com.pantrixui.components.elements.segmentbutton.SegmentButtonStyle
import com.pantrixui.components.elements.segmentbutton.SegmentOption
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.components.inputs.textfield.PantrixTextField
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.foundation.token.color.contrastRatio
import com.pantrixui.foundation.token.density.PantrixDensity
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.siteClickable
import com.pantrixui.site.core.i18n.LocalLanguage

/** What the preset is made of: its name, its colour and what it reads as, its corners, its typeface, its density. */
@Composable
internal fun Controls(
    state: ThemeUiState,
    onAction: (ThemeAction) -> Unit,
    modifier: Modifier
) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val phone = layout == SiteLayout.Phone
    val line = SiteTheme.colors.hairline
    Column(
        modifier
            .then(if (phone) Modifier else Modifier.background(SiteTheme.colors.surfaceAlt))
            .then(
                if (phone) {
                    Modifier
                } else {
                    Modifier.drawBehind {
                        drawLine(line, Offset(size.width, 0f), Offset(size.width, size.height), 1.dp.toPx())
                    }
                }
            )
            .padding(horizontal = if (phone) 16.dp else 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        PantrixTextField(
            value = state.name,
            onValueChange = { onAction(ThemeAction.Name(it)) },
            label = ThemeSay.Name.of(language),
            modifier = Modifier.fillMaxWidth(),
            tagKey = "presetName"
        )
        Fieldset(ThemeSay.PrimaryColour.of(language)) {
            Swatches(state.primary) { onAction(ThemeAction.PickColour(it)) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                PantrixText(
                    text = ThemeSay.Hex.of(language),
                    style = SiteTheme.type.bodySmall,
                    color = SiteTheme.colors.muted
                )
                PantrixTextField(
                    value = state.hexInput,
                    onValueChange = { onAction(ThemeAction.Hex(it)) },
                    placeholder = "#2264FF",
                    contentDescription = ThemeSay.HexLabel.of(language),
                    modifier = Modifier.weight(1f),
                    tagKey = "presetHex"
                )
            }
            Readings(state)
        }
        Fieldset(ThemeSay.CornersTitle.of(language)) {
            Choice(Corners.entries, state.corners, { ThemeSay.corners(it).of(language) }, "presetCorners") {
                onAction(ThemeAction.PickCorners(it))
            }
            PantrixText(
                text = ThemeSay.cornersOf(state.corners).of(language) + ": " +
                    state.corners.radii.all.joinToString(" · ") + " dp",
                style = SiteTheme.type.inlineCode.copy(fontSize = 12.sp),
                color = SiteTheme.colors.muted
            )
        }
        Fieldset(ThemeSay.Typeface.of(language)) {
            Face.entries.forEach { face ->
                FaceOption(face, face == state.face) { onAction(ThemeAction.PickFace(face)) }
            }
            if (state.face == Face.Own) {
                PantrixText(
                    text = ThemeSay.OwnFaceNote.of(language),
                    style = SiteTheme.type.bodySmall.copy(fontSize = 12.5.sp),
                    color = SiteTheme.colors.muted
                )
            }
        }
        Fieldset(
            title = ThemeSay.Density.of(language),
            note = ThemeSay.DensityNote.of(language)
        ) {
            Choice(PantrixDensity.entries, state.density, { ThemeSay.density(it) }, "presetDensity") {
                onAction(ThemeAction.PickDensity(it))
            }
        }
    }
}

/** A group of the controls under its title, and a quieter note after the title where it has one. */
@Composable
private fun Fieldset(
    title: String,
    note: String? = null,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        PantrixText(
            text = buildAnnotatedString {
                append(title)
                if (note != null) {
                    withStyle(SpanStyle(color = SiteTheme.colors.muted, fontWeight = FontWeight.SemiBold)) {
                        append(note)
                    }
                }
            },
            style = SiteTheme.type.bodySmall.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
            color = PantrixTheme.colors.textPrimary
        )
        content()
    }
}

/** One of [options], [selected] marked, as Pantrix's segment button chooses. */
@Composable
private fun <T> Choice(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    tag: String,
    onSelect: (T) -> Unit
) {
    // Three long labels in a narrow column, set as the design sets them: smaller and bold, the chosen one in the text's
    // colour and the others in the muted one.
    PantrixSegmentButton(
        options = options.map { SegmentOption(it, label(it), tagKey = it.toString().lowercase()) },
        selected = selected,
        onSelect = onSelect,
        modifier = Modifier.fillMaxWidth(),
        fullWidth = true,
        // Three choices share the column: their labels keep a little room at their sides, not the platform's.
        style = SegmentButtonStyle(segmentPadding = CHOICE_PADDING),
        colors = SegmentButtonColors(
            selectedContent = PantrixTheme.colors.textPrimary,
            unselectedContent = SiteTheme.colors.muted
        ),
        tagKey = tag,
        labelStyle = PantrixTheme.typography.label.copy(fontSize = CHOICE_TEXT, fontWeight = FontWeight.Bold)
    )
}

/** The swatches to start from, the one chosen ringed. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Swatches(
    primary: Long,
    onPick: (Long) -> Unit
) {
    val language = LocalLanguage.current
    val phone = SiteTheme.layout == SiteLayout.Phone
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ThemeSay.Swatches.forEach { (argb, name) ->
            val shape = RoundedCornerShape(10.dp)
            val chosen = argb == primary
            Box(
                Modifier
                    .size(if (phone) 44.dp else 36.dp)
                    .border(2.dp, if (chosen) PantrixTheme.colors.textPrimary else Color.Transparent, shape)
                    .padding(3.dp)
                    .background(Color(argb), RoundedCornerShape(7.dp))
                    .semantics {
                        contentDescription = name.of(language)
                        selected = chosen
                    }
                    .siteClickable(tag = "swatch_${hexOf(argb).drop(1)}", role = Role.RadioButton, shape = shape) {
                        onPick(argb)
                    }
            )
        }
    }
}

/** What the colour reads as: the text the preset writes on it, and the colour as a link on white. */
@Composable
private fun Readings(state: ThemeUiState) {
    val language = LocalLanguage.current
    val primary = Color(state.primary)
    val role = primaryRole(primary, dark = false)
    val onWhite = role.onColor == Color.White
    val textOn = contrastRatio(role.onColor, role.color)
    val asLink = contrastRatio(primary, Color.White)
    val shape = RoundedCornerShape(10.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .background(PantrixTheme.colors.surface, shape)
            .border(1.dp, PantrixTheme.colors.border, shape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Reading(
            ThemeSay.TextOnIt.of(language) + (if (onWhite) ThemeSay.White else ThemeSay.NearBlack).of(language),
            textOn
        )
        Reading(ThemeSay.LinkOnWhite.of(language), asLink)
    }
    if (asLink < TEXT_CONTRAST) {
        PantrixAlert(
            message = ThemeSay.LinkWarning.of(language),
            tone = AlertTone.Warning,
            icon = null,
            tagKey = "linkWarning"
        )
    }
}

@Composable
private fun Reading(
    label: String,
    ratio: Float
) {
    val style = SiteTheme.type.bodySmall.copy(fontSize = 12.5.sp)
    Row(verticalAlignment = Alignment.CenterVertically) {
        PantrixText(
            text = label,
            style = style,
            color = PantrixTheme.colors.textSecondary,
            modifier = Modifier.weight(1f)
        )
        PantrixText(
            text = PantrixTheme.strings.decimal(ratio, fractionDigits = 1, minFractionDigits = 1) + ":1",
            style = style.copy(fontWeight = FontWeight.ExtraBold),
            color = if (ratio >= TEXT_CONTRAST) PantrixTheme.colors.success.color else PantrixTheme.colors.danger.color
        )
    }
}

/** A typeface to choose: its name, in it where the site has its files, and what it is. */
@Composable
private fun FaceOption(
    face: Face,
    chosen: Boolean,
    onPick: () -> Unit
) {
    val language = LocalLanguage.current
    val (name, note) = ThemeSay.face(face)
    val shape = RoundedCornerShape(10.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = if (SiteTheme.layout == SiteLayout.Phone) 48.dp else 44.dp)
            .background(if (chosen) PantrixTheme.colors.primary.container else PantrixTheme.colors.surface, shape)
            .border(1.dp, if (chosen) PantrixTheme.colors.primary.color else PantrixTheme.colors.border, shape)
            .semantics { selected = chosen }
            .siteClickable(
                tag = "face_${face.name.lowercase()}",
                role = Role.RadioButton,
                shape = shape,
                onClick = onPick
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PantrixText(
            text = name.of(language),
            style = SiteTheme.type.body.copy(fontWeight = FontWeight.Bold),
            color = PantrixTheme.colors.textPrimary,
            modifier = Modifier.weight(1f)
        )
        PantrixText(
            text = note.of(language),
            style = SiteTheme.type.bodySmall.copy(fontSize = 12.sp),
            color = SiteTheme.colors.muted
        )
    }
}

/** The room at each side of a choice's label. */
private val CHOICE_PADDING = 6.dp

/** The size of a choice's label. */
private val CHOICE_TEXT = 12.5.sp
