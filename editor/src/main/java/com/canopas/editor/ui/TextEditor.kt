package com.canopas.editor.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.canopas.editor.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextEditor(
    value: RichTextValue,
    onValueChange: (RichTextValue) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = TextStyle.Default,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    cursorBrush: Brush = SolidColor(Color.Black),
    decorationBox: @Composable (innerTextField: @Composable () -> Unit) -> Unit =
        @Composable { innerTextField -> innerTextField() }
) {

    BasicTextField(
        value = value.textFieldValue,
        onValueChange = {
            onValueChange(
                value.updateTextFieldValue(it)
            )
        },
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        visualTransformation = value.visualTransformation,
        onTextLayout = onTextLayout,
        interactionSource = interactionSource,
        cursorBrush = cursorBrush,
        decorationBox = decorationBox,
    )
}

@Composable
fun StyleContainer(
    value: RichTextValue,
    onValueChange: (RichTextValue) -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.Start,
    ) {

        TitleStyleButton(value, onValueChange)
        StyleButton(
            icon = R.drawable.ic_bold,
            style = RichTextStyle.Bold,
            value = value,
            onValueChange = onValueChange
        )

        StyleButton(
            icon = R.drawable.ic_italic,
            style = RichTextStyle.Italic,
            value = value,
            onValueChange = onValueChange
        )

        StyleButton(
            icon = R.drawable.ic_underlined,
            style = RichTextStyle.Underline,
            value = value,
            onValueChange = onValueChange
        )
    }
}

@Composable
fun TitleStyleButton(value: RichTextValue, onValueChange: (RichTextValue) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val selectedStyle = value.currentStyles

    val onItemSelected = { style: RichTextStyle ->
        onValueChange(value.setTitleStyles(setOf(style)))
        expanded = false
    }

    Row {
        IconButton(
            modifier = Modifier
                .padding(2.dp)
                .size(48.dp),
            onClick = { expanded = true },
        ) {
            Row {
                Icon(
                    painter = painterResource(id = R.drawable.ic_title), contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.wrapContentSize()
        ) {

            DropDownItem(
                text = "Text",
                isSelected = selectedStyle.any { it is RichTextStyle.FontSize },
                onItemSelected = { onItemSelected(RichTextStyle.FontSize(TextUnit.Unspecified)) })
            DropDownItem(text = "Title", isSelected = RichTextStyle.TITLE in selectedStyle,
                onItemSelected = { onItemSelected(RichTextStyle.TITLE) })
            DropDownItem(text = "Header 1", isSelected = RichTextStyle.H1 in selectedStyle,
                onItemSelected = { onItemSelected(RichTextStyle.H1) })
            DropDownItem(text = "Header 2", isSelected = RichTextStyle.H2 in selectedStyle,
                onItemSelected = { onItemSelected(RichTextStyle.H2) })
            DropDownItem(text = "Header 3", isSelected = RichTextStyle.H3 in selectedStyle,
                onItemSelected = { onItemSelected(RichTextStyle.H3) })
            DropDownItem(text = "Header 4", isSelected = RichTextStyle.H4 in selectedStyle,
                onItemSelected = { onItemSelected(RichTextStyle.H4) })
            DropDownItem(text = "Header 5", isSelected = RichTextStyle.H5 in selectedStyle,
                onItemSelected = { onItemSelected(RichTextStyle.H5) })
            DropDownItem(text = "Header 6", isSelected = RichTextStyle.H6 in selectedStyle,
                onItemSelected = { onItemSelected(RichTextStyle.H6) })
        }
    }
}

@Composable
fun DropDownItem(
    text: String,
    isSelected: Boolean,
    onItemSelected: () -> Unit
) {

    DropdownMenuItem(
        text = {
            Text(text = text)
        }, onClick = onItemSelected,
        modifier = Modifier.background(
            color = if (isSelected) {
                Color.Gray.copy(alpha = 0.2f)
            } else {
                Color.Transparent
            }, shape = RoundedCornerShape(6.dp)
        )
    )
}


@Composable
fun StyleButton(
    @DrawableRes icon: Int,
    style: RichTextStyle,
    value: RichTextValue,
    onValueChange: (RichTextValue) -> Unit
) {
    IconButton(
        modifier = Modifier
            .padding(2.dp)
            .size(48.dp)
            .background(
                color = if (value.currentStyles.contains(style)) {
                    Color.Gray.copy(alpha = 0.2f)
                } else {
                    Color.Transparent
                }, shape = RoundedCornerShape(6.dp)
            ),
        onClick = {
            onValueChange(value.toggleStyle(style))
        },
    ) {
        Icon(
            painter = painterResource(id = icon), contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
    }
}