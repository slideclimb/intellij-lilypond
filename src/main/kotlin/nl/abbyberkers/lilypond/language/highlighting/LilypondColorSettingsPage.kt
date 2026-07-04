package nl.abbyberkers.lilypond.language.highlighting

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import nl.abbyberkers.lilypond.LilypondIcons
import javax.swing.Icon

class LilypondColorSettingsPage : ColorSettingsPage {
    override fun getDisplayName(): String = "LilyPond"

    override fun getIcon(): Icon = LilypondIcons.FILE

    override fun getHighlighter(): SyntaxHighlighter = LilypondSyntaxHighlighter()

    override fun getDemoText(): String = DEMO_TEXT

    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey>? = null

    override fun getAttributeDescriptors(): Array<AttributesDescriptor> = DESCRIPTORS

    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY
}

private val DESCRIPTORS = arrayOf(
    AttributesDescriptor("Command", LilypondSyntaxHighlighter.COMMAND),
    AttributesDescriptor("String", LilypondSyntaxHighlighter.STRING),
    AttributesDescriptor("Comments//Line comment", LilypondSyntaxHighlighter.LINE_COMMENT),
    AttributesDescriptor("Comments//Block comment", LilypondSyntaxHighlighter.BLOCK_COMMENT),
    AttributesDescriptor("Number", LilypondSyntaxHighlighter.NUMBER),
    AttributesDescriptor("Braces and groups", LilypondSyntaxHighlighter.BRACES),
    AttributesDescriptor("Brackets", LilypondSyntaxHighlighter.BRACKETS),
    AttributesDescriptor("Parentheses", LilypondSyntaxHighlighter.PARENTHESES),
    AttributesDescriptor("Scheme//Marker (# \$ #{ #})", LilypondSyntaxHighlighter.SCHEME_MARKER),
    AttributesDescriptor("Scheme//Constant (#t #f)", LilypondSyntaxHighlighter.CONSTANT),
    AttributesDescriptor("Bad character", LilypondSyntaxHighlighter.BAD_CHARACTER),
)

private val DEMO_TEXT =
    """
    \version "2.24.0"

    % a line comment
    %{ a block comment %}

    melody = \relative c' {
      \clef "treble"
      c4 d8 e16 f | g,2 r2
    }

    \score {
      << \melody >>
      \layout { }
      \midi { }
    }

    #(define (double x) (* x 2))
    """.trimIndent()