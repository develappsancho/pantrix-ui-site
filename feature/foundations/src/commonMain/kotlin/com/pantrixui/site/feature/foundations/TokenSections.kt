package com.pantrixui.site.feature.foundations

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.elements.button.ButtonVariant
import com.pantrixui.components.elements.button.PantrixButton
import com.pantrixui.components.elements.icon.PantrixIcon
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.icons.PantrixIcons
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.foundation.token.density.PantrixDensity
import com.pantrixui.foundation.token.shape.PantrixShapes
import com.pantrixui.site.core.designsystem.PantrixPreset
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.model.FoundationSection
import kotlin.math.roundToInt

/** The type: each of the theme's roles saying something in itself, with its size, its line and its weight. */
@Composable
internal fun TypeSection(modifier: Modifier) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val type = PantrixTheme.typography
    val roles = listOf(
        "display" to type.display,
        "headline" to type.headline,
        "title" to type.title,
        "callout" to type.callout,
        "body" to type.body,
        "label" to type.label,
        "footnote" to type.footnote,
        "caption" to type.caption,
        "code" to type.code
    )
    Section(FoundationSection.Type.title.of(language), modifier) {
        if (layout != SiteLayout.Phone) {
            val lead = if (layout == SiteLayout.Desktop) FoundationsSay.TypeLead else FoundationsSay.TypeLeadShort
            Lead(lead.of(language))
        }
        Framed {
            roles.forEachIndexed { index, (name, style) ->
                if (index > 0) Hairline()
                TypeRow(name, style, large = index < LARGE_ROLES)
            }
        }
    }
}

/** A type role's row: its name and measures, and a sentence in it; on a phone the measures beside a small one. */
@Composable
private fun TypeRow(
    name: String,
    style: TextStyle,
    large: Boolean
) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val sample = FoundationsSay.samples[name]?.of(language).orEmpty()
    val spec = "$name · ${style.fontSize.value.roundToInt()}/${style.lineHeight.value.roundToInt()}" +
        if (layout == SiteLayout.Tablet) "" else weightName(style.fontWeight)?.let { " $it" }.orEmpty()
    val specStyle = mono(if (layout == SiteLayout.Desktop) 12.sp else 11.5.sp)
    val muted = SiteTheme.colors.muted
    val ink = PantrixTheme.colors.textPrimary
    when {
        layout != SiteLayout.Phone -> Row(
            Modifier.fillMaxWidth().padding(
                horizontal = if (layout == SiteLayout.Desktop) 20.dp else 18.dp,
                vertical = if (large) 18.dp else 14.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PantrixText(
                text = spec,
                style = specStyle,
                color = muted,
                modifier = Modifier.width(if (layout == SiteLayout.Desktop) 156.dp else 120.dp)
            )
            PantrixText(text = sample, style = style, color = ink, modifier = Modifier.weight(1f))
        }

        large -> Column(
            Modifier.fillMaxWidth().padding(14.dp, 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            PantrixText(text = spec, style = mono(11.sp), color = muted)
            PantrixText(text = sample, style = style, color = ink)
        }

        else -> Row(Modifier.fillMaxWidth().padding(14.dp, 12.dp), verticalAlignment = Alignment.CenterVertically) {
            PantrixText(
                text = if (name == "code") "typography.code" else sample,
                style = style,
                color = ink,
                modifier = Modifier.weight(1f)
            )
            PantrixText(
                text = spec.substringBefore(" ·").let {
                    "$it · ${spec.substringAfter("· ")}"
                },
                style = mono(11.sp),
                color = muted
            )
        }
    }
}

/** What the design calls a weight beside a role's measures: none for the body's own. */
private fun weightName(weight: FontWeight?): String? = when (weight) {
    FontWeight.ExtraBold -> "ExtraBold"
    FontWeight.Bold -> "Bold"
    FontWeight.SemiBold -> "SemiBold"
    else -> null
}

/** The spacing: the theme's steps, each with its size and a bar as long as it. */
@Composable
internal fun SpacingSection(modifier: Modifier) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val spacing = PantrixTheme.spacing
    val steps = listOf(
        "none" to spacing.none,
        "xs" to spacing.xs,
        "sm" to spacing.sm,
        "md" to spacing.md,
        "lg" to spacing.lg,
        "xl" to spacing.xl,
        "xxl" to spacing.xxl
    )
    Section(FoundationSection.Spacing.title.of(language), modifier) {
        when (layout) {
            SiteLayout.Desktop -> {
                Lead(
                    FoundationsSay.SpacingLeadStart.of(language),
                    "PantrixTheme.spacing",
                    FoundationsSay.SpacingLeadEnd.of(language)
                )
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    steps.forEach { (name, size) -> SpacingRow(name, size, nameWidth = 90.dp, valueWidth = 48.dp) }
                }
            }

            SiteLayout.Tablet -> {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Bottom) {
                    steps.forEach { (name, size) ->
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Bar(size, horizontal = false)
                            PantrixText(
                                text = name,
                                style = mono(12.sp, FontWeight.SemiBold),
                                color = PantrixTheme.colors.textPrimary
                            )
                            PantrixText(
                                text = "${size.value.roundToInt()}dp",
                                style = strong(12.sp, FontWeight.Medium),
                                color = SiteTheme.colors.muted
                            )
                        }
                    }
                }
                Note(FoundationsSay.SpacingTablet.of(language))
            }

            SiteLayout.Phone -> {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    steps.drop(1).forEach { (name, size) ->
                        SpacingRow(name, size, nameWidth = 48.dp, valueWidth = 40.dp)
                    }
                }
                Note(FoundationsSay.SpacingPhone.of(language))
            }
        }
    }
}

