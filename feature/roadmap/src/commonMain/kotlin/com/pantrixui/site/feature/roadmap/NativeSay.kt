package com.pantrixui.site.feature.roadmap

import com.pantrixui.site.core.designsystem.BySize
import com.pantrixui.site.core.model.LibraryFacts
import com.pantrixui.site.core.model.TokenKind
import com.pantrixui.site.core.model.Words

/** The words of the part about the three libraries, in each language and each size. */
internal object NativeSay {
    val Pill = Words("One design system, three libraries", "Tek tasarım sistemi, üç kütüphane")
    val Title = Words("Take the Pantrix your project is written in", "Projenin yazıldığı Pantrix'i al")
    val Lead = BySize(
        Words(
            "A Compose Multiplatform project takes today's library. A native Android project will take Pantrix for " +
                "Jetpack Compose, and a native iOS project Pantrix for SwiftUI. The three are the same library: the " +
                "same components under the same names, the same tokens and presets, the same tags. What differs is " +
                "the language and the toolkit.",
            "Bir Compose Multiplatform projesi bugünkü kütüphaneyi alır. Yerel bir Android projesi Jetpack Compose " +
                "için Pantrix'i, yerel bir iOS projesi de SwiftUI için Pantrix'i alacak. Üçü aynı kütüphanedir: aynı " +
                "adlarla aynı bileşenler, aynı tokenlar ve hazır ayarlar, aynı etiketler. Değişen, dil ve araç " +
                "takımıdır."
        ),
        tablet = Words(
            "A Compose Multiplatform project takes today’s library, a native Android project Pantrix for Jetpack " +
                "Compose, a native iOS project Pantrix for SwiftUI. The same components under the same names, the " +
                "same tokens, presets and tags; only the language and the toolkit differ.",
            "Bir Compose Multiplatform projesi bugünkü kütüphaneyi, yerel bir Android projesi Jetpack Compose için " +
                "Pantrix'i, yerel bir iOS projesi SwiftUI için Pantrix'i alır. Aynı adlarla aynı bileşenler, aynı " +
                "tokenlar, hazır ayarlar ve etiketler; yalnızca dil ve araç takımı değişir."
        ),
        phone = Words(
            "The same library for a Compose Multiplatform, a native Android and a native iOS project. Only the " +
                "language and the toolkit differ.",
            "Bir Compose Multiplatform, yerel bir Android ve yerel bir iOS projesi için aynı kütüphane. Yalnızca dil " +
                "ve araç takımı değişir."
        )
    )

    /** The three libraries: today's first. */
    val Libraries = listOf(
        Library(
            project = Words("A Compose Multiplatform project", "Bir Compose Multiplatform projesi"),
            name = Words("Pantrix UI", "Pantrix UI"),
            body = Words(
                "Today’s library: one Kotlin codebase for every platform.",
                "Bugünkü kütüphane: her platform için tek bir Kotlin kod tabanı."
            ),
            availability = Availability.Today,
            targets = listOf(
                Words("Android", "Android"),
                Words("iOS", "iOS"),
                Words("Desktop", "Masaüstü"),
                Words("Web", "Web")
            ),
            file = "commonMain · Kotlin",
            code = { _ -> kotlin("Res") }
        ),
        Library(
            project = Words("A native Android project", "Yerel bir Android projesi"),
            name = Words("Pantrix for Jetpack Compose", "Jetpack Compose için Pantrix"),
            body = Words(
                "The same library written again for an Android-only app, on Google’s Jetpack Compose libraries.",
                "Yalnızca Android'de çalışan bir uygulama için, Google'ın Jetpack Compose kütüphaneleri üzerinde " +
                    "yeniden yazılan aynı kütüphane."
            ),
            availability = Availability.Later,
            targets = listOf(Words("Android", "Android")),
            file = "app · Kotlin",
            code = { _ -> kotlin("R") }
        ),
        Library(
            project = Words("A native iOS project", "Yerel bir iOS projesi"),
            name = Words("Pantrix for SwiftUI", "SwiftUI için Pantrix"),
            body = Words(
                "The same library written again in Swift, as SwiftUI views.",
                "Swift'te, SwiftUI görünümleri olarak yeniden yazılan aynı kütüphane."
            ),
            availability = Availability.Later,
            targets = listOf(Words("iOS", "iOS")),
            file = "Checkout.swift · Swift",
            code = { label -> swift(label) }
        )
    )

    /** The Swift sample's label: shorter on a phone. */
    val NoteLabel = BySize(Words("Note to the courier", "Kuryeye not"), phone = Words("Note", "Not"))

