package com.pantrixui.site.feature.components

import com.pantrixui.components.chart.common.ChartStyle
import com.pantrixui.components.chart.progresscircle.ProgressCircleStyle
import com.pantrixui.components.container.accordion.AccordionStyle
import com.pantrixui.components.container.alert.AlertStyle
import com.pantrixui.components.container.card.CardStyle
import com.pantrixui.components.container.listdetail.ListDetailScaffoldStyle
import com.pantrixui.components.container.page.PageScaffoldStyle
import com.pantrixui.components.container.scaffold.ScaffoldStyle
import com.pantrixui.components.container.showcase.ShowcaseScaffoldStyle
import com.pantrixui.components.container.supportingpane.SupportingPaneScaffoldStyle
import com.pantrixui.components.container.tab.TabsStyle
import com.pantrixui.components.container.topbar.TopBarStyle
import com.pantrixui.components.data.carousel.CarouselStyle
import com.pantrixui.components.data.codeblock.CodeBlockStyle
import com.pantrixui.components.data.datatable.DataTableStyle
import com.pantrixui.components.data.jsonview.JsonViewStyle
import com.pantrixui.components.data.pagination.PaginationStyle
import com.pantrixui.components.data.tag.TagStyle
import com.pantrixui.components.data.treeview.TreeViewStyle
import com.pantrixui.components.date.datepicker.DatePickerStyle
import com.pantrixui.components.date.elasticdatepicker.ElasticDatePickerStyle
import com.pantrixui.components.date.timepicker.TimePickerStyle
import com.pantrixui.components.elements.avatar.AvatarStyle
import com.pantrixui.components.elements.badge.BadgeStyle
import com.pantrixui.components.elements.button.ButtonStyle
import com.pantrixui.components.elements.buttongroup.ButtonGroupStyle
import com.pantrixui.components.elements.checkbox.CheckboxStyle
import com.pantrixui.components.elements.chip.ChipStyle
import com.pantrixui.components.elements.filedrop.FileDropStyle
import com.pantrixui.components.elements.filedrop.FileItemStyle
import com.pantrixui.components.elements.icon.IconStyle
import com.pantrixui.components.elements.radiobutton.RadioStyle
import com.pantrixui.components.elements.segmentbutton.SegmentButtonStyle
import com.pantrixui.components.elements.spinner.SpinnerStyle
import com.pantrixui.components.elements.switchbutton.SwitchStyle
import com.pantrixui.components.inputs.filledtextfield.FilledTextFieldStyle
import com.pantrixui.components.inputs.textfield.TextFieldStyle
import com.pantrixui.components.navigation.breadcrumb.BreadcrumbStyle
import com.pantrixui.components.navigation.navigationbar.NavigationBarStyle
import com.pantrixui.components.navigation.navigationrail.NavigationRailStyle
import com.pantrixui.components.navigation.sidebar.SidebarStyle
import com.pantrixui.components.overlay.commandpalette.CommandPaletteStyle
import com.pantrixui.components.overlay.dialog.DialogStyle
import com.pantrixui.components.overlay.menu.MenuStyle
import com.pantrixui.components.overlay.popover.PopoverStyle
import com.pantrixui.components.overlay.select.SelectStyle
import com.pantrixui.components.overlay.sheet.SheetStyle
import com.pantrixui.components.overlay.snackbar.SnackbarStyle
import com.pantrixui.components.overlay.tooltip.TooltipStyle
import com.pantrixui.foundation.platform.PlatformType
import com.pantrixui.foundation.token.style.PantrixStyleKey

/**
 * The library's component styles by their names, each its key: what holds the style's preset on each platform, which a
 * component's page shows side by side. A style the library adds is added here.
 */
internal val STYLE_KEYS: Map<String, PantrixStyleKey<*>> = mapOf(
    "AccordionStyle" to AccordionStyle,
    "AlertStyle" to AlertStyle,
    "AvatarStyle" to AvatarStyle,
    "BadgeStyle" to BadgeStyle,
    "BreadcrumbStyle" to BreadcrumbStyle,
    "ButtonGroupStyle" to ButtonGroupStyle,
    "ButtonStyle" to ButtonStyle,
    "CardStyle" to CardStyle,
    "CarouselStyle" to CarouselStyle,
    "ChartStyle" to ChartStyle,
    "CheckboxStyle" to CheckboxStyle,
    "ChipStyle" to ChipStyle,
    "CodeBlockStyle" to CodeBlockStyle,
    "CommandPaletteStyle" to CommandPaletteStyle,
    "DataTableStyle" to DataTableStyle,
    "DatePickerStyle" to DatePickerStyle,
    "DialogStyle" to DialogStyle,
    "ElasticDatePickerStyle" to ElasticDatePickerStyle,
    "FileDropStyle" to FileDropStyle,
    "FileItemStyle" to FileItemStyle,
    "FilledTextFieldStyle" to FilledTextFieldStyle,
    "IconStyle" to IconStyle,
    "JsonViewStyle" to JsonViewStyle,
    "ListDetailScaffoldStyle" to ListDetailScaffoldStyle,
    "MenuStyle" to MenuStyle,
    "NavigationBarStyle" to NavigationBarStyle,
    "NavigationRailStyle" to NavigationRailStyle,
    "PageScaffoldStyle" to PageScaffoldStyle,
    "PaginationStyle" to PaginationStyle,
    "PopoverStyle" to PopoverStyle,
    "ProgressCircleStyle" to ProgressCircleStyle,
    "RadioStyle" to RadioStyle,
    "ScaffoldStyle" to ScaffoldStyle,
    "SegmentButtonStyle" to SegmentButtonStyle,
    "SelectStyle" to SelectStyle,
    "SheetStyle" to SheetStyle,
    "ShowcaseScaffoldStyle" to ShowcaseScaffoldStyle,
    "SidebarStyle" to SidebarStyle,
    "SnackbarStyle" to SnackbarStyle,
    "SpinnerStyle" to SpinnerStyle,
    "SupportingPaneScaffoldStyle" to SupportingPaneScaffoldStyle,
    "SwitchStyle" to SwitchStyle,
    "TabsStyle" to TabsStyle,
    "TagStyle" to TagStyle,
    "TextFieldStyle" to TextFieldStyle,
    "TimePickerStyle" to TimePickerStyle,
    "TooltipStyle" to TooltipStyle,
    "TopBarStyle" to TopBarStyle,
    "TreeViewStyle" to TreeViewStyle
)

/**
 * What each field of the style [name] is on each platform, as its preset gives it: a field's value by its name, a
 * style inside it by its own fields after its name, `medium.height`. Null for a style the site does not know.
 */
internal fun presetValues(name: String): Map<PlatformType, Map<String, String>>? {
    val key = STYLE_KEYS[name] ?: return null
    return PlatformType.entries.associateWith { platform -> StyleValues.of(key.presets[platform].toString()) }
}
