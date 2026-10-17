package nl.abbyberkers.lilypond.run

import com.intellij.openapi.util.JDOMUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.abbyberkers.lilypond.run.pdf.PdfViewerMode
import org.jdom.Element

class LilypondRunConfigurationOptionsTest : BasePlatformTestCase() {
    private fun newConfiguration() =
        LilypondRunConfigurationType.instance.createTemplateConfiguration(project)

    private fun configured() =
        newConfiguration().apply {
            options.mainFilePath = "{projectDir}/scores/song.ly"
            options.executablePath = "/opt/lilypond/bin/lilypond"
            options.outputDirectory = "{projectDir}/build/scores"
            options.extraArguments = "-dpaper-size=a4"
            options.pdfViewer = PdfViewerMode.CUSTOM_COMMAND
            options.customViewerCommand = "evince {pdf}"
        }

    fun testOptionsRoundTrip() {
        val element = Element("configuration")
        configured().writeExternal(element)

        val restored = newConfiguration()
        restored.readExternal(element)

        assertEquals("{projectDir}/scores/song.ly", restored.options.mainFilePath)
        assertEquals("/opt/lilypond/bin/lilypond", restored.options.executablePath)
        assertEquals("{projectDir}/build/scores", restored.options.outputDirectory)
        assertEquals("-dpaper-size=a4", restored.options.extraArguments)
        assertEquals(PdfViewerMode.CUSTOM_COMMAND, restored.options.pdfViewer)
        assertEquals("evince {pdf}", restored.options.customViewerCommand)
    }

    /**
     * The names are the persistence contract: renaming a property discards the stored value in every
     * configuration users have already saved.
     */
    fun testSerializedOptionNamesAreStable() {
        val element = Element("configuration")
        configured().writeExternal(element)
        val xml = JDOMUtil.write(element)

        for (name in listOf(
            "mainFilePath",
            "executablePath",
            "outputDirectory",
            "extraArguments",
            "pdfViewer",
            "customViewerCommand",
        )) {
            assertTrue("$name is missing from the serialized configuration:\n$xml", xml.contains("\"$name\""))
        }
    }

    /**
     * Keeping untouched values out of the XML is what makes a saved configuration small and portable.
     *
     * The viewer is put back to the property default first. On a machine with no PDF viewer plugin the
     * template seeds the system viewer instead, and that deviation is meant to be written — a value that
     * lived only in the template would not survive being reloaded elsewhere.
     */
    fun testUntouchedOptionsAreNotSerialized() {
        val element = Element("configuration")
        newConfiguration().apply { options.pdfViewer = PdfViewerMode.BUILT_IN }.writeExternal(element)
        val xml = JDOMUtil.write(element)

        assertFalse("Untouched options should stay out of the XML:\n$xml", xml.contains("outputDirectory"))
        assertFalse("Untouched options should stay out of the XML:\n$xml", xml.contains("pdfViewer"))
    }

    fun testOutputDirectoryDefaultsToProjectOut() {
        assertEquals("{projectDir}/out", newConfiguration().options.outputDirectory)
    }
}
