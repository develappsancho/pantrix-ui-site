---
title: detekt ayarları
description: Pantrix'in detekt'inin iki ayar dosyası: kütüphanenin kendisininki ve demonunki, demoyu Pantrix'le yazılmış bir uygulama gibi denetleyen.
section: Rehberler
---

Pantrix kodunu detekt'le ve kendi kural setiyle, `PantrixUIRule` ile denetler ([detekt rehberi](/docs/guides/detekt)). Hangi kuralın nerede çalıştığını iki ayar dosyası tutar: kütüphanenin modülleri ilkiyle, demo ikincisiyle denetlenir. Kuralları benimseyen bir uygulama demonunkinden başlar.

Dosyalar kütüphanenin `config/detekt/demo.yml` ve `config/detekt/detekt.yml` dosyalarının kopyalarıdır.

## Demonun ayarı, bir uygulamanınki gibi {#demo-yml}

Demo, kütüphaneyi kullanan bir uygulama gibi denetlenir: yalnızca Pantrix'in bir uygulamanın da uyduğu kuralları çalışır, detekt'in kendi kuralları kapalıdır. `UntaggedControl` ve `ImageWithoutDescription` bulgularını bildirir ama derlemeyi durdurmaz.

```yaml demo.yml
# Demo'nun detekt ayarı. Demo, kütüphaneyi kullanan bir uygulama gibi denetlenir: yalnızca Pantrix'in bir uygulamanın
# da uyduğu kuralları çalışır, detekt'in kendi kuralları kapalı. NoMaterialImport, demo'nun Material'ı bağımlılık
# olarak tutup hiçbir şeyini kullanmaması kuralını tutar (CONTRIBUTING.md).

build:
  # Bu ikisi bildirir ama derlemeyi durdurmaz: ağırlığı sıfır olan bulgu maxIssues'a sayılmaz.
  weights:
    UntaggedControl: 0
    ImageWithoutDescription: 0

comments:
  active: false
complexity:
  active: false
coroutines:
  active: false
empty-blocks:
  active: false
exceptions:
  active: false
naming:
  active: false
performance:
  active: false
potential-bugs:
  active: false
style:
  active: false

PantrixUIRule:
  active: true
  NoMaterialImport:
    active: true
    # Material 3 örneği (samples/pantrix-material3) bir Material uygulamasıdır: Material'ı doğrudan kullanır.
    excludes: [ '**/samples/pantrix-material3/**' ]
  FixedSideLayout:
    active: true
  ImageWithoutDescription:
    active: true
    excludes: &tests
      - '**/commonTest/**'
      - '**/jvmTest/**'
      - '**/iosTest/**'
      - '**/jsTest/**'
      - '**/wasmJsTest/**'
      - '**/webTest/**'
      - '**/androidHostTest/**'
      - '**/androidDeviceTest/**'
  UntaggedControl:
    active: true
    excludes: *tests
```

## Kütüphanenin ayarı {#detekt-yml}

Kütüphanenin modülleri detekt'in kurallarıyla ve projenin kendi kurallarıyla denetlenir; her bulgu derlemeyi durdurur, bulgu ya düzeltilir ya da gerekçesi aynı satırda yazılı bir `@Suppress` olur. Bir uygulamanın tuttuğu iki kural, `ImageWithoutDescription` ve `UntaggedControl`, burada kapalıdır.

