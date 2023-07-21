package com.canopas.editor.ui

import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
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
fun RichEditor(
    state: TextEditorValue,
    onValueChange: (TextEditorValue) -> Unit,
    modifier: Modifier = Modifier,
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
                    }, onRemoveClicked = {
                        onValueChange(state.removeContent(index))
                    })
                }

                ContentType.VIDEO -> {
                    val contentValue = value as VideoContentValue
                    VideoComponent(
                        contentValue,
                        onToggleSelection = { isFocused ->
                            if (isFocused) focusManager.clearFocus(true)
                            onValueChange(state.setFocused(index, isFocused))
                        },
                        onRemoveClicked = {
                            onValueChange(state.removeContent(index))
                        },
                    )
                }
            }
        }
    }
}

@Composable
internal fun ImageComponent(
    contentValue: ImageContentValue,
    onToggleSelection: (Boolean) -> Unit,
    onRemoveClicked: () -> Unit
) {
    Box(modifier = Modifier
        .wrapContentSize()
        .border(
            1.dp,
            if (contentValue.isFocused) MaterialTheme.colorScheme.primary else Color.Transparent
        )
        .clickable {
            onToggleSelection(!contentValue.isFocused)
        }) {

        AsyncImage(
            model = contentValue.uri, contentDescription = null
        )

        ContentDeleteButton(contentValue.isFocused, onRemoveClicked)
    }
}

@Composable
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
internal fun VideoComponent(
    contentValue: VideoContentValue,
    onToggleSelection: (Boolean) -> Unit,
    onRemoveClicked: () -> Unit
) {
    val context = LocalContext.current

    val exoPlayer = remember(contentValue.uri) {
        ExoPlayer.Builder(context).build().also { exoPlayer ->
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
        if (!contentValue.isFocused) exoPlayer.playWhenReady = false
    })

    Box(
        modifier = Modifier
            .wrapContentSize()
            .border(
                1.dp,
                if (contentValue.isFocused) MaterialTheme.colorScheme.primary else Color.Transparent
            )
            .clickable {
                onToggleSelection(!contentValue.isFocused)
            }
    ) {
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
                    setOnClickListener {
                        if (!contentValue.isFocused) onToggleSelection(true)
                    }
                }
            },
            modifier = Modifier
                .background(Color.Black, RoundedCornerShape(2.dp)),
        )

        ContentDeleteButton(contentValue.isFocused, onRemoveClicked)
    }

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
    )
}

@Composable
private fun BoxScope.ContentDeleteButton(focused: Boolean, onRemoveClicked: () -> Unit) {
    AnimatedVisibility(
        visible = focused,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.align(Alignment.TopEnd)
    ) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp, end = 4.dp)
                .size(34.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(0.9f), shape = CircleShape
                )
                .clickable { onRemoveClicked() }, contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Delete",
                tint = MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun rememberEditorState(): MutableState<TextEditorValue> {
    return remember {
        mutableStateOf(TextEditorValue())
    }
}