@Composable
private fun SpacingRow(
    name: String,
    size: Dp,
    nameWidth: Dp,
    valueWidth: Dp
) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        PantrixText(
            text = name,
            style = mono(12.5.sp, FontWeight.SemiBold),
            color = PantrixTheme.colors.textPrimary,
            modifier = Modifier.width(nameWidth)
        )
        PantrixText(
            text = "${size.value.roundToInt()}dp",
            style = strong(13.sp, FontWeight.Medium),
            color = SiteTheme.colors.muted,
            modifier = Modifier.width(valueWidth)
        )
        Bar(size, horizontal = true)
    }
}

/** A step's bar: four times its size along a row, or its size across a tablet's column; nought is a thin line. */
@Composable
private fun Bar(
    size: Dp,
    horizontal: Boolean
) {
    if (size == 0.dp) {
        Box(Modifier.width(2.dp).height(if (horizontal) 20.dp else 32.dp).background(PantrixTheme.colors.borderStrong))
    } else {
        Box(
            Modifier
                .width(if (horizontal) size * BAR_SCALE else size)
                .height(if (horizontal) 20.dp else 32.dp)
                .background(SiteTheme.colors.accentLine, RoundedCornerShape(4.dp))
        )
    }
}

/** The shapes: each of the preset's roundings drawn, with Pantrix's own beside its measure. */
@Composable
internal fun ShapeSection(modifier: Modifier) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val brand = PantrixPreset.shapes()
    val own = PantrixShapes()
    val roles = listOf(
        Triple("ExtraSmall", brand.xs, own.xs),
        Triple("Small", brand.sm, own.sm),
        Triple("Medium", brand.md, own.md),
        Triple("Large", brand.lg, own.lg),
        Triple("ExtraLarge", brand.xl, own.xl)
    )
    Section(FoundationSection.Shape.title.of(language), modifier) {
        if (layout == SiteLayout.Desktop) {
            Lead(FoundationsSay.ShapeLeadStart.of(language), "ShapeRole", FoundationsSay.ShapeLeadEnd.of(language))
        }
        val columns = if (layout == SiteLayout.Phone) PHONE_SHAPES else roles.size + 1
        val pill = if (layout == SiteLayout.Desktop) FoundationsSay.APill else FoundationsSay.APillShort
        val tiles = roles.map { (name, shape, pantrix) ->
            val measure = when (layout) {
                SiteLayout.Desktop -> "${radius(shape)}dp · Pantrix ${radius(pantrix)}dp"
                else -> "${radius(shape)} · ${radius(pantrix)}dp"
            }
            Triple(name, measure, shape)
        } + Triple("Full", pill.of(language), PantrixTheme.shapes.full)
        Grid(tiles, columns, gap = if (layout == SiteLayout.Desktop) 14.dp else 12.dp) { (name, measure, shape), tile ->
            ShapeTile(name, measure, shape, tile)
        }
        if (layout == SiteLayout.Tablet) Note(FoundationsSay.ShapeFoot.of(language), muted = true)
    }
}

