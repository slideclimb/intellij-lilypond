package nl.abbyberkers.lilypond.language.highlighting

import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Exercises the annotator-driven note highlighting end to end. Lexer token colors (command,
 * string, number, ...) are applied by the editor's lexer highlighter and do not surface in
 * doHighlighting(); only the annotator's pitch/duration/octave annotations do, so these tests
 * assert exactly on those.
 */
class LilypondHighlightingTest : BasePlatformTestCase() {
    private data class Painted(val text: String, val key: String)

    /** The note annotations (pitch/duration/octave), as (highlighted text, attribute key). */
    private fun notePainting(source: String): List<Painted> {
        myFixture.configureByText("test.ly", source)
        return myFixture.doHighlighting()
            .mapNotNull { info: HighlightInfo ->
                val key = info.forcedTextAttributesKey?.externalName ?: return@mapNotNull null
                if (key !in NOTE_KEYS) return@mapNotNull null
                Painted(source.substring(info.startOffset, info.endOffset), key)
            }
    }

    fun testPitchAndAttachedDurationSplitWithinOneWord() {
        assertEquals(listOf(Painted("c", PITCH), Painted("4", NUMBER)), notePainting("{ c4 }"))
        assertEquals(listOf(Painted("cis", PITCH), Painted("16", NUMBER)), notePainting("{ cis16 }"))
    }

    fun testBarePitchWithoutDuration() {
        assertEquals(listOf(Painted("c", PITCH)), notePainting("{ c d e }").filter { it.text == "c" })
        assertEquals(listOf(Painted("r", PITCH)), notePainting("{ r2 }").map { it }.filter { it.key == PITCH })
    }

    fun testOctaveMarksColored() {
        // The 8 is a standalone DIGIT token (lexer-colored), so only pitch + octave are annotated.
        assertEquals(listOf(Painted("gis", PITCH), Painted("'", OCTAVE)), notePainting("{ gis'8 }"))
        assertEquals(
            listOf(Painted("c", PITCH), Painted("'", OCTAVE), Painted("'", OCTAVE)),
            notePainting("{ c'' }"),
        )
    }

    fun testNonNoteWordNotColored() {
        assertEmpty(notePainting("\\clef treble").filter { it.key == PITCH })
    }

    fun testAssignmentNameNotColored() {
        val painting = notePainting("bes = { c4 }")
        assertEmpty(painting.filter { it.text == "bes" })
        assertContainsElements(painting, Painted("c", PITCH))
    }

    fun testLyricModeNotColored() {
        assertEmpty(notePainting("\\lyricmode { as a be }"))
        assertEmpty(notePainting("\\addlyrics { la la }"))
        assertEmpty(notePainting("\\lyricsto \"v\" { as }"))
    }

    fun testNoteAfterLyricBlockColoredAgain() {
        assertContainsElements(notePainting("\\lyricmode { as } c4"), Painted("c", PITCH))
    }

    companion object {
        private const val PITCH = "LILYPOND_PITCH"
        private const val OCTAVE = "LILYPOND_OCTAVE"
        private const val NUMBER = "LILYPOND_NUMBER"
        private val NOTE_KEYS = setOf(PITCH, OCTAVE, NUMBER)
    }
}
