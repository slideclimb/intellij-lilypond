package nl.abbyberkers.lilypond.run

import com.intellij.execution.RunManager
import com.intellij.execution.RunManagerListener
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.openapi.project.Project

/**
 * Keeps the run configurations that running a score from its context creates.
 *
 * The platform adds such a configuration as *temporary*, and temporary configurations are evicted in
 * LRU order once there are more than `temporary.configurations.limit` of them (five by default), so
 * compiling a sixth score would silently drop the configuration of the first. Making them stable
 * immediately is what gives a score's configuration the lifetime users expect of one.
 *
 * `makeStable` fires `runConfigurationChanged` rather than `runConfigurationAdded`, so reacting to an
 * addition here cannot loop.
 */
class LilypondTemporaryConfigurationKeeper(private val project: Project) : RunManagerListener {
    override fun runConfigurationAdded(settings: RunnerAndConfigurationSettings) {
        if (!settings.isTemporary) return
        if (settings.configuration !is LilypondRunConfiguration) return
        RunManager.getInstance(project).makeStable(settings)
    }
}
