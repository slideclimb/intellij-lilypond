package nl.abbyberkers.lilypond.run

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.Ref
import com.intellij.openapi.util.io.FileUtil
import com.intellij.psi.PsiElement
import nl.abbyberkers.lilypond.language.LilypondFileType
import nl.abbyberkers.lilypond.run.core.LilypondPaths

class LilypondRunConfigurationProducer : LazyRunConfigurationProducer<LilypondRunConfiguration>(), DumbAware {
    override fun getConfigurationFactory(): ConfigurationFactory = LilypondRunConfigurationType.instance

    /**
     * [configuration] is already a clone of the run configuration template, so only the file-specific
     * settings are touched here — everything else is inherited from the template, which is where
     * users edit their defaults.
     */
    override fun setupConfigurationFromContext(
        configuration: LilypondRunConfiguration,
        context: ConfigurationContext,
        sourceElement: Ref<PsiElement>,
    ): Boolean {
        val psiFile = context.psiLocation?.containingFile ?: return false
        val virtualFile = psiFile.virtualFile ?: return false
        if (virtualFile.fileType != LilypondFileType) return false
        if (!LilypondPaths.isCompilable(virtualFile.name)) return false

        configuration.options.mainFilePath = LilypondPaths.collapse(virtualFile.path, context.project.basePath)
        configuration.setGeneratedName()
        sourceElement.set(psiFile)
        return true
    }

    override fun isConfigurationFromContext(
        configuration: LilypondRunConfiguration,
        context: ConfigurationContext,
    ): Boolean {
        val path = context.psiLocation?.containingFile?.virtualFile?.path ?: return false
        // The stored path is collapsed to {projectDir}/..., so it has to be expanded to compare.
        val configured = LilypondPaths.expand(configuration.options.mainFilePath, context.project.basePath)
            ?: return false
        return FileUtil.pathsEqual(configured, path)
    }
}
