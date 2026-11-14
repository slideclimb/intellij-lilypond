package nl.abbyberkers.lilypond.run.core

import org.junit.Assert.assertEquals
import org.junit.Test

class LilypondOutputsTest {
    @Test
    fun `finds the single produced pdf`() {
        assertEquals(
            listOf("song.pdf"),
            LilypondOutputs.selectProducedPdfs("song", listOf("song.pdf", "song.midi")),
        )
    }

    @Test
    fun `orders numbered files numerically after the unnumbered one`() {
        assertEquals(
            listOf("song.pdf", "song-1.pdf", "song-2.pdf", "song-10.pdf"),
            LilypondOutputs.selectProducedPdfs(
                "song",
                listOf("song-2.pdf", "song.pdf", "song-10.pdf", "song-1.pdf"),
            ),
        )
    }

    @Test
    fun `handles a multi book score with no unnumbered pdf`() {
        assertEquals(
            listOf("song-1.pdf", "song-2.pdf"),
            LilypondOutputs.selectProducedPdfs("song", listOf("song-2.pdf", "song-1.pdf")),
        )
    }

    @Test
    fun `ignores files belonging to other scores or formats`() {
        assertEquals(
            emptyList<String>(),
            LilypondOutputs.selectProducedPdfs(
                "song",
                listOf("songextra.pdf", "song-1.midi", "other.pdf", "song.pdf.bak", "song-a.pdf"),
            ),
        )
    }

    @Test
    fun `handles an empty directory`() {
        assertEquals(emptyList<String>(), LilypondOutputs.selectProducedPdfs("song", emptyList()))
    }

    @Test
    fun `treats the basename literally`() {
        assertEquals(
            listOf("a.b.pdf"),
            LilypondOutputs.selectProducedPdfs("a.b", listOf("a.b.pdf", "axb.pdf")),
        )
    }
}
