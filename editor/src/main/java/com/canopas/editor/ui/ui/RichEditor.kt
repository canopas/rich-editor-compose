package com.canopas.editor.ui.ui

import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.canopas.editor.ui.data.EditorAttribute
import com.canopas.editor.ui.data.EditorAttribute.TextAttribute
import com.canopas.editor.ui.data.RichEditorState

@Composable
fun RichEditor(
    state: RichEditorState,
    modifier: Modifier = Modifier,
    embeddedErrorPlaceHolder: Painter? = null,
) {
    val scrollState = rememberScrollState()

    Column(modifier.verticalScroll(scrollState)) {
        state.attributes.forEachIndexed { index, value ->
            val isFocused = state.focusedAttributeIndex == index
            when (value) {
                is TextAttribute -> {
                    TextFieldComponent(value, isFocused, onFocusChange = {
                        state.setFocused(index, it)
                    }) {
                        state.focusUp(index)
                    }
                }

                is EditorAttribute.ImageAttribute -> {
                    ImageComponent(value, isFocused, embeddedErrorPlaceHolder,
                        onToggleSelection = { focused ->
                            state.setFocused(index, focused)
                        }, onRemoveClicked = {
                            state.removeContent(index)
                        })
                }

                is EditorAttribute.VideoAttribute -> {
                    VideoComponent(
                        value, isFocused,
                        onToggleSelection = { focused ->
                            state.setFocused(index, focused)
                        },
                        onRemoveClicked = {
                            state.removeContent(index)
                        },
                    )
                }
            }
        }
    }
}

@Composable
internal fun ImageComponent(
    attribute: EditorAttribute.ImageAttribute,
    isFocused: Boolean,
    embeddedErrorPlaceHolder: Painter? = null,
    onToggleSelection: (Boolean) -> Unit,
    onRemoveClicked: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(key1 = isFocused, block = {
        if (isFocused) {
            focusRequester.requestFocus()
        } else {
            focusRequester.freeFocus()
        }
    })

    Box(
        modifier = Modifier
            .wrapContentSize()
            .focusRequester(focusRequester)
            .border(
                1.dp,
                if (isFocused) MaterialTheme.colorScheme.primary else Color.Transparent
            )
            .focusable()
            .clickable {
                onToggleSelection(!isFocused)
            }
            .background(Color.LightGray)
    ) {
        AsyncImage(
            model = attribute.url,
            contentDescription = null,
            modifier = Modifier,
            error = embeddedErrorPlaceHolder
        )

        ContentDeleteButton(isFocused, onRemoveClicked)
    }
}

@Composable
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
internal fun VideoComponent(
    attribute: EditorAttribute.VideoAttribute, isFocused: Boolean,
    onToggleSelection: (Boolean) -> Unit,
    onRemoveClicked: () -> Unit
) {
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(key1 = isFocused, block = {
        if (isFocused) {
            focusRequester.requestFocus()
        } else {
            focusRequester.freeFocus()
        }
    })

    val exoPlayer = remember(attribute.url) {
        ExoPlayer.Builder(context).build().also { exoPlayer ->
            val mediaItem = MediaItem.fromUri(attribute.url)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
            exoPlayer.prepare()
            exoPlayer.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    super.onIsPlayingChanged(isPlaying)
                    if (isPlaying && !isFocused) onToggleSelection(true)
                }
            })
        }
    }
    LaunchedEffect(key1 = isFocused, block = {
        if (!isFocused) exoPlayer.playWhenReady = false
    })

    Box(
        modifier = Modifier
            .wrapContentSize()
            .focusRequester(focusRequester)
            .focusable()
            .border(
                1.dp,
                if (isFocused) MaterialTheme.colorScheme.primary else Color.Transparent
            )
            .clickable {
                onToggleSelection(!isFocused)
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
                        if (!isFocused) onToggleSelection(true)
                    }
                }
            },
            modifier = Modifier
                .background(Color.Black, RoundedCornerShape(2.dp)),
        )

        ContentDeleteButton(isFocused, onRemoveClicked)
    }

    DisposableEffect(key1 = attribute.url, effect = {
        onDispose {
            exoPlayer.release()
        }
    })
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun TextFieldComponent(
    attribute: TextAttribute,
    isFocused: Boolean,
    onFocusChange: (Boolean) -> Unit,
    onFocusUp: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    var previousFocusState by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = isFocused, block = {
        if (isFocused) {
            focusRequester.requestFocus()
        } else {
            focusRequester.freeFocus()
        }
    })

    RichTextField(
        value = attribute.richText,
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
                    if (attribute.isEmpty || attribute.selection.start == 0) {
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
    if (focused) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp, end = 4.dp)
                .align(Alignment.TopEnd)
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
fun rememberEditorState(): RichEditorState {
    val attribute = remember { mutableStateListOf<EditorAttribute>() }
    return remember { RichEditorState(attribute) }
}