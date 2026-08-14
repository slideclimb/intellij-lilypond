package nl.abbyberkers.lilypond.run

import com.intellij.execution.RunManager
import com.intellij.execution.RunManagerListener
import com.intellij.execution.configurations.UnknownConfigurationType
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class LilypondTemporaryConfigurationKeeperTest : BasePlatformTestCase() {
    private fun subscribe() {
        project.messageBus.connect(testRootDisposable)
            .subscribe(RunManagerListener.TOPIC, LilypondTemporaryConfigurationKeeper(project))
    }

    /**
     * Goes through the message bus rather than calling the listener directly, so that it also covers
     * making a configuration stable from inside the notification of its own addition.
     */
    fun testKeepsAConfigurationThatWasAddedTemporarily() {
        val runManager = RunManager.getInstance(project)
        subscribe()
        val settings = runManager.createConfiguration("song.ly", LilypondRunConfigurationType.instance)

        // This is how the platform adds a configuration created from a file's context.
        runManager.setTemporaryConfiguration(settings)

        assertFalse(
            "A configuration left temporary is evicted once the limit of five is exceeded",
            settings.isTemporary,
        )
        assertTrue("Making it stable must keep it in the list", runManager.allSettings.contains(settings))
        assertEmpty(runManager.tempConfigurationsList)
    }

    fun testLeavesOtherConfigurationTypesTemporary() {
        val runManager = RunManager.getInstance(project)
        val settings = runManager.createConfiguration("something else", UnknownConfigurationType.getInstance())
        settings.isTemporary = true

        LilypondTemporaryConfigurationKeeper(project).runConfigurationAdded(settings)

        assertTrue("Only LilyPond configurations are ours to keep", settings.isTemporary)
    }
}
