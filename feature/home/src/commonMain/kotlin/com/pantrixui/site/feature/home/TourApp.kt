package com.pantrixui.site.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.container.listdetail.PantrixListDetailScaffold
import com.pantrixui.components.container.listdetail.rememberListDetailState
import com.pantrixui.components.container.scaffold.PantrixScaffold
import com.pantrixui.components.container.scaffold.ScaffoldNavigationPlacement
import com.pantrixui.components.container.tabletop.PantrixTabletopLayout
import com.pantrixui.components.data.listitem.PantrixListItem
import com.pantrixui.components.elements.button.ButtonVariant
import com.pantrixui.components.elements.button.PantrixButton
import com.pantrixui.components.elements.checkbox.PantrixCheckbox
import com.pantrixui.components.elements.chip.PantrixChip
import com.pantrixui.components.elements.icon.PantrixIcon
import com.pantrixui.components.elements.image.PantrixImage
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.components.inputs.textfield.PantrixTextField
import com.pantrixui.components.navigation.common.NavigationItem
import com.pantrixui.components.navigation.navigationbar.PantrixNavigationBar
import com.pantrixui.components.navigation.navigationrail.PantrixNavigationRail
import com.pantrixui.components.navigation.sidebar.PantrixSidebar
import com.pantrixui.components.navigation.sidebar.SidebarGroup
import com.pantrixui.components.navigation.sidebar.SidebarItem
import com.pantrixui.foundation.adaptive.WindowWidthClass
import com.pantrixui.foundation.icons.PantrixIcons
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.model.Language
import org.jetbrains.compose.resources.painterResource

/**
 * The recipe app the home page's tour shows, laid out by Pantrix for the window it is drawn in: held on a table, a
 * foldable cooks the mixed kebab, its step above the fold and its controls below; otherwise the scaffold puts the
 * navigation at the bottom, at the side or in a sidebar, a Medium window shows the recipes as a grid, and the
 * list-detail scaffold shows the list, the recipe and the week's shopping as its room allows.
 *
 * @param photos whether the dishes' pictures are drawn: until the tour has been seen they are their colours alone.
 */
@Composable
internal fun RecipeApp(photos: Boolean) {
    val window = PantrixTheme.window
    if (window.posture.isTabletop) {
        Cooking()
        return
    }
    val language = LocalLanguage.current
    var tab by remember { mutableStateOf(TAB_RECIPES) }
    val items = TABS.map { (key, words, icon) ->
        NavigationItem(key = key, label = words.of(language), icon = icon, tagKey = key)
    }
    val sidebar = window.widthClass >= WindowWidthClass.Large
    PantrixScaffold(
        navigation = { placement, insets ->
            when {
                placement == ScaffoldNavigationPlacement.Bottom -> {
                    PantrixNavigationBar(items, tab, { tab = it }, insets = insets, tagKey = "tourBar")
                }

                sidebar -> {
                    PantrixSidebar(
                        groups = listOf(
                            SidebarGroup(
                                items.mapIndexed { index, item ->
                                    SidebarItem(
                                        item.key,
                                        item.label,
                                        item.icon,
                                        detail = TAB_COUNTS[index],
                                        tagKey = item.key
                                    )
                                }
                            )
                        ),
                        selected = tab,
                        onSelect = { tab = it },
                        expanded = emptySet(),
                        onExpandedChange = {},
                        adaptive = false,
                        insets = insets,
                        tagKey = "tourSidebar"
                    )
                }

                else -> {
                    PantrixNavigationRail(items, tab, { tab = it }, insets = insets, tagKey = "tourRail")
                }
            }
        },
        windowInsets = WindowInsets(0)
    ) { padding ->
        val grid = window.widthClass == WindowWidthClass.Medium && !window.posture.isBook
        if (grid) {
            RecipeGrid(photos, Modifier.padding(padding))
        } else {
            val compact = window.widthClass == WindowWidthClass.Compact
            val recipes = rememberListDetailState(initialSelectedKey = if (compact) null else Recipe.Kebab.name)
            // A window with room for three panes shows the week's shopping beside the recipe; in a narrower one the
            // scaffold would lay it over the recipe as a sheet, and it waits until it is asked for.
            val three = window.widthClass >= WindowWidthClass.ExtraLarge
            LaunchedEffect(recipes, three) { if (three) recipes.showExtra() }
            PantrixListDetailScaffold(
                state = recipes,
                list = { RecipeList(photos, selected = recipes.selectedKey, large = compact) { recipes.select(it) } },
                detail = { key -> RecipeDetail(Recipe.valueOf(key), photos) },
                extra = { ShoppingList() },
                modifier = Modifier.padding(padding),
                tagKey = "tourRecipes"
            )
        }
    }
}

