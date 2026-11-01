package com.pantrixui.site.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.chart.bar.PantrixBarChart
import com.pantrixui.components.chart.cartesian.ChartAxis
import com.pantrixui.components.chart.cartesian.ChartValue
import com.pantrixui.components.chart.common.ChartStyle
import com.pantrixui.components.data.tag.PantrixTag
import com.pantrixui.components.data.tag.TagStyle
import com.pantrixui.components.data.tag.TagTone
import com.pantrixui.components.date.datepicker.DatePickerStyle
import com.pantrixui.components.date.datepicker.PantrixDatePicker
import com.pantrixui.components.elements.button.ButtonVariant
import com.pantrixui.components.elements.button.PantrixButton
import com.pantrixui.components.elements.chip.PantrixChip
import com.pantrixui.components.elements.selection.ControlPosition
import com.pantrixui.components.elements.switchbutton.PantrixSwitchButton
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.components.inputs.textarea.PantrixTextArea
import com.pantrixui.components.inputs.textfield.PantrixTextField
import com.pantrixui.foundation.date.PantrixDate
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.foundation.token.shape.ShapeRole
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.i18n.LocalLanguage

/**
 * What the stage draws in the chosen platform's conventions: a desktop's form beside a month and a week's reviews,
 * with chips and a sent message under them; a tablet's form beside the two; a phone's form over the message.
 */
@Composable
internal fun Showcase(layout: SiteLayout) {
    when (layout) {
        SiteLayout.Desktop -> Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                // The form grows from 300 wide and the column beside it from 220, each by half of what is left.
                val room = maxWidth - FORM_BASIS - SIDE_BASIS - 16.dp
                if (room >= 0.dp) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.Top) {
                        ReviewCard(phone = false, modifier = Modifier.width(FORM_BASIS + room / 2))
                        Side(Modifier.width(SIDE_BASIS + room / 2))
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        ReviewCard(phone = false, modifier = Modifier.fillMaxWidth())
                        Side(Modifier.fillMaxWidth())
                    }
                }
            }
            ChipsAndMessage()
        }

        SiteLayout.Tablet -> Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            ReviewCard(phone = false, modifier = Modifier.weight(1f))
            Side(Modifier.width(TABLET_SIDE))
        }

        SiteLayout.Phone -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ReviewCard(phone = true, modifier = Modifier.fillMaxWidth())
            SentMessage(Modifier.fillMaxWidth())
        }
    }
}

/** A review being written: its title, its comment, whether it carries the writer's name, and its two buttons. */
@Composable
private fun ReviewCard(
    phone: Boolean,
    modifier: Modifier
) {
    val language = LocalLanguage.current
    // The words are the page's language's: another language starts the form again in its words.
    val title = remember(language) { TextFieldState(HomeSay.TitleValue.of(language)) }
    val comment = remember(language) { TextFieldState(HomeSay.CommentValue.of(language)) }
    var named by rememberSaveable { mutableStateOf(true) }
    StageCard(modifier, padding = if (phone) 16.dp else 20.dp, gap = if (phone) 12.dp else 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PantrixText(
                text = HomeSay.WriteReview.of(language),
                style = display(if (phone) 15.sp else 16.sp, line = 1.4f, spacing = -0.01f),
                color = PantrixTheme.colors.textPrimary,
                modifier = Modifier.weight(1f)
            )
            PantrixTag(
                text = HomeSay.Verified.of(language),
                tone = TagTone.Success,
                style = TagStyle(shape = ShapeRole.Full),
                tagKey = "homeVerified"
            )
        }
        if (!phone) {
            PantrixTextField(
                state = title,
                label = HomeSay.Title.of(language),
                modifier = Modifier.fillMaxWidth(),
                tagKey = "homeTitle"
            )
        }
        PantrixTextArea(
            state = comment,
            label = HomeSay.Comment.of(language),
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 4,
            resizable = false,
            maxLength = COMMENT_LENGTH,
            tagKey = "homeComment"
        )
        // The design's switch stands at the row's end, as a setting's does on iOS.
        PantrixSwitchButton(
            checked = named,
            onCheckedChange = { named = it },
            modifier = Modifier.fillMaxWidth(),
            label = HomeSay.PostWithName.of(language),
            tagKey = "homeNamed",
            labelStyle = SiteTheme.type.nav.copy(color = PantrixTheme.colors.textPrimary),
            controlPosition = ControlPosition.End
        )
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp, if (phone) Alignment.Start else Alignment.End)
        ) {
            PantrixButton(
                text = HomeSay.Cancel.of(language),
                onClick = { comment.clearText() },
                variant = ButtonVariant.Outline,
                modifier = if (phone) Modifier.weight(1f) else Modifier,
                tagKey = "homeCancel"
            )
            PantrixButton(
                text = HomeSay.Send.of(language),
                onClick = {},
                modifier = if (phone) Modifier.weight(2f) else Modifier,
                tagKey = "homeSend"
            )
        }
    }
}

