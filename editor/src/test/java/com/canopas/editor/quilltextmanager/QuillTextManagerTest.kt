package com.canopas.editor.quilltextmanager

import android.content.Context
import android.text.Editable
import androidx.compose.ui.text.TextRange
import androidx.test.core.app.ApplicationProvider
import com.canopas.editor.MainCoroutineRule
import com.canopas.editor.jsonparser.QuillJsonEditorParser
import com.canopas.editor.ui.data.QuillEditorState
import com.canopas.editor.ui.model.QuillTextSpan
import com.canopas.editor.ui.utils.TextSpanStyle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@ExperimentalCoroutinesApi
class QuillTextManagerTest {

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()
    private lateinit var quillEditorState: QuillEditorState
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private lateinit var editableInstance: Editable.Factory
    private var sampleSpansListSize = 0

    @Before
    fun setup() {
        val input =
            context.assets.open("android-quill-sample.json").bufferedReader().use { it.readText() }
        editableInstance = Editable.Factory.getInstance()
        quillEditorState = QuillEditorState.Builder()
            .setInput(input)
            .adapter(QuillJsonEditorParser())
            .build()
        sampleSpansListSize = quillEditorState.manager.quillTextSpans.size
    }

    @Test
    fun `test getQuillSpan`() {
        val quillSpan = quillEditorState.getQuillSpan()
        Assert.assertNotNull(quillSpan)
    }

    @Test
    fun `test output`() {
        val output = quillEditorState.output()
        Assert.assertNotNull(output)
    }

    @Test
    fun `test reset`() {
        quillEditorState.reset()
        Assert.assertTrue(quillEditorState.manager.quillTextSpans.isEmpty())
    }

    @Test
    fun `test hasStyle`() {
        val hasStyle = quillEditorState.hasStyle(TextSpanStyle.BoldStyle)
        Assert.assertFalse(hasStyle)
    }

    @Test
    fun `test toggleStyle`() {
        quillEditorState.toggleStyle(TextSpanStyle.BoldStyle)
        val hasStyle = quillEditorState.hasStyle(TextSpanStyle.BoldStyle)
        Assert.assertTrue(hasStyle)
    }

    @Test
    fun `test updateStyle`() {
        quillEditorState.updateStyle(TextSpanStyle.BoldStyle)
        val hasStyle = quillEditorState.hasStyle(TextSpanStyle.BoldStyle)
        Assert.assertTrue(hasStyle)
    }

    @Test
    fun `scenario 1 - test span added successfully when text is selected and style is added`() {
        quillEditorState.manager.adjustSelection(TextRange(0, 5))
        val spansSize = quillEditorState.manager.quillTextSpans.size
        quillEditorState.toggleStyle(TextSpanStyle.ItalicStyle)
        Assert.assertEquals(1, quillEditorState.manager.quillTextSpans.size - spansSize)
    }

    @Test
    fun `scenario 2 - test span removed successfully when text is selected and style is removed`() {
        quillEditorState.manager.adjustSelection(TextRange(0, 5))
        val spansSize = quillEditorState.manager.quillTextSpans.size
        quillEditorState.toggleStyle(TextSpanStyle.ItalicStyle)
        // Verify span added
        Assert.assertEquals(1, quillEditorState.manager.quillTextSpans.size - spansSize)
        val newSpanSize = quillEditorState.manager.quillTextSpans.size
        quillEditorState.toggleStyle(TextSpanStyle.ItalicStyle)
        // Verify span removed
        Assert.assertFalse(newSpanSize == spansSize)
        Assert.assertEquals(0, quillEditorState.manager.quillTextSpans.size - newSpanSize)
    }

    @Test
    fun `scenario 3 - test span created successfully when user selects style and starts typing`() {
        quillEditorState.manager.adjustSelection(TextRange(2, 2))
        val spansSize = quillEditorState.manager.quillTextSpans.size
        quillEditorState.toggleStyle(TextSpanStyle.ItalicStyle)
        quillEditorState.manager.handleAddingCharacters(editableInstance.newEditable("t"))
        Assert.assertEquals(1, quillEditorState.manager.quillTextSpans.size - spansSize)
    }

