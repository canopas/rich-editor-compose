package com.example.texteditor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.canopas.editor.ui.RichTextValue
import com.canopas.editor.ui.TextEditor
import com.example.texteditor.ui.theme.TextEditorTheme

import androidx.compose.ui.text.*
import com.canopas.editor.ui.StyleContainer


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

            TextEditor(
                value = basicRichTextValue,
                onValueChange = { basicRichTextValue = it },
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { content ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth().sizeIn(minHeight = 100.dp)
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(size = 16.dp)
                            )
                            .border(
                                width = 2.dp,
                                color = Color.LightGray,
                                shape = RoundedCornerShape(size = 16.dp)
                            )
                            .padding(all = 16.dp),
                    ) {
                        content()
                    }

                }
            )

            Spacer(modifier = Modifier.height(60.dp))

            StyleContainer(basicRichTextValue, onValueChange = { basicRichTextValue = it })
        }
    }
}
