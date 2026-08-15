package nl.abbyberkers.lilypond.language.parser

import com.intellij.psi.tree.IElementType
import nl.abbyberkers.lilypond.language.psi.LilypondTypes
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Verifies the lexer's input-mode tracking: bare words are notes (WORD) in note mode but
 * syllables (LYRICS_WORD) inside lyric mode, which is what keeps lyrics out of note highlighting.
 */
class LilypondLexerTest {
    private fun tokenType(text: String, word: String): IElementType {
        val lexer = LilypondLexerAdapter()
        lexer.start(text)
        while (lexer.tokenType != null) {
            if (lexer.tokenText == word) return lexer.tokenType!!
            lexer.advance()
        }
        error("token '$word' not found in '$text'")
    }

    @Test
    fun noteWordsAreWordInNoteMode() {
        assertEquals(LilypondTypes.WORD, tokenType("{ c4 d8 }", "c4"))
        assertEquals(LilypondTypes.WORD, tokenType("melody = \\relative c' { cis16 }", "cis16"))
    }

    /**
     * A duration detached from its note letter (by an octave mark, a chord's `>`, ...) must be one
     * DIGIT token: DIGIT is what the syntax highlighter colors as a number, and a multi-digit run
     * would otherwise be a WORD and stay uncolored, so `c,8` was highlighted but `c,16` was not.
     */
    @Test
    fun detachedDurationIsOneDigitToken() {
        assertEquals(LilypondTypes.DIGIT, tokenType("{ c,16 }", "16"))
        assertEquals(LilypondTypes.DIGIT, tokenType("{ c'16 }", "16"))
        assertEquals(LilypondTypes.DIGIT, tokenType("{ <c e>16 }", "16"))
        assertEquals(LilypondTypes.DIGIT, tokenType("\\tempo 4 = 120", "120"))
        // A digit run followed by letters is still a single WORD, as before.
        assertEquals(LilypondTypes.WORD, tokenType("{ 16a }", "16a"))
    }

    /**
     * A duration multiplier must split off the note word instead of being swallowed by it, so the
     * note stays recognisable and the multiplier's numbers are DIGIT tokens.
     */
    @Test
    fun durationMultiplierSplitsIntoOwnTokens() {
        assertEquals(LilypondTypes.WORD, tokenType("{ R1*15 }", "R1"))
        assertEquals(LilypondTypes.STAR, tokenType("{ R1*15 }", "*"))
        assertEquals(LilypondTypes.DIGIT, tokenType("{ R1*15 }", "15"))
        assertEquals(LilypondTypes.SLASH, tokenType("{ c4*2/3 }", "/"))
        assertEquals(LilypondTypes.DIGIT, tokenType("{ c4*2/3 }", "3"))
        assertEquals(LilypondTypes.DIGIT, tokenType("\\time 3/4", "3"))
    }

    @Test
    fun wordsAreModeWordInsideLyricMode() {
        assertEquals(LilypondTypes.LYRICS_WORD, tokenType("\\lyricmode { as a be }", "as"))
        assertEquals(LilypondTypes.LYRICS_WORD, tokenType("\\addlyrics { la la }", "la"))
        // The mode command may be separated from its brace body by an argument.
        assertEquals(LilypondTypes.LYRICS_WORD, tokenType("\\lyricsto \"v\" { as }", "as"))
    }

    @Test
    fun lyricModeEndsAtItsClosingBrace() {
        // The note after the lyric block is a note again, and inner braces do not end the mode early.
        assertEquals(LilypondTypes.WORD, tokenType("\\lyricmode { as } c4", "c4"))
        assertEquals(LilypondTypes.LYRICS_WORD, tokenType("\\lyricmode { a { b } c }", "c"))
    }
}