/** The month and the week's reviews, one over the other beside the form. */
@Composable
private fun Side(modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        StageCard(Modifier.fillMaxWidth(), padding = 14.dp) {
            var day by remember { mutableStateOf<PantrixDate?>(PICKED_DAY) }
            PantrixDatePicker(
                selected = day,
                onSelect = { day = it },
                modifier = Modifier.fillMaxWidth(),
                style = DatePickerStyle(cellSize = 30.dp, daySize = 26.dp, padding = 0.dp, gap = 2.dp),
                tagKey = "homeDay"
            )
        }
        WeekCard(Modifier.fillMaxWidth())
    }
}

/** A day of the week's reviews. */
private class ReviewDay(
    val name: String,
    val reviews: Int
)

/** "Reviews this week": the count, and a bar for each day, the best day's in the brand's colour. */
@Composable
private fun WeekCard(modifier: Modifier) {
    val language = LocalLanguage.current
    val colors = PantrixTheme.colors
    val week = remember { WEEK.mapIndexed { index, reviews -> ReviewDay("${index + 1}", reviews) } }
    val best = week.maxBy { it.reviews }
    StageCard(modifier, padding = 14.dp, gap = 10.dp) {
        Row(verticalAlignment = Alignment.Bottom) {
            PantrixText(
                text = HomeSay.ReviewsThisWeek.of(language),
                style = SiteTheme.type.bodySmall.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                color = colors.textSecondary,
                modifier = Modifier.weight(1f)
            )
            PantrixText(
                // The week's sum, written as the page's language writes an amount: 1,284, or 1.284 in Turkish.
                text = PantrixTheme.strings.decimal(WEEK.sum(), fractionDigits = 0),
                style = display(20.sp, line = 1.3f, spacing = -0.02f),
                color = colors.textPrimary
            )
        }
        PantrixBarChart(
            data = week,
            x = { it.name },
            values = listOf(
                ChartValue("days", HomeSay.ReviewsThisWeek.of(language), color = SiteTheme.colors.accentLine) {
                    if (it === best) null else it.reviews
                },
                ChartValue("best", HomeSay.ReviewsThisWeek.of(language), color = colors.primary.color) {
                    if (it === best) it.reviews else null
                }
            ),
            modifier = Modifier.fillMaxWidth().height(if (SiteTheme.layout == SiteLayout.Desktop) 72.dp else 64.dp),
            stacked = true,
            xAxis = null,
            // No axis is drawn; its top only sets the best day's bar at the design's height.
            yAxis = ChartAxis(labels = false, line = false, grid = false, min = 0.0, max = WEEK_TOP),
            secondYAxis = null,
            legend = null,
            tooltip = null,
            description = HomeSay.ReviewsThisWeek.of(language),
            style = ChartStyle(barRadius = 5.dp, barBandShare = BAR_SHARE),
            tagKey = "homeReviews"
        )
    }
}

/** What a review is tagged with, as chips, and the message a sent review leaves. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipsAndMessage() {
    val language = LocalLanguage.current
    var picked by rememberSaveable { mutableStateOf(setOf(0)) }
    FlowRow(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        itemVerticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HomeSay.Chips.forEachIndexed { index, words ->
                PantrixChip(
                    label = words.of(language),
                    selected = index in picked,
                    onSelectedChange = { on -> picked = if (on) picked + index else picked - index },
                    tagKey = "homeChip$index"
                )
            }
        }
        SentMessage()
    }
}

/**
 * The message a sent review leaves, as a snackbar draws it: a picture of one, since the snackbar itself is a layer
 * over a screen, which the stage has no room for.
 */
@Composable
private fun SentMessage(modifier: Modifier = Modifier) {
    val language = LocalLanguage.current
    val code = SiteTheme.colors.code
    val phone = SiteTheme.layout == SiteLayout.Phone
    Row(
        modifier
            .heightIn(min = if (phone) 44.dp else 40.dp)
            .background(code.background, RoundedCornerShape(12.dp))
            .padding(start = 14.dp, end = 6.dp),
        horizontalArrangement = if (phone) Arrangement.SpaceBetween else Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PantrixText(
            text = HomeSay.ReviewSent.of(language),
            style = SiteTheme.type.nav.copy(fontSize = if (phone) 13.5.sp else 13.sp),
            color = code.onSelectedTab
        )
        PantrixText(
            text = HomeSay.Undo.of(language),
            style = SiteTheme.type.nav.copy(fontSize = 13.sp, fontWeight = FontWeight.ExtraBold),
            color = code.keyword,
            modifier = Modifier.padding(horizontal = if (phone) 10.dp else 8.dp)
        )
    }
}

/**
 * The widths the desktop's form and the column beside it grow from, and the tablet's column. The design's column is
 * 220 wide; the month's title needs more beside its arrows, which are a finger's targets on a phone's platforms.
 */
private val FORM_BASIS = 260.dp
private val SIDE_BASIS = 260.dp
private val TABLET_SIDE = 270.dp

/** How much of a day's room its bar takes: the design's bars stand 6 apart. */
private const val BAR_SHARE = 0.82f

/** The most a comment takes. */
private const val COMMENT_LENGTH = 280

/** The day the stage's month has chosen. */
private val PICKED_DAY = PantrixDate(2026, 10, 26)

/** The week's reviews, a day at a time. */
private val WEEK = listOf(116, 159, 141, 214, 196, 281, 177)

/** The top of the week's scale: the best day's bar is 92% of the card's room, as the design draws it. */
private const val WEEK_TOP = 305.0
