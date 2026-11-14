package nl.abbyberkers.lilypond.run

import com.intellij.execution.configurations.RuntimeConfigurationError
import com.intellij.execution.configurations.RuntimeConfigurationWarning
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.abbyberkers.lilypond.run.pdf.PdfViewerMode
import java.io.File

class LilypondCheckConfigurationTest : BasePlatformTestCase() {
    private lateinit var score: File

    override fun setUp() {
        super.setUp()
        // A real file on disk: checkConfiguration runs under a read lock and looks at the filesystem
        // rather than the VFS.
        score = File.createTempFile("song", ".ly").apply { deleteOnExit() }
    }

    /**
     * A configuration that is valid apart from whatever the individual test breaks. The executable is
     * set explicitly so the tests do not depend on lilypond being installed.
     */
    private fun validConfiguration() =
        LilypondRunConfigurationType.instance.createTemplateConfiguration(project).apply {
            options.mainFilePath = score.absolutePath
            options.executablePath = executableOnThisMachine()
        }

    private fun executableOnThisMachine() =
        listOf("/bin/sh", "/usr/bin/sh", "C:\\Windows\\System32\\cmd.exe")
            .first { File(it).canExecute() }

    fun testAcceptsAValidConfiguration() {
        validConfiguration().checkConfiguration()
    }

    fun testRejectsAMissingMainFile() {
        val configuration = validConfiguration().apply { options.mainFilePath = null }

        assertThrows(RuntimeConfigurationError::class.java) { configuration.checkConfiguration() }
    }

    fun testRejectsAMainFileThatDoesNotExist() {
        val configuration = validConfiguration().apply { options.mainFilePath = "/nowhere/song.ly" }

        assertThrows(RuntimeConfigurationError::class.java) { configuration.checkConfiguration() }
    }

    fun testRejectsAnExecutableThatDoesNotExist() {
        val configuration = validConfiguration().apply { options.executablePath = "/nowhere/lilypond" }

        assertThrows(RuntimeConfigurationError::class.java) { configuration.checkConfiguration() }
    }

    fun testRejectsABlankCustomViewerCommand() {
        val configuration = validConfiguration().apply {
            options.pdfViewer = PdfViewerMode.CUSTOM_COMMAND
            options.customViewerCommand = "  "
        }

        assertThrows(RuntimeConfigurationError::class.java) { configuration.checkConfiguration() }
    }

    /**
     * Only a warning: the PDF path is appended when the placeholder is missing, so the command still works.
     */
    fun testWarnsAboutACustomCommandWithoutThePlaceholder() {
        val configuration = validConfiguration().apply {
            options.pdfViewer = PdfViewerMode.CUSTOM_COMMAND
            options.customViewerCommand = "evince"
        }

        assertThrows(RuntimeConfigurationWarning::class.java) { configuration.checkConfiguration() }
    }

    fun testWarnsAboutAFileThatIsNotAScore() {
        val other = File.createTempFile("notes", ".txt").apply { deleteOnExit() }
        val configuration = validConfiguration().apply { options.mainFilePath = other.absolutePath }

        assertThrows(RuntimeConfigurationWarning::class.java) { configuration.checkConfiguration() }
    }

    private fun <T : Throwable> assertThrows(expected: Class<T>, block: () -> Unit) {
        try {
            block()
        } catch (e: Throwable) {
            assertInstanceOf(e, expected)
            return
        }
        fail("Expected ${expected.simpleName} to be thrown")
    }
}
