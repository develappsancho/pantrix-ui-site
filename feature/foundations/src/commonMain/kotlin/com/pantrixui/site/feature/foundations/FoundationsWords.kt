package com.pantrixui.site.feature.foundations

import com.pantrixui.foundation.token.density.PantrixDensity
import com.pantrixui.site.core.model.Words

/** The foundations page's words, in each language. */
internal object FoundationsSay {
    val Title = Words("Foundations", "Temeller")
    val Lead = Words(
        "The tokens every component is drawn with. This page shows them in the Pantrix preset, the look this site " +
            "wears: a brand's colours, type and shapes over Pantrix's own spacing, elevation, motion and density.",
        "Her bileşenin çizildiği tokenlar. Bu sayfa onları sitenin giydiği görünüşte, Pantrix hazır ayarında " +
            "gösteriyor: Pantrix'in kendi boşluğu, yükseltisi, hareketi ve yoğunluğu üstünde bir markanın renkleri, " +
            "yazısı ve şekilleri."
    )
    val LeadTablet = Words(
        "The tokens every component is drawn with, in the Pantrix preset: a brand's colours, type and shapes over " +
            "Pantrix's own spacing, elevation, motion and density.",
        "Her bileşenin çizildiği tokenlar, Pantrix hazır ayarında: Pantrix'in kendi boşluğu, yükseltisi, hareketi " +
            "ve yoğunluğu üstünde bir markanın renkleri, yazısı ve şekilleri."
    )
    val LeadPhone = Words(
        "The tokens every component is drawn with, in the Pantrix preset.",
        "Her bileşenin çizildiği tokenlar, Pantrix hazır ayarında."
    )
    val Tags = listOf(
        Words("PantrixPreset", "PantrixPreset"),
        Words("Light and dark", "Açık ve koyu"),
        Words("Contrast worked out on this page", "Kontrast bu sayfada hesaplanır")
    )

    // Colour.
    val Light = Words("Light", "Açık")
    val Dark = Words("Dark", "Koyu")
    val ColourLeadStart = Words(
        "A component asks for a role, never a hex. Each brand role is a ",
        "Bir bileşen bir hex değil, bir rol ister. Markanın her rolü dörtlü bir "
    )
    val ColourLeadEnd = Words(
        " of four: the colour, what is written on it, its container and what is written on that.",
        ": renk, üstüne yazılan, kabı ve kabın üstüne yazılan."
    )
    val Container = Words("container", "kap")
    val Grounds = Words("Grounds, text and lines", "Zeminler, metin ve çizgiler")
    val Role = Words("Role", "Rol")
    val Colour = Words("Colour", "Renk")
    val OnSurface = Words("On surface", "Yüzeyde")
    val For = Words("For", "Ne için")
    val Passes = Words("passes", "geçer")
    val Fails = Words("fails", "kalır")
    val Palette = Words("The Pantrix palette", "Pantrix paleti")

    /** What a ground, a text or a line is for, by its role's name. */
    val uses: Map<String, Words> = mapOf(
        "background" to Words("The window behind the surfaces", "Yüzeylerin arkasındaki pencere"),
        "surface" to Words("Cards, fields, sheets", "Kartlar, alanlar, sayfalar"),
        "textPrimary" to Words("Text", "Metin"),
        "textSecondary" to Words("Help, placeholders, counts", "Yardım, yer tutucular, sayaçlar"),
        "textDisabled" to Words("What cannot be used now", "Şu an kullanılamayan"),
        "border" to Words("A field at rest", "Durgun bir alan"),
        "borderHover" to Words("Under a pointer", "İşaretçinin altında"),
        "borderStrong" to Words("Scroll bars, a control’s edge", "Kaydırma çubukları, bir kontrolün kenarı"),
        "divider" to Words("Lines between rows", "Satırlar arasındaki çizgiler"),
        "focusRing" to Words("The keyboard’s focus", "Klavyenin odağı")
    )

    // Typography.
    val TypeLead = Words(
        "Nine roles in Plus Jakarta Sans, which the library bundles, and one for code. A text given no role is a " +
            "label; a field types in its style's role, the body's on a phone and the label's under a pointer.",
        "Kütüphanenin birlikte getirdiği Plus Jakarta Sans'ta dokuz rol ve kod için bir tane. Rolü verilmeyen metin " +
            "bir etikettir; bir alan stilinin rolünde yazar: telefonda gövdeninkinde, işaretçi altında etiketinkinde."
    )
    val TypeLeadShort = Words(
        "Nine roles in Plus Jakarta Sans, and one for code. A field types in the body's role on a phone and the " +
            "label's under a pointer.",
        "Plus Jakarta Sans'ta dokuz rol ve kod için bir tane. Bir alan telefonda gövdenin, işaretçi altında " +
            "etiketin rolünde yazar."
    )

