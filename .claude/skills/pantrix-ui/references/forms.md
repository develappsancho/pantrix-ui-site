# Forms and inputs

How a form holds its fields, their rules and its submission, and which input takes what. The pages `form`,
`text-field`, `text-area`, `secure-text-field`, `checkbox`, `radio-button`, `switch-button`, `segment-button`,
`select`, `multi-select`, `date-text-field`, `time-text-field` and `file-drop` have every example.

Contents: A form · What binds to a field · Text fields · Text areas · Dates and times

## A form

`FormState` holds a form together: which field is in error, what waits while it is submitted, what Enter does.

```kotlin
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pantrixui.components.elements.button.PantrixButton
import com.pantrixui.components.elements.checkbox.PantrixCheckbox
import com.pantrixui.components.elements.form.Rules
import com.pantrixui.components.elements.form.field
import com.pantrixui.components.elements.form.rememberFormState
import com.pantrixui.components.inputs.securetextfield.PantrixSecureTextField
import com.pantrixui.components.inputs.textfield.PantrixTextField
import com.pantrixui.foundation.theme.PantrixTheme
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun SignUpForm(onSignUp: suspend (email: String, password: String) -> Unit) {
    val scope = rememberCoroutineScope()
    val form = rememberFormState()
    val email = form.field(
        "email",
        initial = "",
        Rules.notBlank(stringResource(Res.string.email_required)),
        Rules.matches(EMAIL, stringResource(Res.string.email_invalid))
    )
    val password = form.field("password", initial = "", Rules.minLength(8, stringResource(Res.string.password_short)))
    val terms = form.field("terms", initial = false, Rules.mustBeTrue(stringResource(Res.string.terms_required)))
    Column(
        modifier = Modifier.widthIn(max = 400.dp),
        verticalArrangement = Arrangement.spacedBy(PantrixTheme.spacing.md)
    ) {
        PantrixTextField(
            field = email,
            modifier = Modifier.fillMaxWidth(),
            form = form,
            label = stringResource(Res.string.email),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        PantrixSecureTextField(
            field = password,
            modifier = Modifier.fillMaxWidth(),
            form = form,
            label = stringResource(Res.string.password)
        )
        PantrixCheckbox(field = terms, label = stringResource(Res.string.accept_terms))
        PantrixButton(
            text = stringResource(Res.string.sign_up),
            onClick = { scope.launch { form.submit { onSignUp(email.value, password.value) } } },
            modifier = Modifier.fillMaxWidth(),
            form = form,
            tagKey = "sign_up"
        )
    }
}

private val EMAIL = Regex("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")
```

- **Fields.** `form.field(id, initial, rules…)` sets up a field. `field` is an extension: it is imported,
  `com.pantrixui.components.elements.form.field`. The `id` is the field's name in the form and the automation tag of
  the component bound to it: a bound component takes no `tagKey`.
- **Rules.** A `Rule` gives a value's error or null. `Rules` has `notBlank`, `required` (a nullable value: a select, a
  date), `mustBeTrue` (consent), `minLength`, `maxLength`, `matches` (the whole value) and `satisfies` (a rule that
  looks at something else: `Rules.satisfies<String>(message) { it == password.value }`). The first rule that fails
  gives the error.
- **When an error shows.** Not before the field is touched: when the focus leaves it, or when the form is submitted.
  A touched field is checked again at every change.
- **Submitting.** `form.submit { … }` checks every field; where one fails, the focus goes to the first in error and the
  block does not run. While it runs `isSubmitting` is true, and a button given the `form` shows as loading and takes no
  second press.
- **Enter.** In a text field given the `form`, the keyboard's action key submits it, with the work of the button given
  the `form`.
- **By hand.** `validateAll()`, `focusFirstError()`, `isValid`, `reset()`, and `field.reset(value)` for a value that
  comes from a server.
- **A component of its own.** One that is not bound shows `field.value`, calls `field.onValueChange(…)` and
  `field.onFocusLost()`, attaches `field.focusRequester`, writes `field.error`, and takes `tagKey = field.id`.

## What binds to a field

The text fields, `PantrixCheckbox`, `PantrixRadioGroup`, `PantrixSwitchButton`, `PantrixSelect`,
`PantrixMultiSelect` and the date and time fields have an overload that takes a `field`.

