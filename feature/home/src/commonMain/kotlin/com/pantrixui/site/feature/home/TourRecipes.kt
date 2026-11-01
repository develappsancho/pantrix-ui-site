package com.pantrixui.site.feature.home

import androidx.compose.ui.graphics.Color
import com.pantrixui.site.core.model.Words
import com.pantrixui.site.feature.home.generated.resources.Res
import com.pantrixui.site.feature.home.generated.resources.recipe_chickpea_salad
import com.pantrixui.site.feature.home.generated.resources.recipe_kebab
import com.pantrixui.site.feature.home.generated.resources.recipe_lemon_cake
import com.pantrixui.site.feature.home.generated.resources.recipe_lentil_soup
import com.pantrixui.site.feature.home.generated.resources.recipe_menemen
import com.pantrixui.site.feature.home.generated.resources.recipe_rice_pudding
import com.pantrixui.site.feature.home.generated.resources.recipe_stuffed_peppers
import org.jetbrains.compose.resources.DrawableResource

/**
 * A dish of the tour's recipe app, mixed kebab first.
 *
 * @property title what it is called.
 * @property category the shelf it is on.
 * @property minutes how long it takes.
 * @property easy whether it is easy to make, or takes some care.
 * @property photo its picture, 480 by 320.
 * @property light the lighter of the two colours its place is drawn in before its picture has come.
 * @property deep the deeper one.
 */
@Suppress("MagicNumber") // The dishes' times and colours are the design's.
internal enum class Recipe(
    val title: Words,
    val category: RecipeCategory,
    val minutes: Int,
    val easy: Boolean,
    val photo: DrawableResource,
    val light: Color,
    val deep: Color
) {
    Kebab(
        Words("Mixed kebab", "Karışık kebap"),
        RecipeCategory.Mains,
        45,
        false,
        Res.drawable.recipe_kebab,
        Color(0xFFC9653B),
        Color(0xFF4D1D08)
    ),
    Menemen(
        Words("Menemen", "Menemen"),
        RecipeCategory.Breakfast,
        20,
        true,
        Res.drawable.recipe_menemen,
        Color(0xFFF6C445),
        Color(0xFF7A5300)
    ),
    LentilSoup(
        Words("Lentil soup", "Mercimek çorbası"),
        RecipeCategory.Soups,
        35,
        true,
        Res.drawable.recipe_lentil_soup,
        Color(0xFFE8893A),
        Color(0xFF6B3200)
    ),
    StuffedPeppers(
        Words("Stuffed peppers", "Biber dolması"),
        RecipeCategory.Mains,
        60,
        false,
        Res.drawable.recipe_stuffed_peppers,
        Color(0xFFE26A4F),
        Color(0xFF6E1E0E)
    ),
    ChickpeaSalad(
        Words("Chickpea salad", "Nohut salatası"),
        RecipeCategory.Mains,
        15,
        true,
        Res.drawable.recipe_chickpea_salad,
        Color(0xFF7CB85B),
        Color(0xFF2D5A14)
    ),
    LemonCake(
        Words("Lemon olive oil cake", "Limonlu zeytinyağlı kek"),
        RecipeCategory.Desserts,
        50,
        false,
        Res.drawable.recipe_lemon_cake,
        Color(0xFFF2DC6B),
        Color(0xFF6B5A00)
    ),
    RicePudding(
        Words("Rice pudding", "Sütlaç"),
        RecipeCategory.Desserts,
        40,
        true,
        Res.drawable.recipe_rice_pudding,
        Color(0xFFEAD9C2),
        Color(0xFF6B5235)
    )
}

/** The shelves the recipes are on, and the one that shows them all. */
internal enum class RecipeCategory(val title: Words) {
    All(Words("All", "Hepsi")),
    Breakfast(Words("Breakfast", "Kahvaltı")),
    Soups(Words("Soups", "Çorbalar")),
    Mains(Words("Mains", "Ana yemekler")),
    Desserts(Words("Desserts", "Tatlılar"))
}

/** The tour's recipe app's words, and the mixed kebab's recipe. */
internal object RecipeSay {
    val Recipes = Words("Recipes", "Tarifler")
    val Saved = Words("Saved", "Kaydedilenler")
    val Plan = Words("Plan", "Plan")
    val Shopping = Words("Shopping", "Alışveriş")
    val Search = Words("Search recipes", "Tarif ara")
    val Ingredients = Words("Ingredients", "Malzemeler")
    val StartCooking = Words("Start cooking", "Pişirmeye başla")
    val AddToList = Words("Add to list", "Listeye ekle")
    val Easy = Words("Easy", "Kolay")
    val Medium = Words("Medium", "Orta")
    val ShoppingTitle = Words("This week's shopping", "Bu haftanın alışverişi")
    val Cooking = Words("cooking", "pişiriliyor")
    val Timer = Words("Timer", "Zamanlayıcı")
    val ThenTurn = Words("then turn the skewers", "sonra şişleri çevir")
    val Previous = Words("Previous", "Önceki")
    val Pause = Words("Pause", "Duraklat")
    val NextStep = Words("Next step", "Sonraki adım")
    val ForThisStep = Words("For this step", "Bu adım için")
    val Back = Words("Back", "Geri")

    /** How long a dish takes, in minutes. */
    fun minutes(count: Int): Words = Words("$count min", "$count dk")

    /** Which of the recipe's steps is on show. */
    fun step(at: Int, of: Int): Words = Words("Step $at of $of", "Adım $at / $of")

    val KebabIngredients = listOf(
        Words("300 g lamb, cubed", "300 g kuzu eti, küp doğranmış"),
        Words("300 g chicken thighs, cubed", "300 g tavuk but, küp doğranmış"),
        Words("250 g minced lamb", "250 g kuzu kıyma"),
        Words("Green peppers and tomatoes", "Sivri biber ve domates"),
        Words("Lavash and sumac onions", "Lavaş ve sumaklı soğan")
    )
    val KebabSteps = listOf(
        Words("Marinate the lamb and the chicken for two hours.", "Kuzu ve tavuğu iki saat marine et."),
        Words("Shape the minced lamb on flat skewers.", "Kıymayı yassı şişlere geçir."),
        Words(
            "Grill the skewers with the peppers and tomatoes, turning them often.",
            "Şişleri biber ve domatesle ızgarada, sık sık çevirerek pişir."
        ),
        Words("Serve on lavash with sumac onions.", "Lavaş üstünde sumaklı soğanla servis et.")
    )
    val ForTheGrill = listOf(Words("Skewers", "Şişler"), Words("Peppers", "Biberler"), Words("Tomatoes", "Domatesler"))
    val ShoppingList = listOf(
        Words("Lamb, 300 g", "Kuzu eti, 300 g"),
        Words("Chicken thighs, 300 g", "Tavuk but, 300 g"),
        Words("Red lentils, 250 g", "Kırmızı mercimek, 250 g"),
        Words("Eggs, a dozen", "Yumurta, bir düzine"),
        Words("Lemons, three", "Limon, üç tane"),
        Words("Short-grain rice, 200 g", "Pirinç, 200 g")
    )

    /** Who took the photos, and where they are from. */
    val Credits = Words(
        "Photos: Victoria Shes, Markus Spiske, Rashpal Singh and The Design Lady on Unsplash; Engin Akyurt, Rukiye " +
            "Demir and Hilal Bülbül on Pexels.",
        "Fotoğraflar: Unsplash'te Victoria Shes, Markus Spiske, Rashpal Singh ve The Design Lady; Pexels'te Engin " +
            "Akyurt, Rukiye Demir ve Hilal Bülbül."
    )
}
