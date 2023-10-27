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
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.canopas.editor.ui.data.RichEditorState
import com.canopas.editor.ui.ui.RichEditor
import com.canopas.editor.ui.ui.rememberEditorState
import com.canopas.editor.ui.utils.TextSpanStyle
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
                    Sample()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Sample() {
    TextEditorTheme {
        val state = rememberEditorState()

        Column {

            StyleContainer(state)

            RichEditor(
                state = state,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(1.dp, Color.Gray)
                    .padding(5.dp),
                embeddedErrorPlaceHolder = painterResource(id = com.canopas.editor.R.drawable.ic_error_placeholder)
            )
        }
    }
}


@Composable
fun StyleContainer(
    state: RichEditorState,
) {
    val scope = rememberCoroutineScope()
    Row(
        Modifier
            .fillMaxWidth()
            .height(52.dp)
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.Start,
    ) {

        TitleStyleButton(state)
        StyleButton(
            icon = R.drawable.ic_bold,
            style = TextSpanStyle.BoldStyle,
            value = state
        )

        StyleButton(
            icon = R.drawable.ic_italic,
            style = TextSpanStyle.ItalicStyle,
            value = state,
        )

        StyleButton(
            icon = R.drawable.ic_underlined,
            style = TextSpanStyle.UnderlineStyle,
            value = state,
        )

        ImagePicker(
            value = state,
        )

        VideoPicker(
            value = state,
        )

        IconButton(
            modifier = Modifier
                .padding(2.dp)
                .size(48.dp),
            onClick = {
                // Log.d("XXX", "Json ${state.toJson()} "
                state.reset()
            },
        ) {
            Icon(
                Icons.Default.Refresh, contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }

    }
}

@Composable
fun TitleStyleButton(
    value: RichEditorState
) {
    var expanded by remember { mutableStateOf(false) }

    val onItemSelected = { style: TextSpanStyle ->
        value.updateStyle(style)
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
            modifier = Modifier.wrapContentSize(),
            properties = PopupProperties(false)
        ) {

            DropDownItem(text = "Text",
                isSelected = value.hasStyle(TextSpanStyle.Default),
                onItemSelected = { onItemSelected(TextSpanStyle.Default) })
            DropDownItem(text = "Header 1", isSelected = value.hasStyle(TextSpanStyle.H1Style),
                onItemSelected = { onItemSelected(TextSpanStyle.H1Style) })
            DropDownItem(text = "Header 2", isSelected = value.hasStyle(TextSpanStyle.H2Style),
                onItemSelected = { onItemSelected(TextSpanStyle.H2Style) })
            DropDownItem(text = "Header 3", isSelected = value.hasStyle(TextSpanStyle.H3Style),
                onItemSelected = { onItemSelected(TextSpanStyle.H3Style) })
            DropDownItem(text = "Header 4", isSelected = value.hasStyle(TextSpanStyle.H4Style),
                onItemSelected = { onItemSelected(TextSpanStyle.H4Style) })
            DropDownItem(text = "Header 5", isSelected = value.hasStyle(TextSpanStyle.H5Style),
                onItemSelected = { onItemSelected(TextSpanStyle.H5Style) })
            DropDownItem(text = "Header 6", isSelected = value.hasStyle(TextSpanStyle.H6Style),
                onItemSelected = { onItemSelected(TextSpanStyle.H6Style) })
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
fun ImagePicker(value: RichEditorState) {

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            // Upload image and add Url
            uri?.let { value.addImage(it.toString()) }
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
fun VideoPicker(value: RichEditorState) {

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let { value.addVideo(it.toString()) }
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
    style: TextSpanStyle,
    value: RichEditorState,
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
            value.toggleStyle(style)
        },
    ) {
        Icon(
            painter = painterResource(id = icon), contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
    }
}