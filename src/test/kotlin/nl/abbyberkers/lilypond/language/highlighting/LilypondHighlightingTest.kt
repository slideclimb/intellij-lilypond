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
        // The duration is a standalone DIGIT token here (lexer-colored, see
        // LilypondLexerTest.detachedDurationIsOneDigitToken), so only pitch + octave are annotated.
        assertEquals(listOf(Painted("gis", PITCH), Painted("'", OCTAVE)), notePainting("{ gis'8 }"))
        assertEquals(listOf(Painted("gis", PITCH), Painted("'", OCTAVE)), notePainting("{ gis'16 }"))
        assertEquals(
            listOf(Painted("c", PITCH), Painted("'", OCTAVE), Painted("'", OCTAVE)),
            notePainting("{ c'' }"),
        )
    }

    fun testContractedFlatNames() {
        assertEquals(listOf(Painted("es", PITCH), Painted("16", NUMBER)), notePainting("{ es16 }"))
        assertEquals(listOf(Painted("as", PITCH), Painted("8", NUMBER)), notePainting("{ as8 }"))
        assertEquals(listOf(Painted("eses", PITCH)), notePainting("{ eses }"))
        assertEquals(listOf(Painted("ases", PITCH), Painted("4", NUMBER)), notePainting("{ ases4 }"))
        // The uncontracted spellings, a letter plus an accidental, keep working.
        assertEquals(listOf(Painted("ees", PITCH), Painted("16", NUMBER)), notePainting("{ ees16 }"))
        assertEquals(listOf(Painted("aeses", PITCH)), notePainting("{ aeses }"))
    }

    fun testNoteWithDurationMultiplier() {
        // The multiplier's own numbers are DIGIT tokens (lexer-colored); the note word before it
        // is what the annotator has to keep recognising.
        assertEquals(listOf(Painted("R", PITCH), Painted("1", NUMBER)), notePainting("{ R1*15 }"))
        assertEquals(listOf(Painted("c", PITCH), Painted("4", NUMBER)), notePainting("{ c4*2/3 }"))
    }

    fun testArticulationsDoNotSuppressColoring() {
        // Each of these keeps the trailing character inside the note's own WORD token.
        assertEquals(listOf(Painted("c", PITCH), Painted("4", NUMBER)), notePainting("{ c4-. }"))
        assertEquals(listOf(Painted("c", PITCH), Painted("4", NUMBER)), notePainting("{ c4-- }"))
        assertEquals(listOf(Painted("c", PITCH), Painted("4", NUMBER)), notePainting("{ c4-+ }"))
        assertEquals(listOf(Painted("c", PITCH), Painted("4", NUMBER)), notePainting("{ c4^\\marcato }"))
        assertEquals(listOf(Painted("c", PITCH), Painted("4", NUMBER)), notePainting("{ c4_\"text\" }"))
        assertEquals(listOf(Painted("c", PITCH), Painted("4", NUMBER)), notePainting("{ c4~ }"))
        assertEquals(listOf(Painted("c", PITCH), Painted("4", NUMBER)), notePainting("{ c4:8 }"))
        assertEquals(listOf(Painted("c", PITCH), Painted("4", NUMBER)), notePainting("{ c4-1 }"))
        // A cautionary accidental comes between the pitch and the duration.
        assertEquals(listOf(Painted("cis", PITCH), Painted("4", NUMBER)), notePainting("{ cis?4 }"))
        // An articulation on a note without a duration still leaves the pitch colored.
        assertEquals(listOf(Painted("c", PITCH)), notePainting("{ c~ }"))
    }

    fun testNonNoteWordNotColored() {
        assertEmpty(notePainting("\\clef treble").filter { it.key == PITCH })
        // A word that merely starts like a note is not one: the tail is not articulation.
        assertEmpty(notePainting("\\repeat unfold 2 { d4 }").filter { it.text == "unfold" })
        assertEmpty(notePainting("{ as-is }").filter { it.key == PITCH })
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