/** The recipes as a list: its title, the search, the shelves and a row a dish; a phone's title is a large one. */
@Composable
private fun RecipeList(
    photos: Boolean,
    selected: String?,
    large: Boolean,
    onPick: (String) -> Unit
) {
    val language = LocalLanguage.current
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Title(RecipeSay.Recipes.of(language), if (large) 32.sp else 22.sp)
            SearchField()
            Shelves()
        }
        Column(Modifier.verticalScroll(rememberScrollState())) {
            Recipe.entries.forEach { recipe ->
                PantrixListItem(
                    headline = recipe.title.of(language),
                    supportingText = recipe.facts(language),
                    leading = { Picture(recipe, photos, Modifier.size(if (large) 64.dp else 56.dp), corner = 12.dp) },
                    onClick = { onPick(recipe.name) },
                    selected = recipe.name == selected,
                    tagKey = recipe.name.lowercase()
                )
            }
        }
    }
}

/** The recipes as cards, two to a row, on a tablet. */
@Composable
private fun RecipeGrid(
    photos: Boolean,
    modifier: Modifier
) {
    val language = LocalLanguage.current
    val colors = PantrixTheme.colors
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Box(Modifier.weight(1f)) { Title(RecipeSay.Recipes.of(language), 34.sp) }
            Box(Modifier.size(width = 260.dp, height = 44.dp)) { SearchField() }
        }
        Shelves()
        Recipe.entries.take(GRID_DISHES).chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                row.forEach { recipe ->
                    Column(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .background(colors.surface)
                    ) {
                        Picture(recipe, photos, Modifier.fillMaxWidth().height(150.dp), corner = 0.dp)
                        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                            PantrixText(text = recipe.title.of(language), style = bold(17.sp))
                            PantrixText(text = recipe.facts(language), color = colors.textSecondary)
                        }
                    }
                }
            }
        }
    }
}

/** A recipe: its picture, its name, its facts, its ingredients to tick and the two things to do with it. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecipeDetail(
    recipe: Recipe,
    photos: Boolean
) {
    val language = LocalLanguage.current
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Picture(recipe, photos, Modifier.fillMaxWidth().height(180.dp), corner = 0.dp)
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Title(recipe.title.of(language), 26.sp)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PantrixChip(label = recipe.category.title.of(language), onClick = null)
                PantrixChip(
                    label = RecipeSay.minutes(recipe.minutes).of(language),
                    onClick = null,
                    leadingIcon = PantrixIcons.Clock
                )
                PantrixChip(label = recipe.level(language), onClick = null)
            }
            PantrixText(text = RecipeSay.Ingredients.of(language), style = bold(14.sp))
            RecipeSay.KebabIngredients.forEachIndexed { index, item ->
                var had by remember { mutableStateOf(index == 0) }
                PantrixCheckbox(checked = had, onCheckedChange = {
                    had = it
                }, label = item.of(language), tagKey = "ingredient_$index")
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PantrixButton(text = RecipeSay.StartCooking.of(language), onClick = {}, tagKey = "startCooking")
                PantrixButton(text = RecipeSay.AddToList.of(language), onClick = {
                }, variant = ButtonVariant.Outline, tagKey = "addToList")
            }
        }
    }
}

/** The week's shopping, an item to tick a row: the extra pane of a window with room for three. */
@Composable
private fun ShoppingList() {
    val language = LocalLanguage.current
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Title(RecipeSay.ShoppingTitle.of(language), 18.sp)
        RecipeSay.ShoppingList.forEachIndexed { index, item ->
            var bought by remember { mutableStateOf(index < 2) }
            PantrixCheckbox(checked = bought, onCheckedChange = {
                bought = it
            }, label = item.of(language), tagKey = "shopping_$index")
        }
    }
}

