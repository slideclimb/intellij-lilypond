package nl.abbyberkers.lilypond.language.highlighting

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The color settings preview renders its demo text through the lexer highlighter only, applying
 * the additional-tag ranges on top; an undeclared tag is not an error there, it just shows up as
 * literal `<tag>` text in the preview, so the two are checked against each other here.
 */
class LilypondColorSettingsPageTest {
    private val page = LilypondColorSettingsPage()

    /** The tag names used in the demo text, e.g. `pitch` for both `<pitch>` and `</pitch>`. */
    private fun tagsInDemoText(): Set<String> =
        Regex("</?([a-z]+)>").findAll(page.demoText).map { it.groupValues[1] }.toSet()

    @Test
    fun everyDemoTextTagIsDeclared() {
        assertEquals(emptySet<String>(), tagsInDemoText() - page.additionalHighlightingTagToDescriptorMap.keys)
    }

    @Test
    fun everyDeclaredTagIsUsedInDemoText() {
        assertEquals(emptySet<String>(), page.additionalHighlightingTagToDescriptorMap.keys - tagsInDemoText())
    }

    /**
     * Every color the page lets you configure should be visible in the preview: either painted by
     * the lexer highlighter or tagged in the demo text. Only the keys the annotator owns can be
     * checked mechanically — those must be tagged, since the annotator does not run in the preview.
     */
    @Test
    fun annotatorColorsArePreviewed() {
        val tagged = page.additionalHighlightingTagToDescriptorMap.values.toSet()
        assertEquals(
            emptySet<Any>(),
            setOf(
                LilypondSyntaxHighlighter.PITCH,
                LilypondSyntaxHighlighter.OCTAVE,
                LilypondSyntaxHighlighter.NUMBER,
            ) - tagged,
        )
    }
}
