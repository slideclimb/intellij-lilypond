package nl.abbyberkers.lilypond.run.pdf

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PdfOpenerTest {
    private val score = "/home/me/scores/song.ly"
    private val pdf = "/home/me/out/song.pdf"
    private val otherPdf = "/home/me/out/etude.pdf"

    /**
     * A window is represented by the paths it has open, so the selected window is recognisable by them.
     */
    private fun select(vararg windows: List<String>) =
        PdfOpener.selectHostWindow(windows.toList(), pdf) { it }

    @Test
    fun `the window already showing this score wins over one showing another`() {
        assertEquals(listOf(score, pdf), select(listOf(otherPdf), listOf(score, pdf)))
    }

    @Test
    fun `an earlier version of the same path counts as showing it`() {
        // The compiled file is deleted and rewritten on every run, so only its path stays the same.
        assertEquals(listOf(pdf), select(listOf(otherPdf), listOf(pdf)))
    }

    @Test
    fun `the first window showing this score is picked when several do`() {
        assertEquals(listOf(pdf, score), select(listOf(pdf, score), listOf(pdf)))
    }

    @Test
    fun `any pdf window will do when this score is not open`() {
        assertEquals(listOf(otherPdf), select(listOf(score), listOf(otherPdf), listOf("/home/me/out/air.pdf")))
    }

    @Test
    fun `an uppercase extension is still a pdf`() {
        assertEquals(listOf("/home/me/out/ETUDE.PDF"), select(listOf("/home/me/out/ETUDE.PDF")))
    }

    @Test
    fun `no window is chosen when none shows a pdf`() {
        assertNull(select(listOf(score), listOf("/home/me/notes.txt")))
        assertNull(select(emptyList()))
        assertNull(select())
    }
}