@Composable
private fun ShapeTile(
    name: String,
    measure: String,
    shape: Shape,
    modifier: Modifier
) {
    val layout = SiteTheme.layout
    Column(modifier, verticalArrangement = Arrangement.spacedBy(if (layout == SiteLayout.Desktop) 8.dp else 6.dp)) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(
                    when (layout) {
                        SiteLayout.Desktop -> 84.dp
                        SiteLayout.Tablet -> 76.dp
                        SiteLayout.Phone -> 72.dp
                    }
                )
                .background(PantrixTheme.colors.primary.container, shape)
                .border(2.dp, PantrixTheme.colors.primary.color, shape)
        )
        PantrixText(
            text = name,
            style = strong(
                if (layout ==
                    SiteLayout.Desktop
                ) {
                    14.sp
                } else {
                    13.sp
                }
            ),
            color = PantrixTheme.colors.textPrimary
        )
        PantrixText(
            text = measure,
            style = strong(
                if (layout ==
                    SiteLayout.Desktop
                ) {
                    12.5.sp
                } else {
                    12.sp
                },
                FontWeight.Medium
            ),
            color = SiteTheme.colors.muted
        )
    }
}

/** A rounding's radius in dp, read from the shape as it is drawn at one pixel to a dp. */
private fun radius(shape: Shape): Int {
    val corner = (shape as? CornerBasedShape)?.topStart ?: return 0
    return corner.toPx(Size(SHAPE_PROBE, SHAPE_PROBE), Density(1f)).roundToInt()
}

/** The elevation: a card at each of the theme's heights over the page's ground. */
@Composable
internal fun ElevationSection(modifier: Modifier) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val elevation = PantrixTheme.elevation
    val steps = listOf(
        "none" to elevation.none,
        "sm" to elevation.sm,
        "md" to elevation.md,
        "lg" to elevation.lg,
        "xl" to elevation.xl
    )
    Section(FoundationSection.Elevation.title.of(language), modifier) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(SiteTheme.colors.canvas, RoundedCornerShape(16.dp))
                .padding(
                    when (layout) {
                        SiteLayout.Desktop -> 28.dp
                        SiteLayout.Tablet -> 24.dp
                        SiteLayout.Phone -> 18.dp
                    }
                )
        ) {
            if (layout == SiteLayout.Phone) {
                // Two to a row, the highest across the last.
                Grid(steps.dropLast(1), columns = 2, gap = 14.dp, last = steps.last()) { (name, height), tile ->
                    ElevationCard(name, height, tile)
                }
            } else {
                Grid(
                    steps,
                    columns = steps.size,
                    gap = if (layout ==
                        SiteLayout.Desktop
                    ) {
                        18.dp
                    } else {
                        14.dp
                    }
                ) { (name, height), tile ->
                    ElevationCard(name, height, tile)
                }
            }
        }
    }
}

