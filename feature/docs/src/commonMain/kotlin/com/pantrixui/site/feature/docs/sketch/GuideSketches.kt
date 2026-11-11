package com.pantrixui.site.feature.docs.sketch

import androidx.compose.runtime.Composable

/** The guides' live sketches, by the key a guide's `:::aside` or `:::sketch` names. */
internal val GuideSketches: Map<String, @Composable () -> Unit> = mapOf(
    "keyboard-dismissal" to { KeyboardDismissalSketch() },
    "number-keyboard" to { NumberKeyboardSketch() },
    "text-order" to { TextOrderSketch() },
    "number-format" to { NumberFormatSketch() },
    "date-time" to { DateTimeSketch() },
    "typography" to { TypographySketch() },
    "clipboard" to { ClipboardSketch() },
    "diagnostics" to { DiagnosticsSketch() },
    "files" to { FilesSketch() },
    "scroll-padding" to { ScrollPaddingSketch() },
    "system-bars" to { SystemBarsSketch() },
    "modal" to { ModalSketch() },
    "testing" to { TestingSketch() },
    "window-title" to { WindowTitleSketch() },
    "pointer-icons" to { PointerIconsSketch() },
    "keyboard-shortcuts" to { KeyboardShortcutsSketch() },
    "adaptive" to { AdaptiveSketch() },
    "adaptive-usage" to { AdaptiveUsageSketch() },
    "web-fonts" to { WebFontsSketch() },
    "web-compatibility" to { WebCompatibilitySketch() },
    "ai" to { AiSketch() },
    "analytics" to { AnalyticsSketch() },
    "chart-export" to { ChartExportSketch() },
    "detekt" to { DetektSketch() },
    "material3" to { Material3Sketch() },
    "language" to { LanguageSketch() },
    "previews" to { PreviewsSketch() },
    "sharing" to { SharingSketch() },
    "semantics-tree" to { SemanticsTreeSketch() }
)
