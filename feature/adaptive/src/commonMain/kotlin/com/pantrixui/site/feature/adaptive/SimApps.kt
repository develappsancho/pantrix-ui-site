package com.pantrixui.site.feature.adaptive

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.container.listdetail.PantrixListDetailScaffold
import com.pantrixui.components.container.listdetail.rememberListDetailState
import com.pantrixui.components.container.scaffold.PantrixScaffold
import com.pantrixui.components.container.scaffold.ScaffoldNavigationPlacement
import com.pantrixui.components.container.tabletop.PantrixTabletopLayout
import com.pantrixui.components.container.tabletop.TabletopArrangement
import com.pantrixui.components.container.tabletop.TabletopControls
import com.pantrixui.components.elements.button.ButtonVariant
import com.pantrixui.components.elements.button.PantrixButton
import com.pantrixui.components.elements.icon.PantrixIcon
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.components.navigation.common.NavigationItem
import com.pantrixui.components.navigation.navigationbar.PantrixNavigationBar
import com.pantrixui.components.navigation.navigationrail.PantrixNavigationRail
import com.pantrixui.components.navigation.sidebar.PantrixSidebar
import com.pantrixui.components.navigation.sidebar.SidebarGroup
import com.pantrixui.components.navigation.sidebar.SidebarItem
import com.pantrixui.foundation.icons.PantrixIcons
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.designsystem.siteClickable
import com.pantrixui.site.core.model.Language

/**
 * A shop's inbox as Pantrix lays it out in the window it is drawn in: its navigation where Pantrix's scaffold puts it,
 * and its messages in Pantrix's list-detail scaffold, which decides by its own room how many panes it shows.
 */
@Composable
internal fun InboxApp(
    language: Language,
    sidebar: Boolean
) {
    var selected by rememberSaveable { mutableStateOf("inbox") }
    val keys = listOf("inbox", "orders", "people", "settings")
    val icons = listOf(PantrixIcons.Inbox, PantrixIcons.File, PantrixIcons.Person, PantrixIcons.Star)
    val items = keys.mapIndexed { index, key ->
        NavigationItem(key = key, label = AdaptiveSay.Navigation[index].of(language), icon = icons[index], tagKey = key)
    }
    val inbox = rememberListDetailState()
    PantrixScaffold(
        navigation = { placement, insets ->
            when {
                placement == ScaffoldNavigationPlacement.Bottom -> {
                    PantrixNavigationBar(items, selected, { selected = it }, insets = insets, tagKey = "simBar")
                }

                sidebar -> {
                    PantrixSidebar(
                        groups = listOf(
                            SidebarGroup(
                                items.mapIndexed { index, item ->
                                    SidebarItem(
                                        key = item.key,
                                        label = item.label,
                                        icon = item.icon,
                                        detail = SIDEBAR_COUNTS[index],
                                        tagKey = item.key
                                    )
                                }
                            )
                        ),
                        selected = selected,
                        onSelect = { selected = it },
                        expanded = emptySet(),
                        onExpandedChange = {},
                        adaptive = false,
                        header = { PantrixText(text = AdaptiveSay.ShopAdmin.of(language), style = fontWeightBold()) },
                        insets = insets,
                        tagKey = "simSidebar"
                    )
                }

                else -> {
                    PantrixNavigationRail(items, selected, { selected = it }, insets = insets, tagKey = "simRail")
                }
            }
        },
        windowInsets = WindowInsets(0)
    ) { padding ->
        PantrixListDetailScaffold(
            state = inbox,
            list = { Messages(language) { inbox.select(it) } },
            detail = { key -> Message(language, key.toInt()) },
            modifier = Modifier.padding(padding),
            emptyDetail = {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    PantrixText(
                        text = AdaptiveSay.ChooseAMessage.of(language),
                        color = PantrixTheme.colors.textSecondary
                    )
                }
            },
            extra = { History(language) },
            listTitle = AdaptiveSay.Inbox.of(language),
            detailTitle = { key -> AdaptiveSay.Mail[key.toInt()].subject.of(language) },
            extraTitle = AdaptiveSay.History.of(language),
            tagKey = "simInbox"
        )
    }
}

@Composable
private fun fontWeightBold() = PantrixTheme.typography.title.copy(fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)