@Composable
private fun ElevationCard(
    name: String,
    height: Dp,
    modifier: Modifier
) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val shape = RoundedCornerShape(14.dp)
    val measure =
        "${height.value.roundToInt()}dp" + if (height == 0.dp) ", ${FoundationsSay.ABorder.of(language)}" else ""
    val content: @Composable () -> Unit = {
        PantrixText(text = name, style = mono(12.5.sp, FontWeight.SemiBold), color = PantrixTheme.colors.textPrimary)
        PantrixText(text = measure, style = strong(12.5.sp, FontWeight.Medium), color = SiteTheme.colors.muted)
    }
    val card = modifier
        .height(
            when (layout) {
                SiteLayout.Desktop -> 96.dp
                SiteLayout.Tablet -> 86.dp
                SiteLayout.Phone -> 72.dp
            }
        )
        .then(
            if (height ==
                0.dp
            ) {
                Modifier.border(1.dp, PantrixTheme.colors.border, shape)
            } else {
                Modifier.shadow(height, shape)
            }
        )
        .background(PantrixTheme.colors.surface, shape)
        .padding(horizontal = 14.dp, vertical = 12.dp)
    if (layout == SiteLayout.Phone) {
        Row(card, horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) { content() }
    } else {
        Column(card, verticalArrangement = Arrangement.SpaceBetween) { content() }
    }
}

/** The motion: a dot for each of the theme's durations, sent along its track when the button plays them. */
@Composable
internal fun MotionSection(
    state: FoundationsUiState,
    onAction: (FoundationsAction) -> Unit,
    modifier: Modifier
) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val phone = layout == SiteLayout.Phone
    val motion = PantrixTheme.motion
    val durations = listOf("fast" to motion.fast, "normal" to motion.normal, "slow" to motion.slow)
    val play: @Composable (Modifier) -> Unit = { buttonModifier ->
        PantrixButton(
            onClick = { onAction(FoundationsAction.Play) },
            contentDescription = FoundationsSay.Play.of(language),
            modifier = buttonModifier,
            variant = ButtonVariant.Outline,
            tagKey = "playMotion"
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                PantrixIcon(imageVector = PantrixIcons.Play, contentDescription = null, size = 14.dp)
                PantrixText(text = FoundationsSay.Play.of(language), style = SiteTheme.type.button)
            }
        }
    }
    val title = if (phone) FoundationsSay.MotionTitlePhone else FoundationSection.Motion.title
    Section(title.of(language), modifier, aside = if (phone) null else ({ play(Modifier) })) {
        if (layout == SiteLayout.Desktop) Lead(FoundationsSay.MotionLead.of(language))
        val shape = RoundedCornerShape(16.dp)
        Column(
            Modifier
                .fillMaxWidth()
                .border(1.dp, PantrixTheme.colors.border, shape)
                .padding(if (phone) 16.dp else 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            durations.forEach { (name, millis) -> Track("$name · ${millis}ms", millis, state.moved) }
            if (phone) play(Modifier.fillMaxWidth())
        }
        if (phone) DensityButtons()
    }
}

/** A duration's track, its dot at one end or the other, moving between them in that duration. */
@Composable
private fun Track(
    label: String,
    millis: Int,
    moved: Boolean
) {
    val phone = SiteTheme.layout == SiteLayout.Phone
    val easing = PantrixTheme.motion.standardEasing.curve
    val share by animateFloatAsState(if (moved) 1f else 0f, tween(millis, easing = easing), label = "motion")
    val track: @Composable (Modifier) -> Unit = { trackModifier ->
        BoxWithConstraints(
            trackModifier.height(28.dp).background(SiteTheme.colors.hover, RoundedCornerShape(percent = 50))
        ) {
            val travel = maxWidth - DOT - DOT_INSET * 2
            Box(
                Modifier
                    .offset(x = DOT_INSET + travel * share, y = DOT_INSET)
                    .size(DOT)
                    .background(PantrixTheme.colors.primary.color, RoundedCornerShape(percent = 50))
            )
        }
    }
    val name: @Composable (Modifier) -> Unit = { nameModifier ->
        PantrixText(
            text = label,
            style = mono(if (phone) 12.sp else 12.5.sp, FontWeight.SemiBold),
            color = PantrixTheme.colors.textPrimary,
            modifier = nameModifier
        )
    }
    if (phone) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            name(Modifier)
            track(Modifier.fillMaxWidth())
        }
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            name(Modifier.width(120.dp))
            track(Modifier.weight(1f))
        }
    }
}