    val Table = Words("What is the same, and what differs", "Ne aynı, ne farklı")
    val ColumnNames = BySize(
        listOf("Compose Multiplatform", "Jetpack Compose", "SwiftUI"),
        tablet = listOf("Multiplatform", "Jetpack Compose", "SwiftUI")
    )
    val Same = Words("Same · ", "Aynı · ")
    val SameRows = listOf(
        Words("Components", "Bileşenler") to Words(
            "every component under the same name: PantrixTextArea, PantrixButton, PantrixDatePicker…",
            "her bileşen aynı adla: PantrixTextArea, PantrixButton, PantrixDatePicker…"
        ),
        Words("The look", "Görünüş") to Words(
            "the same tokens and presets, Shopeers’ among them",
            "aynı tokenlar ve hazır ayarlar, Shopeers'inki de"
        ),
        Words("Tags", "Etiketler") to Words(
            "TextArea_courierNote in all three, so one Maestro flow tests the Android and the iOS app",
            "üçünde de TextArea_courierNote; böylece tek bir Maestro akışı Android ve iOS uygulamasını sınar"
        ),
        Words("Accessibility", "Erişilebilirlik") to Words(
            "each platform’s own: TalkBack, VoiceOver, the keyboard, the touch targets",
            "her platformun kendisi: TalkBack, VoiceOver, klavye, dokunma hedefleri"
        ),
        Words("Documents", "Belgeler") to Words(
            "one site and one MCP server, every page with each library’s code",
            "tek site ve tek MCP sunucusu, her sayfa her kütüphanenin koduyla"
        )
    )
    val DiffRows = listOf(
        Words("Language and toolkit", "Dil ve araç takımı") to listOf(
            Words("Kotlin, Compose Multiplatform", "Kotlin, Compose Multiplatform"),
            Words("Kotlin, Jetpack Compose", "Kotlin, Jetpack Compose"),
            Words("Swift, SwiftUI", "Swift, SwiftUI")
        ),
        Words("Platforms", "Platformlar") to listOf(
            Words("Android, iOS, desktop, web", "Android, iOS, masaüstü, web"),
            Words("Android", "Android"),
            Words("iOS", "iOS")
        )
    )

    // A phone's table, as two lists.
    val SameInAll = Words("Same in all three", "Üçünde de aynı")
    val WhatDiffers = Words("What differs", "Ne farklı")
    val Differs = listOf(
        Words(
            "Kotlin and Compose Multiplatform: Android, iOS, desktop, web.",
            "Kotlin ve Compose Multiplatform: Android, iOS, masaüstü, web."
        ),
        Words("Kotlin and Jetpack Compose: Android.", "Kotlin ve Jetpack Compose: Android."),
        Words("Swift and SwiftUI: iOS.", "Swift ve SwiftUI: iOS.")
    )

    /** A desktop's line on the tokens: one file the three would be made from, still being weighed. */
    val Tokens = Words("Being weighed, one source of tokens:", "Tartılan, tek bir token kaynağı:")
    const val TOKENS_FILE = "shopeers.tokens.json"
    val TokenOutputs = listOf("ShopeersPreset.kt", "ShopeersPreset.swift", "shopeers.css")

    /** The Kotlin sample, its strings from [res]: `Res` in a multiplatform project, `R` in an Android one. */
    private fun kotlin(res: String) = listOf(
        CodeLine("PantrixTheme(preset = ShopeersPreset) {", TokenKind.Function),
        CodeLine("    PantrixTextArea(", TokenKind.Function),
        CodeLine("        state = note,"),
        CodeLine("        label = stringResource($res.string.note),"),
        CodeLine("        maxLength = $NOTE_LENGTH,", TokenKind.Number),
        CodeLine("        tagKey = \"courierNote\"", TokenKind.StringLiteral),
        CodeLine("    )"),
        CodeLine("}")
    )

    /** The Swift sample, its label [label]. */
    private fun swift(label: String) = listOf(
        CodeLine("PantrixTheme(preset: .shopeers) {", TokenKind.Function),
        CodeLine("    PantrixTextArea(", TokenKind.Function),
        CodeLine("        \"$label\",", TokenKind.StringLiteral),
        CodeLine("        text: \$note,"),
        CodeLine("        maxLength: $NOTE_LENGTH", TokenKind.Number),
        CodeLine("    )"),
        CodeLine("    .tagKey(\"courierNote\")", TokenKind.StringLiteral),
        CodeLine("}")
    )
}

/**
 * One of the three libraries.
 *
 * @property project the project it is for.
 * @property name what it is called.
 * @property body what it is.
 * @property availability whether it can be taken today.
 * @property targets the platforms it runs on.
 * @property file where its sample stands.
 * @property code its sample, given the label a Swift field is called by.
 */
internal class Library(
    val project: Words,
    val name: Words,
    val body: Words,
    val availability: Availability,
    val targets: List<Words>,
    val file: String,
    val code: (String) -> List<CodeLine>
)

/**
 * Whether a library can be taken today, which says its status and what its sample is.
 *
 * @property status what its card says it is.
 * @property note what its sample is: today's code or a sketch.
 */
internal enum class Availability(
    val status: Words,
    val note: Words
) {
    Today(
        Words("Available · ${LibraryFacts.VERSION}", "Kullanılabilir · ${LibraryFacts.VERSION}"),
        Words("Today", "Bugün")
    ),
    Later(Words("Later", "Sonra"), Words("A sketch", "Bir taslak"))
}