| Value | Component | The field's type |
| :--- | :--- | :--- |
| A line or a few lines of text | `PantrixTextField`, `PantrixFilledTextField` | `FormField<String>` |
| A comment, a message, a note: text of several lines | `PantrixTextArea`, `PantrixFilledTextArea` | `FormField<String>` |
| A password | `PantrixSecureTextField`, `PantrixFilledSecureTextField` | `FormField<String>` |
| Text that shrinks to fit its box: an amount, a note read whole | `PantrixAutoResizeTextField` | `FormField<String>` |
| Yes or no: a consent, a setting | `PantrixCheckbox`, `PantrixSwitchButton`; a settings row's switch at its end with `controlPosition = ControlPosition.End` and `Modifier.fillMaxWidth()` | `FormField<Boolean>` |
| One of a few, all in view | `PantrixRadioGroup` with `RadioOption`s | `FormField<T>` |
| One of a list | `PantrixSelect` with `SelectOption`s | `FormField<T>`, `T` nullable while nothing is chosen |
| Several of a list | `PantrixMultiSelect` | `FormField<Set<T>>` |
| A date, a time | `PantrixDateTextField`, `PantrixTimeTextField` | `FormField<PantrixDate?>`, `FormField<PantrixTime?>` |

Two or three options always in view, outside a form, are `PantrixSegmentButton` with `SegmentOption`s; picking files
is `PantrixFileDrop`.

## Text fields

- **Three overloads.** One takes a `TextFieldState` (`rememberTextFieldState()`), which holds the text, the selection
  and what the keyboard is composing: the keyboard is never handed a text that has moved on. One takes a `value` and
  an `onValueChange`, for code that keeps a `String`. One takes a form's `field`.
- **Label above.** `label` stays above the box and is the field's name for a screen reader; `placeholder` shows while
  it is empty. A field with no label gets its name from `contentDescription`.
- **Help and error.** `supportingText` is written under the box. In the overloads that are not bound, `error = true`
  draws the field in the `danger` role and `supportingText` says what to put right.
- **Length.** `maxLength` turns down an edit that would pass it and counts under the box.
- **Several lines.** `lineLimits = TextFieldLineLimits.MultiLine(…)`.
- **Keyboards.** `keyboardOptions` chooses the keyboard and its action key; a field of numbers that has an action asks
  for its keyboard with `numberKeyboardOptions(imeAction = …, decimal = …)` from `com.pantrixui.foundation.platform`,
  since an iPhone's number pad has no action key. `contentType` lets the platform fill the field in: an e-mail
  address, a password.
- **The keyboard over the content.** A screen with fields keeps them clear of the keyboard with
  `Modifier.imePadding()` on its content ([layout.md](layout.md)).

## Text areas

`PantrixTextArea` and `PantrixFilledTextArea` are for text of several lines. They take the text field's `style` and
`colors` (`TextFieldStyle`, `FilledTextFieldStyle`), and have the same three overloads.

- **Lines.** The box is `minLines` tall (3) and grows with its text to `maxLines` (8); past them the text scrolls,
  with a bar that shows how far. `Modifier.height` gives it a height of its own instead.
- **Enter is a new line,** always: no `ImeAction` is used, since on Android one takes the place of the key for a new
  line. A text that is sent takes `onSubmit`, called by Ctrl or Cmd and Enter on a hardware keyboard; give the screen
  a button that sends as well. Bound to a field with the `form`, Ctrl or Cmd and Enter submit the form.
- **A soft limit.** `maxLength` may be passed (`overLimit = TextLimit.Allow`, the default): the count says how far
  past, in words, the field is drawn in error, and a form is not submitted with it (the field's error is
  `PantrixStrings.tooLong`). `TextLimit.Block` turns an edit past the limit down, as a text field's limit does.
- **The handle.** On a desktop and on the web a handle in the bottom corner drags the box taller or shorter;
  `resizable = false` takes it away. A phone has none.
- **The keyboard on iOS** has no key that puts it away from a field of several lines: put `Modifier.clearFocusOnTap()`
  (`com.pantrixui.foundation.keyboard`) on the screen's root, and a tap outside the field closes it.

```kotlin
import com.pantrixui.components.inputs.textarea.PantrixTextArea
import com.pantrixui.foundation.keyboard.clearFocusOnTap

val comment = rememberTextFieldState()
Column(Modifier.fillMaxSize().clearFocusOnTap()) {
    PantrixTextArea(
        state = comment,
        label = stringResource(Res.string.comment),
        maxLength = 500,
        onSubmit = { viewModel.send(comment.text.toString()) },
        tagKey = "comment"
    )
}
```

## Dates and times

`PantrixDate` and `PantrixTime` are Pantrix's own: no date library is needed. `rememberToday()` is today by the screen's
clock, which a test gives. `PantrixDateTextField` is typed in the language's own order and opens a picker;
`PantrixDatePicker` is the calendar alone, `PantrixTimePicker` the clock alone, and `PantrixElasticDatePicker` a range
of time, absolute or relative ("last 15 minutes").