    @Test
    fun `scenario 4 - test other span created successfully when user deselects style and starts typing`() {
        quillEditorState.manager.adjustSelection(TextRange(2, 2))
        val spansSize = quillEditorState.manager.quillTextSpans.size
        quillEditorState.toggleStyle(TextSpanStyle.ItalicStyle)
        quillEditorState.manager.handleAddingCharacters(editableInstance.newEditable("t"))
        // Verify span added
        Assert.assertEquals(1, quillEditorState.manager.quillTextSpans.size - spansSize)
        val newSpanSize = quillEditorState.manager.quillTextSpans.size
        quillEditorState.toggleStyle(TextSpanStyle.ItalicStyle)
        quillEditorState.manager.handleAddingCharacters(editableInstance.newEditable("t"))
        // Verify other span added
        Assert.assertEquals(1, quillEditorState.manager.quillTextSpans.size - newSpanSize)
        // Verify 2 new spans added compared to android-quill-sample.json as original has 3 spans
        Assert.assertEquals(sampleSpansListSize + 2, quillEditorState.manager.quillTextSpans.size)
    }

    @Test
    fun `scenario 5 - extend current span if user starts typing in middle of the word which have style on it`() {
        val originalInsert = quillEditorState.manager.editableText
        val newInsert = originalInsert.replace("RichEditor", "RichTEditor")
        quillEditorState.manager.adjustSelection(TextRange(4, 4))
        quillEditorState.manager.setEditable(editableInstance.newEditable(newInsert))
        val to = quillEditorState.manager.quillTextSpans[0].to
        val expectedSpan =
            QuillTextSpan(0, 9, listOf(TextSpanStyle.H1Style, TextSpanStyle.BoldStyle))
        Assert.assertEquals(10, to + 1)
        Assert.assertEquals(
            "RichTEditor",
            quillEditorState.manager.editableText.substringBefore("\n")
        )
        Assert.assertEquals(expectedSpan, quillEditorState.manager.quillTextSpans[0])
    }

    @Test
    fun `scenario 6 - extend current span if user starts typing just after styles text`() {
        val originalInsert = quillEditorState.manager.editableText
        val newInsert = originalInsert.replace("RichEditor", "RichEditorT")
        quillEditorState.manager.adjustSelection(TextRange(9, 9))
        quillEditorState.manager.setEditable(editableInstance.newEditable(newInsert))
        val to = quillEditorState.manager.quillTextSpans[0].to
        Assert.assertEquals(10, to + 1)
        Assert.assertEquals(
            "RichEditorT",
            quillEditorState.manager.editableText.substringBefore("\n")
        )
    }

    @Test
    fun `scenario 7 - test span is moved by typed character if user starts typing just before styles text`() {
        val originalInsert = quillEditorState.manager.editableText
        val newInsert = originalInsert.replace("RichEditor", "TRichEditor")
        quillEditorState.manager.adjustSelection(TextRange(0, 0))
        quillEditorState.manager.setEditable(editableInstance.newEditable(newInsert))
        val to = quillEditorState.manager.quillTextSpans[0].to
        Assert.assertEquals(10, to + 1)
        Assert.assertEquals(
            "TRichEditor",
            quillEditorState.manager.editableText.substringBefore("\n")
        )
    }

    @Test
    fun `scenario 8 - add span with selected style if user starts typing at initial position`() {
        quillEditorState.toggleStyle(TextSpanStyle.ItalicStyle)
        quillEditorState.manager.onTextFieldValueChange(
            editableInstance.newEditable("t"),
            TextRange(0, 0)
        )
        Assert.assertTrue(quillEditorState.manager.quillTextSpans[0].style.contains(TextSpanStyle.ItalicStyle))
    }

    @Test
    fun `scenario 9 - break spans into 2 when user removes style from middle of word by selection text`() {
        val currentSpansSize = quillEditorState.manager.quillTextSpans.size
        quillEditorState.manager.adjustSelection(TextRange(4, 6))
        quillEditorState.toggleStyle(TextSpanStyle.BoldStyle)
        Assert.assertEquals(currentSpansSize + 2, quillEditorState.manager.quillTextSpans.size)
    }

    @Test
    fun `scenario 10 - break spans into 2 when user deselects style and starts typing in middle of any word which have style`() {
        val currentSpansSize = quillEditorState.manager.quillTextSpans.size
        quillEditorState.manager.adjustSelection(TextRange(4, 4))
        quillEditorState.toggleStyle(TextSpanStyle.BoldStyle)
        val originalInsert = quillEditorState.manager.editableText
        val newInsert = originalInsert.replace("RichEditor", "RichTEditor")
        quillEditorState.manager.handleAddingCharacters(editableInstance.newEditable(newInsert))
        Assert.assertEquals(currentSpansSize + 2, quillEditorState.manager.quillTextSpans.size)
    }

