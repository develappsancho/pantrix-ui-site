package com.pantrixui.site.core.designsystem.content

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pantrixui.components.elements.icon.PantrixIcon
import com.pantrixui.components.inputs.textfield.PantrixTextField
import com.pantrixui.components.inputs.textfield.TextFieldColors
import com.pantrixui.components.inputs.textfield.TextFieldStyle
import com.pantrixui.foundation.icons.PantrixIcons
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.foundation.token.shape.ShapeSpec
import com.pantrixui.site.core.designsystem.SiteTheme

/**
 * The field that filters a page's list as it is typed in: a magnifier before what is typed and a button that empties
 * it. The guides and the components are filtered by it.
 *
 * @param field what is typed.
 * @param placeholder what it says while it is empty, and its name for a screen reader.
 * @param height how tall it is: lower on a desktop, a finger's reach elsewhere.
 * @param tag its automation key.
 * @param modifier its modifier.
 */
@Composable
fun FilterField(
    field: TextFieldState,
    placeholder: String,
    height: Dp,
    tag: String,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    PantrixTextField(
        state = field,
        modifier = modifier,
        placeholder = placeholder,
        leadingIcon = {
            PantrixIcon(
                imageVector = PantrixIcons.Search,
                contentDescription = null,
                tint = SiteTheme.colors.muted,
                size = 16.dp
            )
        },
        showClearButton = true,
        style = TextFieldStyle(
            shape = ShapeSpec.Fixed(RoundedCornerShape(12.dp)),
            height = height,
            horizontalPadding = 12.dp,
            iconPadding = 10.dp
        ),
        colors = TextFieldColors(border = PantrixTheme.colors.borderHover),
        contentDescription = placeholder,
        tagKey = tag
    )
}
