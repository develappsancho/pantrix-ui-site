# Overlays

Dialogs, sheets, menus, popovers, tooltips, snackbars, the command palette and a modal of the app's own: which one a
task takes, and the contract they share. The pages `dialog`, `modal`, `sheet`, `menu`, `popover`, `tooltip`,
`snackbar`, `command-palette` and `adaptive-fold-overlays` have every example.

Contents: Which one · The shared contract · Each one · Snackbars · An overlay of the app's own

## Which one

| Task | Use |
| :--- | :--- |
| Ask for a decision, or give news that must not go unseen | `PantrixDialog` |
| A short task of the current screen: sorting, a field or two, the details of what was pressed | `PantrixSheet` |
| The actions of one element: a "more" button, a row's actions | `PantrixMenu` with `PantrixMenuItem`s |
| More about one element than it can show: a status, a few controls | `PantrixPopover` |
| The name of an icon-only control | `PantrixTooltip` |
| News of what the app has just done, with one action at the most: "Conversation archived", "Undo" | `PantrixSnackbarState.show` |
| Every command of the app, found by its name and run from the keyboard | `PantrixCommandPalette` |
| A layer none of these draws: a search with filters of its own, a picture shown large, an editor that fills a phone | `PantrixModal` |
| One choice of a list in a form | `PantrixSelect` ([forms.md](forms.md)) |

## The shared contract

- **The screen holds whether it is shown.** Each takes `visible` and `onDismissRequest`. A press on the scrim or
  outside, back and Escape call `onDismissRequest`; the overlay never closes itself. The screen sets `visible` to false.
- **It stays in the tree.** A closed overlay composes nothing, and leaves with the theme's motion. Taken out of the
  composition while it is open, it goes at once, with no motion: keep it in the tree and change `visible`.
- **Modal or not.** A dialog, a sheet, the command palette and `PantrixModal` are modal: nothing behind them takes a press, and Tab
  stays inside. A menu, a popover and a tooltip have no scrim.
- **Focus.** Opened with the keyboard, the focus goes into the overlay; opened by touch or a click, it stays where it
  was. When the overlay closes, the focus comes back to what opened it.
- **Not dismissible.** `dismissible = false` leaves only the overlay's own controls as a way out.
- **On a fold.** On a foldable held half open, every overlay keeps to a half of the fold by itself
  ([layout.md](layout.md)).

```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.pantrixui.components.elements.button.ButtonVariant
import com.pantrixui.components.elements.button.PantrixButton
import com.pantrixui.components.overlay.dialog.PantrixDialog
import org.jetbrains.compose.resources.stringResource

@Composable
fun DeleteDraft(onDelete: () -> Unit) {
    var asking by rememberSaveable { mutableStateOf(false) }
    PantrixButton(
        text = stringResource(Res.string.delete),
        onClick = { asking = true },
        variant = ButtonVariant.Destructive,
        tagKey = "delete"
    )
    PantrixDialog(
        visible = asking,
        onDismissRequest = { asking = false },
        title = stringResource(Res.string.delete_draft_title),
        message = stringResource(Res.string.delete_draft_message),
        actions = {
            // The safe choice first: a dialog opened with the keyboard focuses its first action.
            PantrixButton(
                text = stringResource(Res.string.cancel),
                onClick = { asking = false },
                variant = ButtonVariant.Text,
                tagKey = "cancel"
            )
            PantrixButton(
                text = stringResource(Res.string.delete),
                onClick = {
                    asking = false
                    onDelete()
                },
                variant = ButtonVariant.Destructive,
                tagKey = "confirm_delete"
            )
        },
        tagKey = "delete_draft"
    )
}
```

## Each one

- **Dialog.** `title`, `message`, `actions` (end-aligned buttons), an `icon` above the title, and `content` for a field
  or a list of choices under the message.
