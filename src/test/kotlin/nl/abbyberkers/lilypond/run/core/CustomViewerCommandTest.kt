package nl.abbyberkers.lilypond.run.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CustomViewerCommandTest {
    private val pdf = "/home/me/project/out/song.pdf"

    @Test
    fun `substitutes the placeholder`() {
        assertEquals(listOf("evince", pdf), CustomViewerCommand.buildArgv("evince {pdf}", pdf))
    }

    @Test
    fun `substitutes inside a token`() {
        assertEquals(
            listOf("zathura", "--synctex-forward=1:1:$pdf"),
            CustomViewerCommand.buildArgv("zathura --synctex-forward=1:1:{pdf}", pdf),
        )
    }

    @Test
    fun `keeps a quoted executable as one token`() {
        assertEquals(
            listOf("C:\\Program Files\\SumatraPDF\\SumatraPDF.exe", "-reuse-instance", pdf),
            CustomViewerCommand.buildArgv(""""C:\Program Files\SumatraPDF\SumatraPDF.exe" -reuse-instance {pdf}""", pdf),
        )
    }

    @Test
    fun `appends the path when there is no placeholder`() {
        assertEquals(listOf("evince", pdf), CustomViewerCommand.buildArgv("evince", pdf))
    }

    @Test
    fun `returns null for a blank command`() {
        assertNull(CustomViewerCommand.buildArgv(null, pdf))
        assertNull(CustomViewerCommand.buildArgv("   ", pdf))
    }
}