```yaml detekt.yml
# PantrixUI detekt konfigürasyonu

comments:
  active: true
  UndocumentedPublicClass:
    active: true
    excludes: &notPublished
      - '**/app/**'
      - '**/test/**'
      - '**/androidTest/**'
      - '**/commonTest/**'
      - '**/jsTest/**'
      - '**/jvmTest/**'
      - '**/iosTest/**'
      - '**/wasmJsTest/**'
      - '**/androidUnitTest/**'
      - '**/androidInstrumentedTest/**'
      # KMP Android kütüphane eklentisinin test kaynak kümeleri (yukarıdaki iki adın bugünkü karşılığı).
      - '**/androidHostTest/**'
      - '**/androidDeviceTest/**'
      # Bileşenlerin ekran görüntüsü sahneleri: testlerin çizdiği, yayımlanmayan kod.
      - '**/screenshotScenes/**'
  UndocumentedPublicFunction:
    active: true
    excludes: *notPublished
  UndocumentedPublicProperty:
    active: true
    excludes: *notPublished


naming:
  FunctionNaming:
    active: true
    # @Composable fonksiyonlar PascalCase — resmî Compose API kılavuzu.
    # Muafiyet değil; aracın kod tabanının konvansiyonunu tanıması.
    ignoreAnnotated: [ 'Composable', 'Preview' ]
    # Test adları snake_case (`a_theme_told_it_is_dark_is_dark`). detekt'in varsayılan dışlama listesi
    # androidHostTest'i tanımıyor; yayımlanmayan kodun listesi onun yerine geçiyor.
    excludes: *notPublished
  # Kapalı. Bir dosya, bileşenini onun tipleriyle birlikte taşır: Text.kt'de PantrixText ile TextRole.
  # Kural ya dosyanın adını değiştirmeyi ister, ki bu JVM'de dosya sınıfının (PlatformKt) adını ve
  # ABI'yi değiştirir, ya da birlikte okunan kodu ayrı dosyalara böler. KMP actual dosyalarında da
  # (`Platform.jvm.kt`) yanlış pozitif veriyordu.
  MatchingDeclarationName:
    active: false

style:
  # @Preview fonksiyonları GİRİŞ NOKTASI: IDE onları yansımayla çağırıyor,
  # kod içinden kimse çağırmıyor. "Kullanılmıyor" teknik olarak doğru,
  # anlamlı olarak yanlış.
  #
  # Bu muafiyet olmadan preview'lar `private` olamıyor ve `internal`
  # yazılıyordu — yani görünürlük, tasarım kararı değil kuraldan kaçış
  # olarak seçiliyordu. Muafiyet doğru yerde durunca preview'lar hak
  # ettikleri görünürlüğe (private) dönebiliyor.
  UnusedPrivateMember:
    active: true
    ignoreAnnotated: [ 'Preview' ]

  MagicNumber:
    active: true
    excludes:
      - '**/ui/foundation/**'
      # Testte sayı, saklanacak bir ayrıntı değil İDDİANIN KENDİSİ.
      # `assertEquals(37.sp, style.fontSize)` satırında 37'yi
      # EXPECTED_FONT_SIZE'a çevirmek, testin ne iddia ettiğini okuyucudan
      # gizler — kuralın korumaya çalıştığı şeyin tersi.
      #
      # DAR TUTULDU: yalnızca *Test.kt. Test kaynak kümesinin tamamını
      # dışlamak, test YARDIMCILARINDAKİ (fixture, builder) gerçek sihirli
      # sayıları da kaçırırdı.
      - '**/*Test.kt'

complexity:
  # Bir katalog/ekran dosyasında @Composable fonksiyonlar AYRI SORUMLULUK
  # değil, TEK SAYFANIN BÖLÜMLERİ. Kural tanrı-nesneye karşı; bölümlerine
  # ayrılmış bir sayfa tam tersi. Uzun tek gövde (LongMethod) ile çok sayıda
  # küçük bölüm arasında kural bizi ikincisinden cezalandırmamalı — ki
  # ikincisi doğru olan.
  TooManyFunctions:
    active: true
    ignoreAnnotatedFunctions: [ 'Composable', 'Preview' ]
    # Bir test sınıfında her test bir fonksiyon: ekran görüntüsü testinde bileşen başına bir tane. detekt'in varsayılan
    # dışlama listesi test kaynak kümelerini muaf tutuyor ama androidHostTest'i tanımıyor; FunctionNaming'deki gibi
    # yayımlanmayan kodun listesi onun yerine geçiyor.
    excludes: *notPublished

  LongParameterList:
    active: true
    # Stil sınıfları (ButtonStyle…) boyut ya da rol başına bir değer taşır: beş boyutu olan bir
    # bileşenin stili beş parametreyle başlar. Parametre sayısı tasarımın kendisi, dağınıklık değil.
    excludes: [ '**/ui/foundation/**', '**/*Style.kt' ]
    # Compose bileşenleri doğası gereği çok parametreli: içerik + stil +
    # modifier + davranış slotları. Eşik 6, bir Composable için yanlış
    # ölçü — PantrixText'in parametreleri (text, style, modifier, color,
    # maxLines, overflow, textAlign, …) fazlalık değil, API'nin kendisi.
    ignoreAnnotated: [ 'Composable' ]
    # Composable olmayan API'ler de çoğu zaman varsayılanlı parametrelerle büyür: snackbar'ın show()'u
    # (mesaj, eylem, süre, kapat düğmesi, ton, tagKey) gibi. Varsayılan eşikler (fonksiyonda 6,
    # constructor'da 7) bunları hemen yakalıyordu; biraz esnetildi. Eşik, kuralın tetiklendiği sayı:
    # fonksiyonda 7, constructor'da 8 parametreye kadar serbest.
    functionThreshold: 8
    constructorThreshold: 9

PantrixUIRule:
  # Projenin kendi kuralları (tooling/detekt-rules): CONTRIBUTING.md'deki kütüphane kuralları ve erişilebilirlik, dil
  # ve token kuralları. Kütüphanede her bulgu derlemeyi durdurur; bulgu ya düzeltilir ya gerekçesi yazılı bir
  # @Suppress olur. Baseline yok. Testler ve demo bu kuralların dışında: demo'nun kendi ayarı demo.yml'de.
  NoMaterialImport:
    active: true
    # Material 3 köprüsü (ui/material3) kütüphanede Material kullanan tek modüldür: işi iki temayı birbirine
    # çevirmek. Hiçbir kütüphane modülü ona bağlanmaz.
    excludes: [ '**/ui/material3/src/**' ]
  NoFoundationPrimitive:
    active: true
    # Sarmalayıcıların kendi yaşadığı yer muaf — oradan primitife
    # dokunmak işin tanımı: ui/foundation, BasicText'i saran PantrixText'in dosyası,
    # BasicTextField'ı saran PantrixTextField'ın dosyası ve BasicSecureTextField'ı saran
    # PantrixSecureTextField'ın dosyası.
    allowedPathPattern: '/ui/foundation/|/components/elements/text/Text\.kt$|/components/inputs/textfield/TextField\.kt$|/components/inputs/securetextfield/SecureTextField\.kt$'
  UnmarkedComponent:
    active: true
    includes: [ '**/ui/components/src/**' ]
    excludes: *notPublished
  SettingInStrings:
    active: true
    excludes: *notPublished
  TagFromVisibleText:
    active: true
    excludes: *notPublished
  RawTestTag:
    active: true
    excludes: *notPublished
    # Etiket fonksiyonları burada: pantrixTestTag, Compose'un testTag'ini çağıran tek yer.
    allowedPathPattern: '/foundation/testing/'
  EmptyComposableDefault:
    active: true
    excludes: *notPublished
  KDocNotASentence:
    active: true
    excludes: *notPublished
  UndocumentedParameter:
    active: true
    excludes: *notPublished
  LiteralTextInLibrary:
    active: true
    # Yayımlanmayan kod ve metin paketlerinin kendisi (l10n): bir paketin sözleri orada yazılır.
    excludes:
      - '**/app/**'
      - '**/test/**'
      - '**/androidTest/**'
      - '**/commonTest/**'
      - '**/jsTest/**'
      - '**/jvmTest/**'
      - '**/iosTest/**'
      - '**/wasmJsTest/**'
      - '**/androidUnitTest/**'
      - '**/androidInstrumentedTest/**'
      - '**/androidHostTest/**'
      - '**/androidDeviceTest/**'
      - '**/screenshotScenes/**'
      - '**/foundation/l10n/**'
  FixedSideLayout:
    active: true
    excludes: *notPublished
  SizeOutsideStyle:
    active: true
    # Bileşenler: foundation'ın kendisi token katmanı. Stil dosyalarında ölçü yazılır.
    includes: [ '**/ui/components/src/**' ]
    excludes: *notPublished
    allowedPathPattern: 'Style[^/]*\.kt$'
  ColorOutsideTokens:
    active: true
    includes: [ '**/ui/components/src/**' ]
    excludes: *notPublished
    allowedPathPattern: 'Style[^/]*\.kt$'
  # Bu ikisi kütüphaneyi kullanan uygulama içindir: demo.yml'de ve tüketicinin ayarında açılır.
  ImageWithoutDescription:
    active: false
  UntaggedControl:
    active: false
```
