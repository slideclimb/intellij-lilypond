package nl.abbyberkers.lilypond.run

import nl.abbyberkers.lilypond.run.core.LilypondCommandLineBuilder
import nl.abbyberkers.lilypond.run.core.LilypondCompileRequest
import nl.abbyberkers.lilypond.run.core.LilypondOutputs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Runs the real `lilypond` binary to pin down the parts of its command-line contract the plugin
 * depends on. Skipped when LilyPond is not installed, so it does not break a machine without it.
 */
class LilypondCompileIntegrationTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var executable: String
    private lateinit var scores: File
    private lateinit var output: File

    @Before
    fun setUp() {
        val found = LilypondExecutable.findOnPath()
        assumeNotNull(found)
        executable = found!!.absolutePath
        scores = temporaryFolder.newFolder("scores")
        output = temporaryFolder.newFolder("out")
    }

    private fun score(name: String, content: String) =
        File(scores, name).apply { writeText(content) }

    private fun compile(mainFile: File): Int {
        val invocation = LilypondCommandLineBuilder.build(
            LilypondCompileRequest(
                executablePath = executable,
                mainFilePath = mainFile.absolutePath,
                outputDirectory = output.absolutePath,
            ),
        )
        val process = ProcessBuilder(listOf(invocation.executablePath) + invocation.parameters)
            .directory(File(invocation.workingDirectory))
            .redirectErrorStream(true)
            .start()
        assertTrue("lilypond did not finish in time", process.waitFor(2, TimeUnit.MINUTES))
        return process.exitValue()
    }

    @Test
    fun `writes the pdf into the output directory under the source basename`() {
        val exitCode = compile(score("song.ly", """\relative c' { c4 d e f }"""))

        assertEquals(0, exitCode)
        assertTrue("Expected song.pdf in $output, found ${output.list()?.toList()}", File(output, "song.pdf").isFile)
    }

    @Test
    fun `does not double the pdf extension`() {
        compile(score("song.ly", """\relative c' { c4 }"""))

        // LilyPond appends the extension to --output, so passing a path ending in .pdf would produce
        // song.pdf.pdf. This is why the builder passes a basename.
        assertFalse(File(output, "song.pdf.pdf").exists())
    }

    @Test
    fun `numbers the pdfs of a score with several books`() {
        val exitCode = compile(
            score(
                "twobooks.ly",
                """
                \book { \score { \relative c' { c4 } } }
                \book { \score { \relative c' { d4 } } }
                """.trimIndent(),
            ),
        )

        assertEquals(0, exitCode)
        // The first book keeps the plain basename and the rest are numbered from 1 (LilyPond 2.26).
        assertEquals(
            listOf("twobooks.pdf", "twobooks-1.pdf"),
            LilypondOutputs.selectProducedPdfs("twobooks", output.list()!!.toList()),
        )
    }
}