- **Sheet.** `edge = SheetEdge.Bottom` (the default, a drag down closes it), `SheetEdge.End` (a side sheet with a close
  button), `SheetEdge.Start` (the same from the start edge: an app's menu in a narrow window) or `SheetEdge.Adaptive`
  (a full-screen sheet from the bottom in a narrow window, a side sheet in a wider one). `halfOpen` opens a tall sheet
  halfway first; `fullScreen` makes it as tall as the window. A long list goes in a `LazyColumn` with
  `scrollContent = false`.
- **Menu.** `anchor` is the control it opens beside; `side` and `alignment` place it (`PopoverSide`,
  `PopoverAlignment`). A row's `onClick` is called after the menu was asked to close: a row says only what it does.
  `destructive = true` draws a row in the `danger` colour, and `supportingText` gives it a second, quieter line under
  its label. A `PantrixDivider` parts groups. In a narrow window the menu
  is a sheet: `presentation = OverlayPresentation.Adaptive`, the default.
- **Popover.** `anchor`, `title` (every popover of more than a line has one) and `content`. Its `presentation` is a
  popover in every window unless it is given `OverlayPresentation.Adaptive`.
- **Tooltip.** `PantrixTooltip(text = …) { PantrixIconButton(…) }`: it shows on a pointer's rest, on keyboard focus
  and after a long press, and is hidden from a screen reader. It says what the control's `contentDescription` already
  says, never anything new.
- **Command palette.** `PantrixCommandPalette` with `PantrixCommand(key, label, group, icon, shortcut, keywords)`,
  and a second line under the label with `supportingText`, which a search finds too;
  `onRun` closes the palette before the command does its work. A `PantrixShortcut` is bound with `Modifier.onShortcut`,
  and every command's at once with `Modifier.commandShortcuts`; `Modifier.shortcutRoot()` on the window's root hears
  them while nothing has the focus.

## Snackbars

A snackbar is asked for, not drawn: `PantrixSnackbarState.show` puts it in the queue and returns how it left, and a
`PantrixSnackbarHost` shows it. A `PantrixScaffold` given `snackbars` sets the host up and provides the queue to every
screen inside it as `LocalPantrixSnackbars`.

```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.pantrixui.components.elements.button.PantrixButton
import com.pantrixui.components.overlay.snackbar.LocalPantrixSnackbars
import com.pantrixui.components.overlay.snackbar.SnackbarResult
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

@Composable
fun ArchiveButton(onArchive: () -> Unit, onUndo: () -> Unit) {
    val snackbars = LocalPantrixSnackbars.current
    val scope = rememberCoroutineScope()
    PantrixButton(
        text = stringResource(Res.string.archive),
        onClick = {
            onArchive()
            scope.launch {
                val result = snackbars?.show(
                    message = getString(Res.string.archived),
                    actionLabel = getString(Res.string.undo),
                    tagKey = "archived"
                )
                if (result == SnackbarResult.ActionPerformed) onUndo()
            }
        },
        tagKey = "archive"
    )
}
```

- **One at a time,** in the order they were asked for. Without an action a snackbar leaves after four seconds
  (`SnackbarDuration.Short`); with one it stays until it is acted on or closed.
- **Tones.** `tone = SnackbarTone.Success` (or `Info`, `Warning`, `Danger`, `Primary`, `Neutral`, the default) picks
  the colour, the icon and how it is announced; only `Danger` interrupts a screen reader.
- **Placed by platform:** at the bottom on Android, at the top on iOS, in the bottom end corner on the desktop and the
  web.

## An overlay of the app's own

A modal layer of the app's own is `PantrixModal`: the scrim, back and Escape, the focus kept inside and given back,
the motion and the fold are its, and the content is the app's, with no fill, shape or width given to it.
`position = ModalPosition.Top` keeps a search's field where it is; `margin = PaddingValues()`, content that fills itself
and `scrimColor = PantrixTheme.colors.background` make it a page on a phone.

```kotlin
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pantrixui.components.container.surface.PantrixSurface
import com.pantrixui.components.overlay.modal.ModalPosition
import com.pantrixui.components.overlay.modal.PantrixModal
import com.pantrixui.foundation.theme.PantrixTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun SiteSearch(searching: Boolean, onClose: () -> Unit) {
    PantrixModal(
        visible = searching,
        onDismissRequest = onClose,
        position = ModalPosition.Top,
        paneTitle = stringResource(Res.string.search),
        tagKey = "search"
    ) {
        PantrixSurface(Modifier.widthIn(max = 640.dp), shape = PantrixTheme.shapes.lg, elevation = 8.dp) {
            SearchPanel(onOpen = onClose)
        }
    }
}
```

A layer that is not modal is built on what the library's are built on: `PantrixPortal` composes it at the window's
root, `PantrixModalLayer` makes it modal where it has to be, and `rememberOverlayWindow()` tells it its window and the
folds. The page `building-blocks` has them.
