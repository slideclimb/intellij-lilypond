package nl.abbyberkers.lilypond.language.highlighting

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import nl.abbyberkers.lilypond.LilypondBundle
import nl.abbyberkers.lilypond.LilypondIcons
import javax.swing.Icon

class LilypondColorSettingsPage : ColorSettingsPage {
    override fun getDisplayName(): String = "LilyPond"

    override fun getIcon(): Icon = LilypondIcons.FILE

    override fun getHighlighter(): SyntaxHighlighter = LilypondSyntaxHighlighter()

    override fun getDemoText(): String = DEMO_TEXT

    // The preview only runs the lexer highlighter, never LilypondAnnotator, so everything the
    // annotator paints has to be tagged explicitly in the demo text: pitch, octave, and a
    // duration fused into the note word (`c4`). A detached duration (the `2` of `g,2`) is a
    // DIGIT token that the lexer highlighter colors on its own, so it needs no tag.
    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey> = DEMO_TAGS

    override fun getAttributeDescriptors(): Array<AttributesDescriptor> = DESCRIPTORS

    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY
}

private val DESCRIPTORS = arrayOf(
    AttributesDescriptor(LilypondBundle.messagePointer("color.settings.command"), LilypondSyntaxHighlighter.COMMAND),
    AttributesDescriptor(LilypondBundle.messagePointer("color.settings.string"), LilypondSyntaxHighlighter.STRING),
    AttributesDescriptor(LilypondBundle.messagePointer("color.settings.comment.line"), LilypondSyntaxHighlighter.LINE_COMMENT),
    AttributesDescriptor(LilypondBundle.messagePointer("color.settings.comment.block"), LilypondSyntaxHighlighter.BLOCK_COMMENT),
    AttributesDescriptor(LilypondBundle.messagePointer("color.settings.number"), LilypondSyntaxHighlighter.NUMBER),
    AttributesDescriptor(LilypondBundle.messagePointer("color.settings.pitch"), LilypondSyntaxHighlighter.PITCH),
    AttributesDescriptor(LilypondBundle.messagePointer("color.settings.octave"), LilypondSyntaxHighlighter.OCTAVE),
    AttributesDescriptor(LilypondBundle.messagePointer("color.settings.braces"), LilypondSyntaxHighlighter.BRACES),
    AttributesDescriptor(LilypondBundle.messagePointer("color.settings.brackets"), LilypondSyntaxHighlighter.BRACKETS),
    AttributesDescriptor(LilypondBundle.messagePointer("color.settings.parentheses"), LilypondSyntaxHighlighter.PARENTHESES),
    AttributesDescriptor(LilypondBundle.messagePointer("color.settings.scheme.marker"), LilypondSyntaxHighlighter.SCHEME_MARKER),
    AttributesDescriptor(LilypondBundle.messagePointer("color.settings.scheme.constant"), LilypondSyntaxHighlighter.CONSTANT),
    AttributesDescriptor(LilypondBundle.messagePointer("color.settings.bad.character"), LilypondSyntaxHighlighter.BAD_CHARACTER),
)

private val DEMO_TAGS = mapOf(
    "pitch" to LilypondSyntaxHighlighter.PITCH,
    "octave" to LilypondSyntaxHighlighter.OCTAVE,
    "duration" to LilypondSyntaxHighlighter.NUMBER,
)

private val DEMO_TEXT =
    """
    \version "2.24.0"

    % a line comment
    %{ a block comment %}

    melody = \relative <pitch>c</pitch><octave>'</octave> {
      \clef "treble"
      <pitch>c</pitch><duration>4</duration> <pitch>d</pitch><duration>8</duration> <pitch>e</pitch><duration>16</duration> <pitch>f</pitch> | <pitch>g</pitch><octave>,</octave>16 <pitch>r</pitch><duration>2</duration>
    }

    \score {
      << \melody >>
      \layout { }
      \midi { }
    }

    #(define (double x) (* x 2))
    """.trimIndent()