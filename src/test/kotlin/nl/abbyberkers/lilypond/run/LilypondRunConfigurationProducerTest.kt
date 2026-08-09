package nl.abbyberkers.lilypond.run

import com.intellij.execution.RunManager
import com.intellij.execution.actions.ConfigurationContext
import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class LilypondRunConfigurationProducerTest : BasePlatformTestCase() {
    private val producer = LilypondRunConfigurationProducer()

    private fun produce(file: PsiFile): LilypondRunConfiguration? =
        producer.createConfigurationFromContext(ConfigurationContext(file))?.configuration as? LilypondRunConfiguration

    fun testCreatesConfigurationForLilypondFile() {
        val file = myFixture.configureByText("song.ly", "\\relative c' { c4 }")

        val configuration = produce(file)

        assertNotNull("No configuration was produced for a .ly file", configuration)
        assertTrue(configuration!!.options.mainFilePath!!.endsWith("song.ly"))
        assertEquals("song.ly", configuration.name)
    }

    fun testRefusesIncludeFiles() {
        assertNull(produce(myFixture.configureByText("library.ily", "\\version \"2.24.0\"")))
    }

    fun testRefusesOtherFileTypes() {
        assertNull(produce(myFixture.configureByText("readme.txt", "not a score")))
    }

    fun testRecognisesItsOwnConfiguration() {
        val file = myFixture.configureByText("song.ly", "\\relative c' { c4 }")
        val configuration = produce(file)!!

        assertTrue(
            "The stored path is collapsed, so it must be expanded again to compare",
            producer.isConfigurationFromContext(configuration, ConfigurationContext(file)),
        )
    }

    fun testDoesNotRecogniseAConfigurationForAnotherFile() {
        val configuration = produce(myFixture.configureByText("song.ly", "\\relative c' { c4 }"))!!
        val other = myFixture.configureByText("other.ly", "\\relative c' { d4 }")

        assertFalse(producer.isConfigurationFromContext(configuration, ConfigurationContext(other)))
    }

    /**
     * The template is where users edit their defaults, so everything but the file has to come from it.
     */
    fun testInheritsSettingsFromTheTemplate() {
        val type = LilypondRunConfigurationType.instance
        val template = RunManager.getInstance(project).getConfigurationTemplate(type).configuration
            as LilypondRunConfiguration
        template.options.outputDirectory = "{projectDir}/scores-out"
        template.options.executablePath = "/opt/lilypond/bin/lilypond"

        val configuration = produce(myFixture.configureByText("song.ly", "\\relative c' { c4 }"))!!

        assertEquals("{projectDir}/scores-out", configuration.options.outputDirectory)
        assertEquals("/opt/lilypond/bin/lilypond", configuration.options.executablePath)
    }
}
