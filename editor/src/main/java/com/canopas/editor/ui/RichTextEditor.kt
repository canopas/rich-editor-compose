package com.canopas.editor.ui

import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.canopas.editor.ui.data.ContentType
import com.canopas.editor.ui.data.ImageContentValue
import com.canopas.editor.ui.data.RichTextValue
import com.canopas.editor.ui.data.TextEditorValue
import com.canopas.editor.ui.data.VideoContentValue

@Composable
fun RichTextEditor(
    state: TextEditorValue,
    onValueChange: (TextEditorValue) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = TextStyle.Default,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    cursorBrush: Brush = SolidColor(Color.Black),
) {
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    Column(modifier.verticalScroll(scrollState)) {
        state.values.forEachIndexed { index, value ->
            when (value.type) {
                ContentType.RICH_TEXT -> {
                    val richText = value as RichTextValue

                    TextFieldComponent(richText, onValueChange = {
                        onValueChange(state.update(it, index))
                    }, onFocusChange = { isFocused ->
                        onValueChange(state.setFocused(index, isFocused))
                    }, onFocusUp = {
                        onValueChange(state.focusUp(index))
                    })
                }

                ContentType.IMAGE -> {
                    val imageContentValue = value as ImageContentValue
                    ImageComponent(imageContentValue, onToggleSelection = { isFocused ->
                        if (isFocused) focusManager.clearFocus(true)
                        onValueChange(state.setFocused(index, isFocused))
                    })
                }

                ContentType.VIDEO -> {
                    val contentValue = value as VideoContentValue
                    VideoComponent(contentValue, onToggleSelection = { isFocused ->
                        if (isFocused) focusManager.clearFocus(true)
                        onValueChange(state.setFocused(index, isFocused))
                    })
                }
            }
        }
    }
}

@Composable
internal fun ImageComponent(
    contentValue: ImageContentValue,
    onToggleSelection: (Boolean) -> Unit
) {
    AsyncImage(
        model = contentValue.uri,
        contentDescription = null,
        modifier = Modifier
            .wrapContentSize()
            .border(1.dp, if (contentValue.isFocused) Color.Green else Color.Transparent)
            .clickable {
                onToggleSelection(!contentValue.isFocused)
            },
    )
}

@Composable
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
internal fun VideoComponent(
    contentValue: VideoContentValue,
    onToggleSelection: (Boolean) -> Unit
) {
    val context = LocalContext.current

    val exoPlayer = remember(contentValue.uri) {
        ExoPlayer.Builder(context)
            .build()
            .also { exoPlayer ->
                val mediaItem = MediaItem.fromUri(contentValue.uri)
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
                exoPlayer.prepare()
                exoPlayer.addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        super.onIsPlayingChanged(isPlaying)
                        if (isPlaying && !contentValue.isFocused) onToggleSelection(true)
                    }
                })
            }
    }
    LaunchedEffect(key1 = contentValue.isFocused, block = {
        exoPlayer.playWhenReady = contentValue.isFocused
    })

    AndroidView(
        factory = {
            PlayerView(context).apply {
                player = exoPlayer
                setShowVrButton(false)
                setShowFastForwardButton(false)
                setShowRewindButton(false)
                setShowNextButton(false)
                setShowPreviousButton(false)
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
                layoutParams = ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
//                setControllerVisibilityListener(PlayerView.ControllerVisibilityListener { visibility ->
//                    if (visibility == View.VISIBLE) {
//                        onToggleSelection(true)
//                    }
//                })
            }
        },
        modifier = Modifier
            .wrapContentSize()
            .border(1.dp, if (contentValue.isFocused) Color.Green else Color.Transparent)
            .background(Color.Black, RoundedCornerShape(2.dp))
    )


    DisposableEffect(key1 = contentValue.uri, effect = {
        onDispose {
            exoPlayer.release()
        }
    })
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun TextFieldComponent(
    richText: RichTextValue,
    onValueChange: (RichTextValue) -> Unit,
    onFocusChange: (Boolean) -> Unit,
    onFocusUp: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    var previousFocusState by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = richText.isFocused, block = {
        if (richText.isFocused) {
            focusRequester.requestFocus()
        } else {
            focusRequester.freeFocus()
        }
    })

    RichTextField(
        value = richText,
        onValueChange = {
            onValueChange(it)
        },
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onFocusChanged {
                if (previousFocusState != it.isFocused) {
                    onFocusChange(it.isFocused)
                    previousFocusState = it.isFocused
                }
            }
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyUp && event.key == Key.Backspace) {
                    if (richText.text.isEmpty() || richText.textFieldValue.selection.start == 0) {
                        onFocusUp()
                        return@onKeyEvent true
                    }
                }
                false
            },
        // cursorBrush = if (richText.isFocused) SolidColor(Color.Black) else SolidColor(Color.Transparent)
    )

}

@Composable
fun rememberEditorState(): MutableState<TextEditorValue> {
    return remember {
        mutableStateOf(TextEditorValue())
    }
}