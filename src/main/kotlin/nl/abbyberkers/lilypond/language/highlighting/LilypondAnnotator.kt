package nl.abbyberkers.lilypond.language.highlighting

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import nl.abbyberkers.lilypond.language.psi.LilypondTypes

/**
 * Default (Dutch) note names — a letter with an optional accidental — plus rests (r, R),
 * spacer (s) and chord repeat (q), followed by an optional attached duration. Accidentals
 * are listed longest-first so `isis`/`eses` win over `is`/`es`.
 */
private val NOTE = Regex("^(?<pitch>[a-g](?:isis|eses|is|es|ih|eh)?|[rRsq])(?<duration>\\d+)?$")

class LilypondAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when (element.node?.elementType) {
            LilypondTypes.WORD -> annotateNote(element, holder)
            LilypondTypes.SINGLE_QUOTE, LilypondTypes.COMMA ->
                if (isOctaveMark(element)) holder.paint(element.textRange, LilypondSyntaxHighlighter.OCTAVE)
        }
    }

    /**
     * Colors the parts of a note. Pitch and (attached) duration live inside a single WORD token —
     * e.g. `c4`, `cis16` — because the lexer fuses a note letter with its trailing duration digits,
     * so they can only be separated as sub-ranges here. Octave marks (' ,) are their own tokens
     * following the note.
     *
     * Only WORD tokens are treated as notes. Words in non-note modes (e.g. lyric mode) are emitted
     * by the lexer as LYRICS_WORD, so syllables like `as` or a lyric comma are never colored.
     */
    private fun annotateNote(element: PsiElement, holder: AnnotationHolder) {
        if (isNoteWord(element)) {
            val match = NOTE.matchEntire(element.text) ?: return
            val start = element.textRange.startOffset
            val pitch = match.groups["pitch"]!!.range
            holder.paint(TextRange(start + pitch.first, start + pitch.last + 1), LilypondSyntaxHighlighter.PITCH)
            match.groups["duration"]?.range?.let { dur ->
                holder.paint(TextRange(start + dur.first, start + dur.last + 1), LilypondSyntaxHighlighter.NUMBER)
            }
        }
    }

    /**
     * An ' or , is an octave mark when it is contiguously attached to a note word, possibly after
     * other octave marks (`c''`, `cis,,`). Walking back from the mark keeps the range inside the
     * element currently being annotated.
     */
    private fun isOctaveMark(element: PsiElement): Boolean {
        var prev = element.prevSibling
        var expectedEnd = element.textRange.startOffset
        while (prev != null) {
            if (prev.textRange.endOffset != expectedEnd) return false
            when (prev.node?.elementType) {
                LilypondTypes.SINGLE_QUOTE, LilypondTypes.COMMA -> {
                    expectedEnd = prev.textRange.startOffset
                    prev = prev.prevSibling
                }
                LilypondTypes.WORD -> return isNoteWord(prev)
                else -> return false
            }
        }
        return false
    }

    /**
     * A note-shaped WORD that is not a variable definition's left-hand side (`bes = ...`).
     */
    private fun isNoteWord(word: PsiElement): Boolean =
        word.parent?.node?.elementType != LilypondTypes.ASSIGNMENT_NAME && NOTE.matches(word.text)

    private fun AnnotationHolder.paint(range: TextRange, key: TextAttributesKey) =
        newSilentAnnotation(HighlightSeverity.INFORMATION).range(range).textAttributes(key).create()
}