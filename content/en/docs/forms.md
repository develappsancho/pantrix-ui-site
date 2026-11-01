---
title: Forms
description: How a form holds its fields, their rules and its submission, which input takes what, and what keeps a keyboard out of a reader's way on every platform.
section: Getting started
---

## A form in three parts {#form}

:::steps
## Set up its fields {#fields toc="The fields"}

`FormState` holds a form together: which field is in error, what waits while it is sent, what Enter does. `form.field(id, initial, rules…)` sets a field up; its `id` is its name in the form and the automation tag of the component bound to it.

```kotlin SignUpForm.kt
val form = rememberFormState()
val email = form.field(
    "email",
    initial = "",
    Rules.notBlank(stringResource(Res.string.email_required)),
    Rules.matches(EMAIL, stringResource(Res.string.email_invalid))
)
val password = form.field("password", initial = "", Rules.minLength(8, stringResource(Res.string.password_short)))
val terms = form.field("terms", initial = false, Rules.mustBeTrue(stringResource(Res.string.terms_required)))
```

## Bind the components to them {#bind toc="The components"}

A component given a `field` takes no `tagKey`: it is tagged by the field's `id`.

```kotlin SignUpForm.kt
PantrixTextField(
    field = email,
    form = form,
    label = stringResource(Res.string.email),
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
)
PantrixSecureTextField(field = password, form = form, label = stringResource(Res.string.password))
PantrixCheckbox(field = terms, label = stringResource(Res.string.accept_terms))
```

## Send it {#submit toc="Sending"}

`form.submit { … }` checks every field. Where one fails, the focus goes to the first in error and the block does not run; while it runs, a button given the `form` shows that it is loading and takes no second press.

```kotlin SignUpForm.kt
PantrixButton(
    text = stringResource(Res.string.sign_up),
    onClick = { scope.launch { form.submit { onSignUp(email.value, password.value) } } },
    form = form,
    tagKey = "sign_up"
)
```
:::

## The rules {#rules}

:::table What a rule says
- `Rules.notBlank`: The text is not empty or only spaces
- `Rules.required`: A value is chosen: a select, a date
- `Rules.mustBeTrue`: A consent is given
- `Rules.minLength`, `Rules.maxLength`: The text is long enough, or short enough
- `Rules.matches`: The whole value matches a pattern
- `Rules.satisfies`: A rule that looks at something else: a password typed twice
:::

An error does not show before the field is touched: when the focus leaves it, or when the form is sent. A touched field is checked again at every change.

## Which input takes what {#inputs}

:::table By value
| Value | Component |
| --- | --- |
| A line of text | `PantrixTextField`, `PantrixFilledTextField` |
| Text of several lines | `PantrixTextArea`, `PantrixFilledTextArea` |
| A password | `PantrixSecureTextField` |
| Yes or no | `PantrixCheckbox`, `PantrixSwitchButton` |
| One of a few, all in view | `PantrixRadioGroup` |
| One of a list, or several | `PantrixSelect`, `PantrixMultiSelect` |
| A date, a time | `PantrixDateTextField`, `PantrixTimeTextField` |
:::

## The keyboard on every platform {#keyboard}

:::rows
- **Put away**: An iPhone's keyboard has no key that closes it from a field of several lines. `Modifier.clearFocusOnTap()` on the screen's root closes it on a tap outside: [Putting the keyboard away](/docs/guides/keyboard-dismissal).
- **Numbers**: An iPhone's number pad has no action key. `numberKeyboardOptions(imeAction = ImeAction.Done)` asks for one that has: [A number field and its action key](/docs/guides/number-keyboard).
- **Over the content**: A screen with fields keeps them clear of the keyboard with `Modifier.imePadding()` on its content.
- **Enter**: In a text field given the `form`, the keyboard's action key sends the form; in a text area it is a new line, and Ctrl or Cmd with Enter sends.
:::

## Where next {#next}

:::cards
- [Testing](/docs/testing): Find a form's fields by their tags.
- [Text Area](/components/text-area): Lines, a soft limit and the handle.
- [Date and time](/docs/guides/date-time): A day and a time without a date library.
- [Putting the keyboard away](/docs/guides/keyboard-dismissal): A tap outside that closes it.
:::
