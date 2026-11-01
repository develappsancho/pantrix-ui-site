package com.pantrixui.site.feature.theme

import com.pantrixui.foundation.token.density.PantrixDensity
import com.pantrixui.site.core.model.Words

/** The theme builder's words, in each language. */
internal object ThemeSay {
    val Title = Words("Theme builder", "Tema oluşturucu")
    val SubtitleStart = Words("A ", "Kotlin olarak alınan bir ")
    val SubtitleEnd = Words(" you take away as Kotlin", "")
    val StartAgain = Words("Start again from Shopeers", "Shopeers'tan yeniden başla")
    val CopyKotlin = Words("Copy Kotlin", "Kotlin'i kopyala")
    val Copied = Words("Copied", "Kopyalandı")
    val SeeTheKotlin = Words("See the Kotlin", "Kotlin'i gör")
    val Close = Words("Close", "Kapat")

    // The controls.
    val Name = Words("Name", "Ad")
    val PrimaryColour = Words("Primary colour", "Ana renk")
    val Hex = Words("Hex", "Hex")
    val HexLabel = Words("Primary colour in hex", "Hex olarak ana renk")
    val White = Words("white", "beyaz")
    val NearBlack = Words("near black", "siyaha yakın")
    val TextOnIt = Words("Text on it, ", "Üstündeki metin, ")
    val LinkOnWhite = Words("As a link on white", "Beyaz üstünde bağlantı olarak")
    val LinkWarning = Words(
        "Text in this colour does not read on white. The preset writes links and focused borders in a darker shade " +
            "of it.",
        "Bu renkte metin beyaz üstünde okunmuyor. Hazır ayar bağlantıları ve odaklı kenarları onun daha koyu bir " +
            "tonunda yazar."
    )
    val CornersTitle = Words("Corners", "Köşeler")
    val Typeface = Words("Typeface", "Yazı tipi")
    val Density = Words("Density", "Yoğunluk")
    val DensityNote = Words(" · the theme's, not the preset's", " · hazır ayarın değil, temanın")
    val Light = Words("Light", "Açık")
    val Dark = Words("Dark", "Koyu")
    val Then = Words("Then: ", "Sonra: ")

    /** What a choice of corners is called, and what its note says the scale is. */
    fun corners(corners: Corners): Words = when (corners) {
        Corners.Sharp -> Words("Sharp", "Keskin")
        Corners.Rounded -> Words("Rounded", "Yuvarlak")
        Corners.Soft -> Words("Soft", "Yumuşak")
    }

    /** Whose scale the corners are, before their radii. */
    fun cornersOf(corners: Corners): Words = when (corners) {
        Corners.Sharp -> Words("Pantrix’s own", "Pantrix'in kendisi")
        Corners.Rounded -> Words("Shopeers", "Shopeers")
        Corners.Soft -> Words("Soft", "Yumuşak")
    }

    /** What a typeface is called, and what the note beside it says. */
    fun face(face: Face): Pair<Words, Words> = when (face) {
        Face.Bundled -> Words("Plus Jakarta Sans", "Plus Jakarta Sans") to Words("Bundled", "Birlikte gelir")
        Face.Own -> Words("Your own typeface", "Kendi yazı tipin") to Words("Your font files", "Senin font dosyaların")
    }

    val OwnFaceNote = Words(
        "The preview stays in Plus Jakarta Sans: your fonts are your app's resources.",
        "Önizleme Plus Jakarta Sans'ta kalır: fontların senin uygulamanın kaynaklarıdır."
    )

    /** What a density is called. */
    fun density(density: PantrixDensity): String = density.name

    /** The swatches to start from, each a colour and what it is called. */
    val Swatches: List<Pair<Long, Words>> = listOf(
        0xFF2264FF to Words("Shopeers blue", "Shopeers mavisi"),
        0xFF6D4AFF to Words("Violet", "Mor"),
        0xFF0F7B7D to Words("Teal", "Camgöbeği"),
        0xFFE5761F to Words("Orange", "Turuncu"),
        0xFFC42A55 to Words("Raspberry", "Ahududu"),
        0xFF101322 to Words("Ink", "Mürekkep")
    )

    // The preview.
    val Checkout = Words("Checkout", "Ödeme")
    val Secure = Words("Secure", "Güvenli")
    val Leaves =
        Words(
            "Ordered before 16:00, it leaves the warehouse today.",
            "16:00'dan önce verilen sipariş bugün depodan çıkar."
        )
    val Address = Words("Address", "Adres")
    val AddressValue = Words("12 Harbour Street, Flat 4", "Liman Sokağı 12, Daire 4")
    val DeliverTo = Words("Deliver to", "Teslimat yeri")
    val Places = listOf(Words("Home", "Ev"), Words("Work", "İş"), Words("Pick-up point", "Teslim noktası"))
    val Neighbour = Words("Leave with a neighbour", "Komşuya bırak")
    val NobodyAnswers = Words("If nobody answers", "Kimse açmazsa")
    val GiftWrap = Words("Gift wrap", "Hediye paketi")
    val NoteInside = Words("A note inside", "İçine bir not")
    val Total = Words("Total", "Toplam")
    val Back = Words("Back", "Geri")
    val Pay = Words("Pay", "Öde")
    val Terms = Words("Terms of sale", "Satış koşulları")
}
