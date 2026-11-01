package com.pantrixui.site.feature.roadmap

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.elements.button.ButtonSizeStyle
import com.pantrixui.components.elements.button.ButtonStyle
import com.pantrixui.components.elements.button.ButtonVariant
import com.pantrixui.components.elements.button.PantrixButton
import com.pantrixui.components.elements.selection.ControlPosition
import com.pantrixui.components.elements.switchbutton.PantrixSwitchButton
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.components.inputs.textarea.PantrixTextArea
import com.pantrixui.components.inputs.textfield.PantrixTextField
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.content.Hairline
import com.pantrixui.site.core.designsystem.content.live
import com.pantrixui.site.core.i18n.LocalLanguage

/**
 * The screen Pantrix draws from the sample description, with Pantrix's own components: in a phone's frame on a
 * desktop and a tablet, in a plain card on a phone. A desktop's has the whole sample; a smaller one leaves a part out.
 */
@Composable
internal fun Preview(sample: Sample) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val colors = PantrixTheme.colors
    val frame = when (layout) {
        SiteLayout.Desktop -> {
            Modifier
                .width(DESKTOP_PHONE)
                .clip(RoundedCornerShape(36.dp))
                .border(8.dp, SiteTheme.colors.sketch.frame, RoundedCornerShape(36.dp))
                .padding(8.dp)
        }

        SiteLayout.Tablet -> {
            Modifier
                .width(TABLET_PHONE)
                .clip(RoundedCornerShape(32.dp))
                .border(7.dp, SiteTheme.colors.sketch.frame, RoundedCornerShape(32.dp))
                .padding(7.dp)
        }

        SiteLayout.Phone -> {
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .border(1.dp, colors.border, RoundedCornerShape(18.dp))
        }
    }
    Column(frame.live().background(colors.surface)) {
        PantrixText(
            text = SduiSay.screenTitle(sample).of(language),
            style = strong(if (layout == SiteLayout.Desktop) 17.sp else 16.sp),
            color = colors.textPrimary,
            modifier = Modifier
                .fillMaxWidth()
                .height(if (layout == SiteLayout.Desktop) 52.dp else 48.dp)
                .padding(horizontal = if (layout == SiteLayout.Desktop) 16.dp else 14.dp)
                .wrapContentHeight(Alignment.CenterVertically)
        )
        Hairline()
        when (sample) {
            Sample.Campaign -> Campaign()
            Sample.Form -> Form()
        }
    }
}

/** The campaign's card: its picture with when it ends, what it offers and its button; a desktop's way out under it. */
@Composable
private fun Campaign() {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val desktop = layout == SiteLayout.Desktop
    val colors = PantrixTheme.colors
    val room = if (desktop) 16.dp else 14.dp
    Column(Modifier.padding(room), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        val shape = RoundedCornerShape(if (desktop) 18.dp else 16.dp)
        Column(Modifier.fillMaxWidth().clip(shape).border(1.dp, SiteTheme.colors.hairline, shape)) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(if (desktop) 130.dp else 110.dp)
                    .background(Brush.linearGradient(CAMPAIGN_BLUES, start = Offset.Zero, end = Offset.Infinite))
                    .padding(if (desktop) 14.dp else 12.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                PantrixText(
                    text = SduiSay.EndsSunday.of(language),
                    style = strong(if (desktop) 12.sp else 11.5.sp),
                    color = CAMPAIGN_INK,
                    modifier = Modifier.background(Color.White, CircleShape).padding(horizontal = 9.dp, vertical = 3.dp)
                )
            }
            Column(Modifier.padding(if (desktop) 14.dp else 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PantrixText(
                    text = SduiSay.CardTitle.desktop.of(language),
                    style = strong(if (desktop) 17.sp else 16.sp, FontWeight.Bold),
                    color = colors.textPrimary
                )
                PantrixText(
                    text = SduiSay.CardText.desktop.of(language),
                    style = small(if (layout == SiteLayout.Tablet) 13.sp else 13.5.sp),
                    color = colors.textSecondary
                )
                PillButton(
                    SduiSay.ShopNow.of(language),
                    "sduiShopNow",
                    height = if (layout ==
                        SiteLayout.Phone
                    ) {
                        46.dp
                    } else {
                        44.dp
                    }
                )
            }
        }
        if (desktop) PillButton(SduiSay.NotNow.of(language), "sduiNotNow", outline = true)
    }
}

/** The delivery form: a name, a switch and the save button; a desktop's has the courier's note too. */
@Composable
private fun Form() {
    val language = LocalLanguage.current
    val desktop = SiteTheme.layout == SiteLayout.Desktop
    val name = rememberTextFieldState()
    val note = rememberTextFieldState()
    var neighbour by remember { mutableStateOf(true) }
    Column(
        Modifier.padding(if (desktop) 16.dp else 14.dp),
        verticalArrangement = Arrangement.spacedBy(if (desktop) 14.dp else 12.dp)
    ) {
        PantrixTextField(
            state = name,
            modifier = Modifier.fillMaxWidth(),
            label = SduiSay.Name.of(language),
            placeholder = SduiSay.NamePlaceholder.of(language),
            tagKey = "sduiName"
        )
        // The design's switch stands at the row's end, its words before it.
        PantrixSwitchButton(
            checked = neighbour,
            onCheckedChange = { neighbour = it },
            modifier = Modifier.fillMaxWidth(),
            label = SduiSay.Neighbour.of(language),
            supportingText = if (desktop) SduiSay.NobodyAnswers.of(language) else null,
            tagKey = "sduiNeighbour",
            controlPosition = ControlPosition.End
        )
        if (desktop) {
            PantrixTextArea(
                state = note,
                modifier = Modifier.fillMaxWidth(),
                label = SduiSay.Note.of(language),
                placeholder = SduiSay.NotePlaceholder.of(language),
                maxLength = NOTE_LENGTH,
                tagKey = "sduiNote"
            )
        }
        PantrixButton(
            text = SduiSay.Save.of(language),
            onClick = {},
            fullWidth = true,
            style = ButtonStyle(medium = ButtonSizeStyle(height = 48.dp, horizontalPadding = 16.dp)),
            shape = RoundedCornerShape(12.dp),
            tagKey = "sduiSave"
        )
    }
}

/** A round-ended button of the campaign, filled or with a line around it; it does nothing here. */
@Composable
private fun PillButton(
    text: String,
    tag: String,
    height: Dp = 44.dp,
    outline: Boolean = false
) {
    PantrixButton(
        text = text,
        onClick = {},
        variant = if (outline) ButtonVariant.Outline else ButtonVariant.Solid,
        fullWidth = true,
        style = ButtonStyle(medium = ButtonSizeStyle(height = height, horizontalPadding = 16.dp)),
        shape = CircleShape,
        tagKey = tag
    )
}

private val DESKTOP_PHONE = 320.dp
private val TABLET_PHONE = 290.dp

/** The campaign picture's blues, from light to deep, as the design paints it. */
private val CAMPAIGN_BLUES = listOf(Color(0xFF2659D2), Color(0xFF264795), Color(0xFF1D3362))

/** The words of the campaign's tag: its deepest blue. */
private val CAMPAIGN_INK = Color(0xFF1D3362)
