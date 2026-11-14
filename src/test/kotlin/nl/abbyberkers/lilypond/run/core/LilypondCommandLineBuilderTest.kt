package nl.abbyberkers.lilypond.run.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LilypondCommandLineBuilderTest {
    private fun request(extraArguments: String = "") =
        LilypondCompileRequest(
            executablePath = "/usr/bin/lilypond",
            mainFilePath = "/home/me/project/scores/song.ly",
            outputDirectory = "/home/me/project/out",
            extraArguments = extraArguments,
        )

    @Test
    fun `builds the default command`() {
        val invocation = LilypondCommandLineBuilder.build(request())

        assertEquals("/usr/bin/lilypond", invocation.executablePath)
        assertEquals(
            listOf("--pdf", "--output=/home/me/project/out/song", "/home/me/project/scores/song.ly"),
            invocation.parameters,
        )
        assertEquals("/home/me/project/scores", invocation.workingDirectory)
    }

    @Test
    fun `never appends the pdf extension to the output`() {
        val output = LilypondCommandLineBuilder.build(request()).parameters.single { it.startsWith("--output=") }

        assertTrue("--output must be a basename, or LilyPond produces song.pdf.pdf", output.endsWith("/song"))
    }

    @Test
    fun `keeps quoted extra arguments as one token`() {
        val parameters = LilypondCommandLineBuilder.build(request("""-dpaper-size="a4 landscape"""")).parameters

        assertEquals(listOf("-dpaper-size=a4 landscape"), parameters.filter { it.startsWith("-dpaper") })
    }

    @Test
    fun `places extra arguments after the output and before the input file`() {
        val parameters = LilypondCommandLineBuilder.build(request("-dno-point-and-click")).parameters

        assertTrue(
            parameters.indexOf("--output=/home/me/project/out/song") < parameters.indexOf("-dno-point-and-click"),
        )
        assertEquals("/home/me/project/scores/song.ly", parameters.last())
    }

    @Test
    fun `ignores blank extra arguments`() {
        assertEquals(
            LilypondCommandLineBuilder.build(request()).parameters,
            LilypondCommandLineBuilder.build(request("   ")).parameters,
        )
    }
}
