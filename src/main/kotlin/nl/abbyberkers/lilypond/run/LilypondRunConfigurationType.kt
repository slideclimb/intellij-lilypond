package nl.abbyberkers.lilypond.run

import com.intellij.execution.configurations.ConfigurationTypeUtil
import com.intellij.execution.configurations.SimpleConfigurationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NotNullLazyValue
import nl.abbyberkers.lilypond.LilypondBundle
import nl.abbyberkers.lilypond.LilypondIcons

/**
 * [SimpleConfigurationType] is both the type and its single factory, which is what makes the factory
 * id a stable constant rather than a convention: its `getId` is final and returns [ID].
 */
class LilypondRunConfigurationType : SimpleConfigurationType(
    ID,
    LilypondBundle.message("run.configuration.name"),
    LilypondBundle.message("run.configuration.description"),
    // Lazy because the type is constructed eagerly while extensions are registered.
    NotNullLazyValue.createValue { LilypondIcons.FILE },
) {
    override fun createTemplateConfiguration(project: Project) =
        LilypondRunConfiguration(project, this, LilypondBundle.message("run.configuration.name"))

    override fun getOptionsClass() = LilypondRunConfigurationOptions::class.java

    override fun isEditableInDumbMode() = true

    companion object {
        // Changing this orphans every configuration users have saved.
        const val ID = "LilypondRunConfiguration"

        val instance: LilypondRunConfigurationType
            get() = ConfigurationTypeUtil.findConfigurationType(LilypondRunConfigurationType::class.java)
    }
}