/**
 * The mixed kebab, cooking, on a foldable held on a table: the step above the fold, read from across the kitchen, and
 * below it the timer and the buttons, where a finger reaches.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Cooking() {
    val language = LocalLanguage.current
    val colors = PantrixTheme.colors
    val steps = RecipeSay.KebabSteps
    PantrixTabletopLayout(
        content = {
            Column(
                Modifier.fillMaxSize().padding(horizontal = 28.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PantrixIcon(
                        PantrixIcons.ChevronBack,
                        contentDescription = RecipeSay.Back.of(language),
                        size = 24.dp
                    )
                    PantrixText(
                        text = "${Recipe.Kebab.title.of(language)} · ${RecipeSay.Cooking.of(language)}",
                        style = bold(18.sp)
                    )
                }
                PantrixText(
                    text = RecipeSay.step(COOKING_STEP + 1, steps.size).of(language).uppercase(),
                    style = bold(13.sp),
                    color = colors.primary.color
                )
                PantrixText(text = steps[COOKING_STEP].of(language), style = bold(36.sp).copy(lineHeight = 42.sp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    steps.indices.forEach { index ->
                        Box(
                            Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp))
                                .background(if (index <= COOKING_STEP) colors.primary.color else colors.border)
                        )
                    }
                }
            }
        },
        controls = {
            Column(Modifier.fillMaxSize().padding(28.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    PantrixText(
                        text = "08:00",
                        style = PantrixTheme.typography.code.copy(fontSize = 64.sp, lineHeight = 64.sp)
                    )
                    Column {
                        PantrixText(text = RecipeSay.Timer.of(language), style = bold(14.sp))
                        PantrixText(text = RecipeSay.ThenTurn.of(language), color = colors.textSecondary)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PantrixButton(text = RecipeSay.Previous.of(language), onClick = {
                    }, variant = ButtonVariant.Outline, tagKey = "previous")
                    PantrixButton(text = RecipeSay.Pause.of(language), onClick = {
                    }, variant = ButtonVariant.Soft, tagKey = "pause")
                    PantrixButton(text = RecipeSay.NextStep.of(language), onClick = {
                    }, modifier = Modifier.weight(1f), tagKey = "nextStep")
                }
                PantrixText(text = RecipeSay.ForThisStep.of(language), style = bold(14.sp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RecipeSay.ForTheGrill.forEach { PantrixChip(label = it.of(language), onClick = null) }
                }
            }
        },
        tagKey = "tourCooking"
    )
}

/** The search over the recipes, which the tour does not type in. */
@Composable
private fun SearchField() {
    PantrixTextField(
        state = rememberTextFieldState(),
        placeholder = RecipeSay.Search.of(LocalLanguage.current),
        leadingIcon = { PantrixIcon(PantrixIcons.Search, contentDescription = null, size = 18.dp) },
        tagKey = "tourSearch"
    )
}

/** The shelves, as chips: all of them chosen. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Shelves() {
    val language = LocalLanguage.current
    var shelf by remember { mutableStateOf(RecipeCategory.All) }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        RecipeCategory.entries.forEach { category ->
            PantrixChip(
                label = category.title.of(language),
                selected = shelf == category,
                onSelectedChange = { shelf = category },
                tagKey = "shelf_${category.name.lowercase()}"
            )
        }
    }
}

/** A heading of the app, [size] large. */
@Composable
private fun Title(
    text: String,
    size: androidx.compose.ui.unit.TextUnit
) {
    PantrixText(text = text, style = bold(size).copy(letterSpacing = (size.value * TITLE_SPACING).sp))
}

/** A dish's picture, its two colours until the pictures are drawn. Its name stands beside it: it says nothing more. */
@Composable
private fun Picture(
    recipe: Recipe,
    photos: Boolean,
    modifier: Modifier,
    corner: Dp
) {
    val shaped = modifier.clip(RoundedCornerShape(corner))
    if (photos) {
        PantrixImage(painterResource(recipe.photo), shaped, contentDescription = null, contentScale = ContentScale.Crop)
    } else {
        Box(shaped.background(Brush.linearGradient(listOf(recipe.light, recipe.deep))))
    }
}

/** The app's bold type at [size]. */
@Composable
private fun bold(size: androidx.compose.ui.unit.TextUnit) =
    PantrixTheme.typography.body.copy(
        fontSize = size,
        lineHeight = (size.value * LINE).sp,
        fontWeight = FontWeight.ExtraBold
    )

/** A dish's shelf, time and how easy it is, on one line. */
private fun Recipe.facts(language: Language): String =
    "${category.title.of(language)} · ${RecipeSay.minutes(minutes).of(language)} · ${level(language)}"

private fun Recipe.level(language: Language): String = (if (easy) RecipeSay.Easy else RecipeSay.Medium).of(language)

/** The app's tabs: their keys, their words and their marks. */
private val TABS = listOf(
    Triple(TAB_RECIPES, RecipeSay.Recipes, PantrixIcons.Folder),
    Triple("saved", RecipeSay.Saved, PantrixIcons.Star),
    Triple("plan", RecipeSay.Plan, PantrixIcons.Calendar),
    Triple("shopping", RecipeSay.Shopping, PantrixIcons.Check)
)

/** What each tab counts, beside it in a sidebar. */
private val TAB_COUNTS = listOf("7", "3", null, "6")

private const val TAB_RECIPES = "recipes"

/** How many dishes a tablet's grid shows. */
private const val GRID_DISHES = 6

/** The step the cooking screen is at: the third, the grill. */
private const val COOKING_STEP = 2

private const val TITLE_SPACING = -0.02f
private const val LINE = 1.3f
