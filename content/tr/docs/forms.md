---
title: Formlar
description: Bir formun alanlarını, kurallarını ve gönderilmesini nasıl tuttuğu, hangi girdinin neyi aldığı ve klavyeyi her platformda okurun yolundan neyin çektiği.
section: Başlarken
---

## Üç parçada bir form {#form}

:::steps
## Alanlarını kur {#fields toc="Alanlar"}

`FormState` bir formu bir arada tutar: hangi alanın hatalı olduğunu, gönderilirken neyin beklediğini, Enter'ın ne yaptığını. `form.field(id, initial, rules…)` bir alan kurar; `id`'si formdaki adı ve ona bağlanan bileşenin otomasyon etiketidir.

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

## Bileşenleri onlara bağla {#bind toc="Bileşenler"}

Bir `field` verilen bileşen `tagKey` almaz: alanın `id`'siyle etiketlenir.

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

## Gönder {#submit toc="Göndermek"}

`form.submit { … }` her alanı denetler. Biri geçmezse odak hatalı ilk alana gider ve blok çalışmaz; blok çalışırken `form` verilen düğme yüklendiğini gösterir ve ikinci bir basışı almaz.

```kotlin SignUpForm.kt
PantrixButton(
    text = stringResource(Res.string.sign_up),
    onClick = { scope.launch { form.submit { onSignUp(email.value, password.value) } } },
    form = form,
    tagKey = "sign_up"
)
```
:::

## Kurallar {#rules}

:::table Bir kuralın söyledikleri
- `Rules.notBlank`: Yazı boş ya da yalnızca boşluk değil
- `Rules.required`: Bir değer seçilmiş: bir seçim kutusu, bir tarih
- `Rules.mustBeTrue`: Bir onay verilmiş
- `Rules.minLength`, `Rules.maxLength`: Yazı yeterince uzun ya da yeterince kısa
- `Rules.matches`: Değerin tamamı bir kalıba uyuyor
- `Rules.satisfies`: Başka bir şeye bakan bir kural: iki kez yazılan bir parola
:::

Alana dokunulmadan hata görünmez: odak ondan ayrıldığında ya da form gönderildiğinde görünür. Dokunulmuş bir alan her değişiklikte yeniden denetlenir.

## Hangi girdi neyi alır {#inputs}

:::table Değere göre
| Değer | Bileşen |
| --- | --- |
| Bir satır yazı | `PantrixTextField`, `PantrixFilledTextField` |
| Birkaç satırlık yazı | `PantrixTextArea`, `PantrixFilledTextArea` |
| Bir parola | `PantrixSecureTextField` |
| Evet ya da hayır | `PantrixCheckbox`, `PantrixSwitchButton` |
| Hepsi görünen birkaç seçenekten biri | `PantrixRadioGroup` |
| Bir listeden biri ya da birkaçı | `PantrixSelect`, `PantrixMultiSelect` |
| Bir tarih, bir saat | `PantrixDateTextField`, `PantrixTimeTextField` |
:::

## Her platformda klavye {#keyboard}

:::rows
- **Kapatmak**: iPhone'un klavyesinde birkaç satırlık bir alandan onu kapatan bir tuş yoktur. Ekranın kökündeki `Modifier.clearFocusOnTap()` dışarıya bir dokunuşta onu kapatır: [Klavyeyi kapatma](/docs/guides/keyboard-dismissal).
- **Sayılar**: iPhone'un sayı tuş takımında eylem tuşu yoktur. `numberKeyboardOptions(imeAction = ImeAction.Done)` olanını ister: [Sayı alanı ve eylem tuşu](/docs/guides/number-keyboard).
- **İçeriğin üstünde**: Alanları olan bir ekran, içeriğindeki `Modifier.imePadding()` ile onları klavyeden uzak tutar.
- **Enter**: `form` verilen bir metin alanında klavyenin eylem tuşu formu gönderir; bir metin kutusunda yeni satırdır, Ctrl ya da Cmd ile Enter gönderir.
:::

## Sırada ne var {#next}

:::cards
- [Test](/docs/testing): Bir formun alanlarını etiketleriyle bulmak.
- [Metin kutusu](/components/text-area): Satırlar, yumuşak bir sınır ve tutamaç.
- [Tarih ve saat](/docs/guides/date-time): Tarih kütüphanesi olmadan bir gün ve bir saat.
- [Klavyeyi kapatma](/docs/guides/keyboard-dismissal): Onu kapatan dışarıya dokunuş.
:::