    /** What each type role is shown saying, by its name. */
    val samples: Map<String, Words> = mapOf(
        "display" to Words("Every platform's manners", "Her platformun alışkanlıkları"),
        "headline" to Words("Orders this week", "Bu haftaki siparişler"),
        "title" to Words("Delivery address", "Teslimat adresi"),
        "callout" to Words("Free returns within thirty days.", "Otuz gün içinde ücretsiz iade."),
        "body" to Words(
            "The parcel leaves the warehouse today and reaches you on Thursday.",
            "Paket bugün depodan çıkıyor ve perşembe sana ulaşıyor."
        ),
        "label" to Words("E-mail address", "E-posta adresi"),
        "footnote" to Words("Prices include VAT.", "Fiyatlara KDV dahildir."),
        "caption" to Words("Updated 2 minutes ago", "2 dakika önce güncellendi"),
        "code" to Words("PantrixTheme.typography.body", "PantrixTheme.typography.body")
    )

    // Spacing.
    val SpacingLeadStart = Words("Seven steps, from ", "Yedi adım, ")
    val SpacingLeadEnd = Words(
        ". A page's margins come from the window's size class.",
        " içinden. Bir sayfanın kenar boşlukları pencerenin boyut sınıfından gelir."
    )
    val SpacingTablet = Words(
        "On this tablet the page keeps Pantrix's Medium layout: 24dp margins, 8 columns, 16dp gutters.",
        "Bu tablette sayfa Pantrix'in Medium düzenini izler: 24dp kenar boşlukları, 8 sütun, 16dp aralıklar."
    )
    val SpacingPhone = Words(
        "On a phone the page keeps Pantrix's Compact layout: 16dp margins, 4 columns.",
        "Telefonda sayfa Pantrix'in Compact düzenini izler: 16dp kenar boşlukları, 4 sütun."
    )

    // Shape.
    val ShapeLeadStart = Words("Components ask for a ", "Bileşenler bir ")
    val ShapeLeadEnd = Words(
        "; the preset says how round each one is. The Pantrix preset rounds more than the library's default scale.",
        " ister; her birinin ne kadar yuvarlak olduğunu hazır ayar söyler. Pantrix hazır ayarı, kütüphanenin " +
            "varsayılan ölçeğinden daha çok yuvarlar."
    )
    val APill = Words("A pill, on every scale", "Her ölçekte hap")
    val APillShort = Words("A pill", "Hap")
    val ShapeFoot = Words(
        "The Pantrix preset first, the library's default scale second.",
        "Önce Pantrix hazır ayarı, sonra kütüphanenin varsayılan ölçeği."
    )
    val ABorder = Words("a border", "bir kenar çizgisi")

    // Motion.
    val MotionTitlePhone = Words("Motion and density", "Hareket ve yoğunluk")
    val Play = Words("Play the three", "Üçünü oynat")
    val MotionLead = Words(
        "Three durations. Under the system's reduced motion the theme keeps what moves still and lets it change at " +
            "once.",
        "Üç süre. Sistem hareketi azalttığında tema kımıldayanı yerinde tutar ve bir anda değişmesine izin verir."
    )

    // Density.
    val DensityLead = Words(
        "Comfortable unless the app asks for another. Compact suits a pointer and never makes a touch target smaller " +
            "than the platform's own; Spacious makes it larger.",
        "Uygulama başkasını istemedikçe Comfortable. Compact işaretçiye uyar ve bir dokunma hedefini asla platformun " +
            "kendininkinden küçültmez; Spacious büyütür."
    )
    val DensityLeadShort = Words(
        "Compact never makes a touch target smaller than the platform's own; Spacious makes it larger.",
        "Compact bir dokunma hedefini asla platformun kendininkinden küçültmez; Spacious büyütür."
    )
    val AddToCart = Words("Add to cart", "Sepete ekle")

    /** What a density is called beside its button. */
    fun name(density: PantrixDensity): String = density.name
}

/**
 * A step of one of the brand's scales of colour, as the palette shows it.
 *
 * @property step its number on the scale: 50, 500.
 * @property argb the colour.
 */
internal class PaletteStep(
    val step: Int,
    val argb: Long
)

/** The preset's blues, from 50 to 700. */
internal val BLUES = listOf(
    PaletteStep(50, 0xFFF4F8FF),
    PaletteStep(100, 0xFFECF3FE),
    PaletteStep(200, 0xFFCFDFFF),
    PaletteStep(300, 0xFF8BADFF),
    PaletteStep(400, 0xFF5386FF),
    PaletteStep(500, 0xFF2264FF),
    PaletteStep(600, 0xFF1A52D6),
    PaletteStep(700, 0xFF1442B8)
)

/** The preset's greys, from 25 to 900. */
internal val GREYS = listOf(
    PaletteStep(25, 0xFFFAFBFC),
    PaletteStep(50, 0xFFF5F7F9),
    PaletteStep(100, 0xFFEEF0F4),
    PaletteStep(200, 0xFFE7E9ED),
    PaletteStep(300, 0xFFD5D9E0),
    PaletteStep(400, 0xFFA0A5B3),
    PaletteStep(500, 0xFF767A8A),
    PaletteStep(600, 0xFF63647B),
    PaletteStep(700, 0xFF4B4F5C),
    PaletteStep(800, 0xFF2C2F3A),
    PaletteStep(900, 0xFF101322)
)
