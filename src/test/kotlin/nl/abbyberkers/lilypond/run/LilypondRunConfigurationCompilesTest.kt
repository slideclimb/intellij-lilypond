package nl.abbyberkers.lilypond.run

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class LilypondRunConfigurationCompilesTest : BasePlatformTestCase() {
    private fun configurationFor(mainFilePath: String) =
        LilypondRunConfigurationType.instance.createTemplateConfiguration(project).apply {
            options.mainFilePath = mainFilePath
        }

    private fun inProject(relativePath: String) =
        "${requireNotNull(project.basePath) { "The test project has no base path to resolve {projectDir} against" }}" +
            "/$relativePath"

    fun testMatchesTheConfiguredFile() {
        val configuration = configurationFor("{projectDir}/scores/song.ly")

        assertTrue(
            "The stored path is collapsed, so it must be expanded before comparing",
            configuration.compiles(inProject("scores/song.ly")),
        )
    }

    fun testRejectsAnotherFile() {
        val configuration = configurationFor("{projectDir}/scores/song.ly")

        assertFalse(configuration.compiles(inProject("scores/other.ly")))
        assertFalse(configuration.compiles(inProject("song.ly")))
    }

    fun testMatchesAnAbsoluteStoredPath() {
        val absolute = inProject("scores/song.ly")

        assertTrue(configurationFor(absolute).compiles(absolute))
    }

    /**
     * The switcher passes `VirtualFile.getPath()` straight through, so both sides have to be canonicalized.
     */
    fun testMatchesRegardlessOfPathForm() {
        val configuration = configurationFor("{projectDir}/scores/song.ly")

        assertTrue(configuration.compiles(inProject("scores/../scores/song.ly")))
    }

    fun testRejectsEverythingWhenNoFileIsConfigured() {
        assertFalse(configurationFor("  ").compiles(inProject("scores/song.ly")))
    }
}
