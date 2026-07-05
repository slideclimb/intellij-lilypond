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

    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey>? = null

    override fun getAttributeDescriptors(): Array<AttributesDescriptor> = DESCRIPTORS

    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY
}

private val DESCRIPTORS = arrayOf(
    AttributesDescriptor(LilypondBundle.messagePointer("color.settings.command"), LilypondSyntaxHighlighter.COMMAND),
    AttributesDescriptor(LilypondBundle.messagePointer("color.settings.string"), LilypondSyntaxHighlighter.STRING),
    AttributesDescriptor(LilypondBundle.messagePointer("color.settings.comment.line"), LilypondSyntaxHighlighter.LINE_COMMENT),
    AttributesDescriptor(LilypondBundle.messagePointer("color.settings.comment.block"), LilypondSyntaxHighlighter.BLOCK_COMMENT),
    AttributesDescriptor(LilypondBundle.messagePointer("color.settings.number"), LilypondSyntaxHighlighter.NUMBER),
    AttributesDescriptor(LilypondBundle.messagePointer("color.settings.braces"), LilypondSyntaxHighlighter.BRACES),
    AttributesDescriptor(LilypondBundle.messagePointer("color.settings.brackets"), LilypondSyntaxHighlighter.BRACKETS),
    AttributesDescriptor(LilypondBundle.messagePointer("color.settings.parentheses"), LilypondSyntaxHighlighter.PARENTHESES),
    AttributesDescriptor(LilypondBundle.messagePointer("color.settings.scheme.marker"), LilypondSyntaxHighlighter.SCHEME_MARKER),
    AttributesDescriptor(LilypondBundle.messagePointer("color.settings.scheme.constant"), LilypondSyntaxHighlighter.CONSTANT),
    AttributesDescriptor(LilypondBundle.messagePointer("color.settings.bad.character"), LilypondSyntaxHighlighter.BAD_CHARACTER),
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