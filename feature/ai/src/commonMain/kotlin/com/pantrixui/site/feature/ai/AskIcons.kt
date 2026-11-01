package com.pantrixui.site.feature.ai

import androidx.compose.ui.graphics.vector.ImageVector
import com.pantrixui.foundation.icons.PantrixIcons

/**
 * The icons of `PantrixIcons` by their names, as the server answers with them: what the page draws an icon by, with
 * the library itself. An icon the library adds after the site's version is not here, and its tile stays empty.
 */
internal object AskIcons {
    /** The icon named [name], or null for one the site's version of the library does not have. */
    fun of(name: String): ImageVector? = BY_NAME[name]

    private val BY_NAME: Map<String, ImageVector> = mapOf(
        "Check" to PantrixIcons.Check,
        "Close" to PantrixIcons.Close,
        "ChevronDown" to PantrixIcons.ChevronDown,
        "ChevronForward" to PantrixIcons.ChevronForward,
        "ChevronBack" to PantrixIcons.ChevronBack,
        "FirstPage" to PantrixIcons.FirstPage,
        "LastPage" to PantrixIcons.LastPage,
        "ArrowBack" to PantrixIcons.ArrowBack,
        "ArrowForward" to PantrixIcons.ArrowForward,
        "Calendar" to PantrixIcons.Calendar,
        "Clock" to PantrixIcons.Clock,
        "Refresh" to PantrixIcons.Refresh,
        "ZoomOut" to PantrixIcons.ZoomOut,
        "ZoomIn" to PantrixIcons.ZoomIn,
        "MoreHorizontal" to PantrixIcons.MoreHorizontal,
        "Minus" to PantrixIcons.Minus,
        "Menu" to PantrixIcons.Menu,
        "Plus" to PantrixIcons.Plus,
        "Info" to PantrixIcons.Info,
        "Success" to PantrixIcons.Success,
        "Warning" to PantrixIcons.Warning,
        "Error" to PantrixIcons.Error,
        "Inbox" to PantrixIcons.Inbox,
        "Star" to PantrixIcons.Star,
        "Folder" to PantrixIcons.Folder,
        "Person" to PantrixIcons.Person,
        "Search" to PantrixIcons.Search,
        "Download" to PantrixIcons.Download,
        "Upload" to PantrixIcons.Upload,
        "File" to PantrixIcons.File,
        "Copy" to PantrixIcons.Copy,
        "Eye" to PantrixIcons.Eye,
        "EyeOff" to PantrixIcons.EyeOff,
        "Pause" to PantrixIcons.Pause,
        "Play" to PantrixIcons.Play
    )
}
