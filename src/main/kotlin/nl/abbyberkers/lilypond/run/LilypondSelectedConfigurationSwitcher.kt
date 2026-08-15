package nl.abbyberkers.lilypond.run

import com.intellij.execution.RunManager
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

/**
 * Follows the editor selection with the run configuration combobox, so that Run compiles the score
 * that is on screen.
 *
 * It only ever switches *to* a configuration that already compiles the newly selected file, and never
 * clears the selection: selecting a file that has no configuration of its own leaves whatever the user
 * picked in the combobox alone, rather than emptying it.
 *
 * Every [FileEditorManagerListener] callback runs on the EDT, which is why the match is a plain string
 * comparison against the stored paths and deliberately touches no PSI, VFS or indexes. Asking the
 * producer instead would mean building a [com.intellij.execution.actions.ConfigurationContext], whose
 * PSI resolution risks the `SlowOperations` assertion on the EDT.
 */
class LilypondSelectedConfigurationSwitcher(private val project: Project) : FileEditorManagerListener {
    override fun selectionChanged(event: FileEditorManagerEvent) {
        val file = event.newFile ?: return
        val runManager = RunManager.getInstance(project)
        val match = findConfigurationFor(runManager, file) ?: return
        if (runManager.selectedConfiguration != match) runManager.selectedConfiguration = match
    }

    private fun findConfigurationFor(runManager: RunManager, file: VirtualFile): RunnerAndConfigurationSettings? =
        runManager.getConfigurationSettingsList(LilypondRunConfigurationType.instance).firstOrNull {
            (it.configuration as? LilypondRunConfiguration)?.compiles(file.path) == true
        }
}
