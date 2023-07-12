package com.example.texteditor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.sp
import com.canopas.editor.ui.RichEditor
import com.canopas.editor.ui.data.TextEditorValue
import com.canopas.editor.ui.model.RichTextStyle
import com.canopas.editor.ui.rememberEditorState
import com.example.texteditor.ui.theme.TextEditorTheme

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

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TextEditorTheme {
        var state by rememberEditorState()

        Column {

            StyleContainer(state, onValueChange = {
                state = it
            })

            RichEditor(
                state = state,
                onValueChange = { state = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(1.dp, Color.Gray)
                    .padding(5.dp)
            )
        }
    }
}


@Composable
fun StyleContainer(
    value: TextEditorValue,
    onValueChange: (TextEditorValue) -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(52.dp)
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

        ImagePicker(
            value = value,
            onValueChange = onValueChange
        )

        VideoPicker(
            value = value,
            onValueChange = onValueChange
        )


    }
}

@Composable
fun TitleStyleButton(
    value: TextEditorValue,
    onValueChange: (TextEditorValue) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    val onItemSelected = { style: RichTextStyle ->
        onValueChange(value.updateStyles(setOf(style)))
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

            DropDownItem(text = "Normal Text",
                isSelected = value.hasStyle(RichTextStyle.FontSize(16.sp)),
                onItemSelected = { onItemSelected(RichTextStyle.FontSize(16.sp)) })
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
fun ImagePicker(value: TextEditorValue, onValueChange: (TextEditorValue) -> Unit) {

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let { onValueChange(value.addImage(it)) }
        }
    )

    IconButton(
        modifier = Modifier
            .padding(2.dp)
            .size(48.dp),
        onClick = {
            pickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_image), contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
    }
}


@Composable
fun VideoPicker(value: TextEditorValue, onValueChange: (TextEditorValue) -> Unit) {

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let { onValueChange(value.addVideo(it)) }
        }
    )

    IconButton(
        modifier = Modifier
            .padding(2.dp)
            .size(48.dp),
        onClick = {
            pickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
            )
        },
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_video), contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun StyleButton(
    @DrawableRes icon: Int,
    style: RichTextStyle,
    value: TextEditorValue,
    onValueChange: (TextEditorValue) -> Unit,
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