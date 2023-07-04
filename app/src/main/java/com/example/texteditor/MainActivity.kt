package com.example.texteditor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.canopas.editor.ui.RichTextValue
import com.canopas.editor.ui.RichTextField
import com.example.texteditor.ui.theme.TextEditorTheme

import com.canopas.editor.ui.RichTextStyle

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TextEditorTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    GreetingPreview()
                }
            }
        }
    }
}


val md = """
Bold with **asterisks** 
# Header 1
## Header 2
    """.trimIndent()

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TextEditorTheme {
        var basicRichTextValue by remember { mutableStateOf(RichTextValue()) }


        Column {
            StyleContainer(basicRichTextValue, onValueChange = { basicRichTextValue = it })

            Spacer(modifier = Modifier.height(10.dp))

            RichTextField(
                value = basicRichTextValue,
                onValueChange = { basicRichTextValue = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                decorationBox = { content ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .sizeIn(minHeight = 100.dp)
                            .border(
                                width = 2.dp,
                                color = Color.LightGray,
                                shape = RoundedCornerShape(size = 2.dp)
                            )
                            .padding(all = 16.dp),
                    ) {
                        content()
                    }

                }
            )
        }
    }
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

            DropDownItem(text = "Title", isSelected = value.hasStyle(RichTextStyle.TITLE),
                onItemSelected = { onItemSelected(RichTextStyle.TITLE) })
            DropDownItem(text = "Subtitle", isSelected = value.hasStyle(RichTextStyle.SUB_TITLE),
                onItemSelected = { onItemSelected(RichTextStyle.SUB_TITLE) })
            DropDownItem(text = "Header 1", isSelected = value.hasStyle(RichTextStyle.H1),
                onItemSelected = { onItemSelected(RichTextStyle.H1) })
            DropDownItem(text = "Header 2", isSelected = value.hasStyle(RichTextStyle.H2),
                onItemSelected = { onItemSelected(RichTextStyle.H2) })
            DropDownItem(text = "Header 3", isSelected = value.hasStyle(RichTextStyle.H3),
                onItemSelected = { onItemSelected(RichTextStyle.H3) })
            DropDownItem(text = "Header 4", isSelected = value.hasStyle(RichTextStyle.H4),
                onItemSelected = { onItemSelected(RichTextStyle.H4) })
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
                color = if (value.hasStyle(style)) {
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