/** The density: the same button in each of the theme's densities, drawn by Pantrix in a theme of that density. */
@Composable
internal fun DensitySection(modifier: Modifier) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    // A phone shows the buttons in its motion's section.
    if (layout == SiteLayout.Phone) return
    Section(FoundationSection.Density.title.of(language), modifier) {
        val lead = if (layout == SiteLayout.Desktop) FoundationsSay.DensityLead else FoundationsSay.DensityLeadShort
        Lead(lead.of(language))
        DensityButtons()
    }
}

@Composable
private fun DensityButtons() {
    val language = LocalLanguage.current
    val phone = SiteTheme.layout == SiteLayout.Phone
    val ground = Modifier
        .fillMaxWidth()
        .background(SiteTheme.colors.canvas, RoundedCornerShape(16.dp))
        .padding(if (phone) 16.dp else 24.dp)
    val each: @Composable (PantrixDensity) -> Unit = { density ->
        PantrixTheme(density = density) {
            PantrixButton(
                text = FoundationsSay.AddToCart.of(language),
                onClick = {},
                tagKey = "density_${density.name.lowercase()}"
            )
        }
    }
    val caption: @Composable (PantrixDensity) -> Unit = { density ->
        PantrixText(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = PantrixTheme.colors.textPrimary, fontWeight = FontWeight.ExtraBold)) {
                    append(FoundationsSay.name(density))
                }
                append(" · ${PantrixTheme.strings.decimal(density.scale, fractionDigits = 2)}")
            },
            style = strong(12.5.sp, FontWeight.Medium),
            color = SiteTheme.colors.muted
        )
    }
    if (phone) {
        Column(ground, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            PantrixDensity.entries.forEach { density ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    each(density)
                    caption(density)
                }
            }
        }
    } else {
        Row(ground, horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.Bottom) {
            PantrixDensity.entries.forEach { density ->
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    each(density)
                    caption(density)
                }
            }
        }
    }
}

/** A frame around rows: a line around them and the room's corners. */
@Composable
private fun Framed(content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(if (SiteTheme.layout == SiteLayout.Phone) 14.dp else 16.dp)
    Column(Modifier.fillMaxWidth().clip(shape).border(1.dp, PantrixTheme.colors.border, shape)) { content() }
}

@Composable
private fun Hairline() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(SiteTheme.colors.hairline))
}

/** A short note under a section's tokens. */
@Composable
private fun Note(
    text: String,
    muted: Boolean = false
) {
    PantrixText(
        text = text,
        style = strong(if (muted) 13.sp else 14.sp, FontWeight.Medium),
        color = if (muted) SiteTheme.colors.muted else PantrixTheme.colors.textSecondary
    )
}

/** [items] drawn by [tile] in rows of [columns], each [gap] apart, and [last] across the whole of a row of its own. */
@Composable
private fun <T> Grid(
    items: List<T>,
    columns: Int,
    gap: Dp,
    last: T? = null,
    tile: @Composable (T, Modifier) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
        items.chunked(columns).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                row.forEach { item -> tile(item, Modifier.weight(1f)) }
                repeat(columns - row.size) { Box(Modifier.weight(1f)) }
            }
        }
        if (last != null) tile(last, Modifier.fillMaxWidth())
    }
}

/** How many of the type roles are shown large on a phone, their sentence under their measures. */
private const val LARGE_ROLES = 5
private const val PHONE_SHAPES = 3
private const val BAR_SCALE = 4
private const val SHAPE_PROBE = 1000f
private val DOT = 20.dp
private val DOT_INSET = 4.dp