/** The inbox's messages, each a row that opens it. */
@Composable
private fun Messages(
    language: Language,
    onOpen: (String) -> Unit
) {
    val line = PantrixTheme.colors.divider
    Column(Modifier.fillMaxWidth()) {
        AdaptiveSay.Mail.forEachIndexed { index, mail ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .siteClickable(tag = "mail$index", role = Role.Button, shape = RectangleShape) { onOpen("$index") }
                    .drawBehind {
                        drawLine(line, Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx())
                    }
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Avatar(mail, 40.dp)
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PantrixText(
                            text = mail.from.of(language),
                            style = PantrixTheme.typography.label.copy(
                                fontWeight = if (index == 0) FontWeight.ExtraBold else FontWeight.SemiBold
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        PantrixText(
                            text = mail.time.of(language),
                            style = PantrixTheme.typography.footnote,
                            color = PantrixTheme.colors.textSecondary
                        )
                    }
                    PantrixText(
                        text = mail.subject.of(language),
                        style = PantrixTheme.typography.label,
                        color = PantrixTheme.colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/** A message, open: who sent it and when, what it says, and what can be done with it. */
@Composable
private fun Message(
    language: Language,
    index: Int
) {
    val mail = AdaptiveSay.Mail[index]
    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Avatar(mail, 36.dp)
            Column {
                PantrixText(
                    text = mail.from.of(language),
                    style = PantrixTheme.typography.label.copy(fontWeight = FontWeight.Bold)
                )
                PantrixText(
                    text = mail.time.of(language),
                    style = PantrixTheme.typography.footnote,
                    color = PantrixTheme.colors.textSecondary
                )
            }
        }
        PantrixText(text = mail.body.of(language), color = PantrixTheme.colors.textSecondary)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PantrixButton(text = AdaptiveSay.TrackTheParcel.of(language), onClick = {}, tagKey = "simTrack")
            PantrixButton(text = AdaptiveSay.Reply.of(language), onClick = {
            }, variant = ButtonVariant.Outline, tagKey = "simReply")
        }
        Box(
            Modifier.fillMaxWidth().height(
                120.dp
            ).background(PantrixTheme.colors.disabledContainer, RoundedCornerShape(14.dp))
        )
    }
}

/** An order's history, the third pane of a wide window. */
@Composable
private fun History(language: Language) {
    val steps = listOf(
        AdaptiveSay.Shipped to AdaptiveSay.Today,
        AdaptiveSay.Packed to AdaptiveSay.YesterdayLate,
        AdaptiveSay.Paid to AdaptiveSay.YesterdayEarly
    )
    Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        steps.forEachIndexed { index, (what, time) ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    Modifier
                        .padding(top = 5.dp)
                        .size(12.dp)
                        .background(
                            if (index ==
                                0
                            ) {
                                PantrixTheme.colors.primary.color
                            } else {
                                PantrixTheme.colors.borderStrong
                            },
                            CircleShape
                        )
                )
                Column {
                    PantrixText(
                        text = what.of(language),
                        style = PantrixTheme.typography.label.copy(fontWeight = FontWeight.Bold)
                    )
                    PantrixText(
                        text = time.of(language),
                        style = PantrixTheme.typography.footnote,
                        color = PantrixTheme.colors.textSecondary
                    )
                }
            }
        }
    }
}

/** A sender's circle, its first letter in it. */
@Composable
private fun Avatar(
    mail: Mail,
    size: Dp
) {
    Box(Modifier.size(size).background(Color(mail.avatar), CircleShape), contentAlignment = Alignment.Center) {
        PantrixText(
            text = mail.from.english.take(1),
            style = PantrixTheme.typography.label.copy(fontWeight = FontWeight.ExtraBold),
            color = Color.White
        )
    }
}

/**
 * A video call as Pantrix's tabletop layout draws it: the picture above a table's fold and its controls, large, under
 * it; side by side in a book; the controls over the picture's foot when the device is flat.
 */
@Composable
internal fun VideoApp(language: Language) {
    PantrixTabletopLayout(
        content = {
            Box(Modifier.fillMaxSize().background(CALL_GROUND), contentAlignment = Alignment.Center) {
                Box(Modifier.size(92.dp).background(CALL_FACE, CircleShape))
                PantrixText(
                    text = AdaptiveSay.Meeting.of(language),
                    style = PantrixTheme.typography.label.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .background(CALL_LABEL, RoundedCornerShape(percent = 50))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        },
        controls = {
            val large = arrangement != TabletopArrangement.Flat
            Row(
                Modifier
                    .fillMaxWidth()
                    .then(if (large) Modifier.fillMaxSize().background(CALL_NIGHT) else Modifier.background(CALL_SHADE))
                    .padding(vertical = if (large) 0.dp else 20.dp),
                horizontalArrangement = Arrangement.spacedBy(if (large) 28.dp else 20.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CALL_ICONS.forEachIndexed { index, icon ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            Modifier
                                .size(if (large) 76.dp else 58.dp)
                                .background(
                                    if (index ==
                                        CALL_ICONS.lastIndex
                                    ) {
                                        CALL_LEAVE
                                    } else {
                                        CALL_BUTTON
                                    },
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            PantrixIcon(
                                imageVector = icon,
                                contentDescription = AdaptiveSay.CallButtons[index].of(language),
                                tint = Color.White,
                                size = 26.dp
                            )
                        }
                        if (large) {
                            PantrixText(
                                text = AdaptiveSay.CallButtons[index].of(language),
                                style = PantrixTheme.typography.footnote.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize(),
        flatControls = TabletopControls.Overlay,
        tagKey = "simCall"
    )
}

/** What each of the sidebar's items counts. */
private val SIDEBAR_COUNTS = listOf("6", "24", null, null)

private val CALL_ICONS = listOf(PantrixIcons.Info, PantrixIcons.Eye, PantrixIcons.Upload, PantrixIcons.Close)
private val CALL_GROUND = Color(0xFF1D3362)
private val CALL_FACE = Color(0xFF5386FF)
private val CALL_LABEL = Color(0x73000000)
private val CALL_NIGHT = Color(0xFF0B0E15)
private val CALL_SHADE = Color(0x7305070A)
private val CALL_BUTTON = Color(0xFF333B4E)
private val CALL_LEAVE = Color(0xFFC42A55)
