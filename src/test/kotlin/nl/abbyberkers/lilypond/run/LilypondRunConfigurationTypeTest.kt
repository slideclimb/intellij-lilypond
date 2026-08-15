package nl.abbyberkers.lilypond.run

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.abbyberkers.lilypond.run.pdf.PdfViewerMode

class LilypondRunConfigurationTypeTest : BasePlatformTestCase() {
    /**
     * The id is stored in every saved run configuration, so changing it must be a deliberate act.
     */
    fun testIdIsStable() {
        assertEquals("LilypondRunConfiguration", LilypondRunConfigurationType.instance.id)
    }

    fun testTypeIsItsOwnFactory() {
        val type = LilypondRunConfigurationType.instance
        assertEquals(listOf(type), type.configurationFactories.toList())
    }

    /**
     * The fixture has no PDF viewer plugin, so a fresh configuration must start on the system viewer rather
     * than on a built-in one that would only fall back and warn on every compile.
     */
    fun testTemplateFallsBackToTheSystemViewerWithoutABuiltInOne() {
        val template = LilypondRunConfigurationType.instance.createTemplateConfiguration(project)

        assertEquals(PdfViewerMode.SYSTEM_DEFAULT, template.options.pdfViewer)
    }
}
