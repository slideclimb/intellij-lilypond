package nl.abbyberkers.lilypond.run

import com.intellij.execution.RunManager
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.openapi.options.advanced.AdvancedSettingBean
import com.intellij.openapi.options.advanced.AdvancedSettings
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.ExtensionTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Exercises the switcher through its real wiring: the `projectListeners` registration in plugin.xml is
 * what makes opening a file reach [LilypondSelectedConfigurationSwitcher].
 */
class LilypondSelectedConfigurationSwitcherTest : BasePlatformTestCase() {
    private val runManager: RunManager get() = RunManager.getInstance(project)

    override fun setUp() {
        super.setUp()
        registerFollowSelectedFileSetting()
    }

    /**
     * [AdvancedSettings.getBoolean] throws for an unregistered id, and the fixture only has whatever
     * `advancedSetting` extensions plugin.xml declares, so stand in for that registration when it is absent.
     */
    private fun registerFollowSelectedFileSetting() {
        if (AdvancedSettingBean.EP_NAME.extensionList.any { it.id == FOLLOW_SELECTED_FILE }) return
        val bean = AdvancedSettingBean().apply {
            id = FOLLOW_SELECTED_FILE
            defaultValue = "true"
        }
        ExtensionTestUtil.addExtensions(AdvancedSettingBean.EP_NAME, listOf(bean), testRootDisposable)
    }

    private fun openFile(name: String, text: String = "\\relative c' { c4 }"): VirtualFile =
        myFixture.configureByText(name, text).virtualFile

    private fun configurationFor(file: VirtualFile): RunnerAndConfigurationSettings {
        val settings = runManager.createConfiguration(file.name, LilypondRunConfigurationType.instance)
        // Absolute rather than collapsed: the fixture's files live outside the project base path.
        (settings.configuration as LilypondRunConfiguration).options.mainFilePath = file.path
        runManager.addConfiguration(settings)
        return settings
    }

    fun testSelectingAFileSelectsItsConfiguration() {
        val song = openFile("song.ly")
        val other = openFile("other.ly")
        val songConfiguration = configurationFor(song)
        runManager.selectedConfiguration = configurationFor(other)

        myFixture.openFileInEditor(song)

        assertEquals(songConfiguration, runManager.selectedConfiguration)
    }

    fun testSelectingAnIncludeFileSelectsItsConfiguration() {
        val include = openFile("library.ily", "\\version \"2.24.0\"")
        val other = openFile("other.ly")
        val includeConfiguration = configurationFor(include)
        runManager.selectedConfiguration = configurationFor(other)

        myFixture.openFileInEditor(include)

        assertEquals(includeConfiguration, runManager.selectedConfiguration)
    }

    /**
     * Clearing the combobox for a file that has no configuration of its own would throw away a
     * selection the user made deliberately.
     */
    fun testKeepsTheSelectionForAFileWithoutAConfiguration() {
        val unconfigured = openFile("unconfigured.ly")
        val other = openFile("other.ly")
        val otherConfiguration = configurationFor(other)
        runManager.selectedConfiguration = otherConfiguration

        myFixture.openFileInEditor(unconfigured)

        assertEquals(otherConfiguration, runManager.selectedConfiguration)
    }

    fun testDoesNothingWhenTheSettingIsOff() {
        val song = openFile("song.ly")
        val other = openFile("other.ly")
        configurationFor(song)
        val otherConfiguration = configurationFor(other)
        runManager.selectedConfiguration = otherConfiguration
        AdvancedSettings.setBoolean(FOLLOW_SELECTED_FILE, false)

        try {
            myFixture.openFileInEditor(song)

            assertEquals(otherConfiguration, runManager.selectedConfiguration)
        } finally {
            AdvancedSettings.setBoolean(FOLLOW_SELECTED_FILE, true)
        }
    }

    companion object {
        private const val FOLLOW_SELECTED_FILE = "lilypond.follow.selected.file"
    }
}
