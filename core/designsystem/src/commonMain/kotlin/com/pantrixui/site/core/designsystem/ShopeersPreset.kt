package com.pantrixui.site.core.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pantrixui.foundation.theme.preset.DesignPreset
import com.pantrixui.foundation.token.color.PantrixColor
import com.pantrixui.foundation.token.color.PantrixColors
import com.pantrixui.foundation.token.shape.PantrixShapes
import com.pantrixui.foundation.token.typography.PantrixTypography
import com.pantrixui.foundation.token.typography.rememberPantrixTypography

/**
 * The site's look: Pantrix's structure in the colours of the Shopeers design system, made accessible where its own
 * values were not. The dark primary is a lighter blue with near-black text on it, a white one reads 3.6:1 on the dark
 * surface; the light danger is a darker red, since Shopeers' own gives white text 4.3:1; success and warning carry
 * near-black text.
 *
 * Its colours beyond Pantrix's roles, the site's own grounds and the code's, are [SiteColors].
 */
object ShopeersPreset : DesignPreset {
    override val name: String = "Shopeers"

    override fun colors(dark: Boolean): PantrixColors = if (dark) Dark else Light

    @Composable
    override fun typography(): PantrixTypography =
        rememberPantrixTypography(SiteTypefaces.Sans, codeTypeface = SiteTypefaces.Mono)

    @Composable
    override fun shapes(): PantrixShapes = Shapes

    private val Shapes = PantrixShapes(
        xs = RoundedCornerShape(6.dp),
        sm = RoundedCornerShape(8.dp),
        md = RoundedCornerShape(12.dp),
        lg = RoundedCornerShape(16.dp),
        xl = RoundedCornerShape(20.dp)
    )

    private val NearBlack = Color(0xFF05070A)

    private val Light = PantrixColors(
        primary = PantrixColor(
            color = Color(0xFF2264FF),
            onColor = Color.White,
            container = Color(0xFFECF3FE),
            onContainer = Color(0xFF1A52D6)
        ),
        success = PantrixColor(
            color = Color(0xFF1F9F66),
            onColor = NearBlack,
            container = Color(0xFFE7F6EF),
            onContainer = Color(0xFF146B45)
        ),
        info = PantrixColor(
            color = Color(0xFF2264FF),
            onColor = Color.White,
            container = Color(0xFFECF3FE),
            onContainer = Color(0xFF1A52D6)
        ),
        warning = PantrixColor(
            color = Color(0xFFE5761F),
            onColor = NearBlack,
            container = Color(0xFFFFF2E6),
            onContainer = Color(0xFF8A4310)
        ),
        danger = PantrixColor(
            color = Color(0xFFC42A55),
            onColor = Color.White,
            container = Color(0xFFFCE7EC),
            onContainer = Color(0xFF8E1C3C)
        ),
        neutral = PantrixColor(
            color = Color(0xFF4B4F5C),
            onColor = Color.White,
            container = Color(0xFFEEF0F4),
            onContainer = Color(0xFF05070A)
        ),
        background = Color.White,
        surface = Color.White,
        onSurface = Color(0xFF4B4F5C),
        disabledContainer = Color(0xFFEEF0F4),
        disabledContent = Color(0xFFA0A5B3),
        textPrimary = NearBlack,
        textSecondary = Color(0xFF4B4F5C),
        textDisabled = Color(0xFFA0A5B3),
        textInverse = Color.White,
        border = Color(0xFFE7E9ED),
        borderHover = Color(0xFFD5D9E0),
        borderStrong = Color(0xFFA0A5B3),
        divider = Color(0xFFECEEF3),
        focusRing = Color(0xFF2264FF),
        scrim = Color(0x7A101322),
        chart = listOf(
            Color(0xFF2264FF),
            Color(0xFF1F9F66),
            Color(0xFFE5761F),
            Color(0xFF6D4AFF),
            Color(0xFFC42A55),
            Color(0xFF0E8FA3)
        )
    )

    private val Dark = PantrixColors(
        primary = PantrixColor(
            color = Color(0xFF5386FF),
            onColor = NearBlack,
            container = Color(0xFF16244A),
            onContainer = Color(0xFF8BADFF)
        ),
        success = PantrixColor(
            color = Color(0xFF34C17E),
            onColor = NearBlack,
            container = Color(0xFF12301F),
            onContainer = Color(0xFFA6E5C6)
        ),
        info = PantrixColor(
            color = Color(0xFF5386FF),
            onColor = NearBlack,
            container = Color(0xFF16244A),
            onContainer = Color(0xFF8BADFF)
        ),
        warning = PantrixColor(
            color = Color(0xFFFA8A37),
            onColor = NearBlack,
            container = Color(0xFF2E1D0E),
            onContainer = Color(0xFFFFB77D)
        ),
        danger = PantrixColor(
            color = Color(0xFFF04770),
            onColor = NearBlack,
            container = Color(0xFF3A1420),
            onContainer = Color(0xFFFF7D9C)
        ),
        neutral = PantrixColor(
            color = Color(0xFFC3C8D4),
            onColor = NearBlack,
            container = Color(0xFF212839),
            onContainer = Color(0xFFF4F6FA)
        ),
        background = Color(0xFF0B0E15),
        surface = Color(0xFF151A26),
        onSurface = Color(0xFFC3C8D4),
        disabledContainer = Color(0xFF212839),
        disabledContent = Color(0xFF4B5368),
        textPrimary = Color(0xFFF4F6FA),
        textSecondary = Color(0xFFC3C8D4),
        textDisabled = Color(0xFF4B5368),
        textInverse = NearBlack,
        border = Color(0xFF262D3D),
        borderHover = Color(0xFF333B4E),
        borderStrong = Color(0xFF4B5368),
        divider = Color(0xFF1F2533),
        focusRing = Color(0xFF5386FF),
        scrim = Color(0x9E000000),
        chart = listOf(
            Color(0xFF5386FF),
            Color(0xFF34C17E),
            Color(0xFFFA8A37),
            Color(0xFF9B83FF),
            Color(0xFFF04770),
            Color(0xFF3CC4D6)
        )
    )
}