    @Test
    fun `scenario 11 - update span length when any character is removed from it`() {
        val previousToIndex = quillEditorState.manager.quillTextSpans[0].to
        quillEditorState.manager.adjustSelection(TextRange(4, 4))
        val oldInsert = quillEditorState.manager.editableText
        val newInsert = oldInsert.replace("RichEditor", "RichEdtor")
        quillEditorState.manager.onTextFieldValueChange(
            editableInstance.newEditable(newInsert),
            TextRange(4, 4)
        )
        val newToIndex = quillEditorState.manager.quillTextSpans[0].to
        Assert.assertEquals(previousToIndex - 1, newToIndex)
    }

    @Test
    fun `scenario 12 - Move span by n position forward when user adds n character before styled text anywhere before that text`() {
        val previousToIndex = quillEditorState.manager.quillTextSpans[0].to
        quillEditorState.manager.adjustSelection(TextRange(0, 0))
        val oldInsert = quillEditorState.manager.editableText
        val newInsert = oldInsert.replace("RichEditor", "TextRichEditor")
        quillEditorState.manager.onTextFieldValueChange(
            editableInstance.newEditable(newInsert),
            TextRange(0, 0)
        )
        val newToIndex = quillEditorState.manager.quillTextSpans[0].to
        Assert.assertEquals(previousToIndex + 4, newToIndex)
    }

    @Test
    fun `scenario 13 - Move span by n position backward when user removes n character before styled text anywhere before that text`() {
        val previousToIndex = quillEditorState.manager.quillTextSpans[1].to
        quillEditorState.manager.adjustSelection(TextRange(0, 0))
        val oldInsert = quillEditorState.manager.editableText
        val newInsert = oldInsert.replace("RichEditor", "Editor")
        quillEditorState.manager.onTextFieldValueChange(
            editableInstance.newEditable(newInsert),
            TextRange(0, 0)
        )
        val newToIndex = quillEditorState.manager.quillTextSpans[1].to
        // Verify if 2nd span from list is moved by 4 positions
        Assert.assertEquals(previousToIndex - 4, newToIndex)
    }

    @Test
    fun `scenario 14 - remove header style when user add new line`() {
        quillEditorState.manager.adjustSelection(TextRange(0, 0))
        quillEditorState.toggleStyle(TextSpanStyle.H1Style)
        quillEditorState.manager.onTextFieldValueChange(
            editableInstance.newEditable("\n"),
            TextRange(0, 0)
        )
        // Verify if header style is removed
        Assert.assertFalse(quillEditorState.manager.quillTextSpans[0].style.contains(TextSpanStyle.H1Style))
        // Verify that other styles are not removed
        Assert.assertTrue(quillEditorState.manager.quillTextSpans[0].style.contains(TextSpanStyle.BoldStyle))
    }

    @Test
    fun `scenario 15 - merge spans if style applied to selected text is equivalent to previous and next span`() {
        println("Spans Text:${quillEditorState.manager.editableText.substring(12, 13)}")
        val previousSpanSize = quillEditorState.getRichText().spans.size
        quillEditorState.manager.adjustSelection(TextRange(12, 13))
        quillEditorState.updateStyle(TextSpanStyle.BoldStyle)
        val newSize = quillEditorState.getRichText().spans.size
        // Verify that 3 spans are merged into 1
        Assert.assertEquals(previousSpanSize - 2, newSize)
    }

    @Test
    fun `scenario 16 - if new line is entered in between text then remove header if available and split span into two`() {
        quillEditorState.manager.adjustSelection(TextRange(4, 4))
        val previousSpanSize = quillEditorState.manager.quillTextSpans.size
        val oldInsert = quillEditorState.manager.editableText
        val newInsert = oldInsert.replace("RichEditor", "Rich\nEditor")
        quillEditorState.manager.onTextFieldValueChange(
            editableInstance.newEditable(newInsert),
            TextRange(0, 0)
        )
        Assert.assertEquals(previousSpanSize + 1, quillEditorState.manager.quillTextSpans.size)
        Assert.assertTrue(quillEditorState.manager.quillTextSpans[0].style.contains(TextSpanStyle.BoldStyle))
        Assert.assertTrue(quillEditorState.manager.quillTextSpans[0].style.contains(TextSpanStyle.H1Style))
        Assert.assertFalse(quillEditorState.manager.quillTextSpans[1].style.contains(TextSpanStyle.H1Style))
        //Making false For testing
        Assert.assertFalse(quillEditorState.manager.quillTextSpans[1].style.contains(TextSpanStyle.BoldStyle))
    }

    @Test
    fun `scenario 17 - test remove all styles if selection range is 0,0`() {
        quillEditorState.manager.onTextFieldValueChange(
            editableInstance.newEditable("t"),
            TextRange(0, 0)
        )
        Assert.assertEquals(0, quillEditorState.manager.currentStyles.size)
    }
}