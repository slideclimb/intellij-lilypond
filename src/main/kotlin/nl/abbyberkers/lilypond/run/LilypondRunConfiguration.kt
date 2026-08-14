package nl.abbyberkers.lilypond.run

import com.intellij.execution.Executor
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.LocatableConfigurationBase
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.RuntimeConfigurationError
import com.intellij.execution.configurations.RuntimeConfigurationWarning
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import nl.abbyberkers.lilypond.LilypondBundle
import nl.abbyberkers.lilypond.run.core.CustomViewerCommand
import nl.abbyberkers.lilypond.run.core.LilypondCompileRequest
import nl.abbyberkers.lilypond.run.core.LilypondPaths
import nl.abbyberkers.lilypond.run.pdf.PdfViewerMode
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

class LilypondRunConfiguration(project: Project, factory: ConfigurationFactory, name: String) :
    LocatableConfigurationBase<LilypondRunConfigurationOptions>(project, factory, name) {
    public override fun getOptions(): LilypondRunConfigurationOptions =
        super.getOptions() as LilypondRunConfigurationOptions

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> =
        LilypondRunConfigurationEditor(project)

    override fun getState(executor: Executor, environment: ExecutionEnvironment) =
        LilypondCommandLineState(this, environment)

    override fun suggestedName(): String? =
        options.mainFilePath?.substringAfterLast('/')?.takeIf { it.isNotBlank() }

    /**
     * Whether this configuration compiles the file at [path], which must be absolute.
     *
     * The stored path is collapsed to `{projectDir}/...`, so it has to be expanded to compare.
     */
    fun compiles(path: String): Boolean {
        val configured = LilypondPaths.expand(options.mainFilePath, project.basePath) ?: return false
        return FileUtil.pathsEqual(configured, path)
    }

    /**
     * Resolves every stored placeholder against the project.
     *
     * @throws RuntimeConfigurationError if a required path or the executable cannot be resolved.
     */
    fun buildCompileRequest(): LilypondCompileRequest {
        val mainFile = LilypondPaths.expand(options.mainFilePath, project.basePath)
            ?: throw RuntimeConfigurationError(LilypondBundle.message("run.error.main.file.missing"))
        val outputDirectory =
            LilypondPaths.expand(options.outputDirectory, project.basePath, fallbackBase = LilypondPaths.parentOf(mainFile))
                ?: throw RuntimeConfigurationError(LilypondBundle.message("run.error.output.directory.missing"))

        return LilypondCompileRequest(
            executablePath = resolveExecutable(),
            mainFilePath = mainFile,
            outputDirectory = outputDirectory,
            extraArguments = options.extraArguments.orEmpty(),
        )
    }

    private fun resolveExecutable(): String {
        options.executablePath?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }
        return LilypondExecutable.findOnPath()?.absolutePath
            ?: throw RuntimeConfigurationError(LilypondBundle.message("run.error.executable.not.found"))
    }

    /**
     * Runs under a read lock, so this may touch the filesystem directly but must not refresh the VFS.
     * Errors are raised before warnings, because only the first one thrown is shown.
     */
    override fun checkConfiguration() {
        val mainFile = LilypondPaths.expand(options.mainFilePath, project.basePath)
            ?: throw RuntimeConfigurationError(LilypondBundle.message("run.error.main.file.missing"))
        if (!Files.isRegularFile(Path.of(mainFile))) {
            throw RuntimeConfigurationError(LilypondBundle.message("run.error.main.file.not.found", mainFile))
        }

        val executable = options.executablePath?.trim().orEmpty()
        if (executable.isEmpty()) {
            if (LilypondExecutable.findOnPath() == null) {
                throw RuntimeConfigurationError(LilypondBundle.message("run.error.executable.not.found"))
            }
        } else {
            val file = File(executable)
            if (!file.isFile || !file.canExecute()) {
                throw RuntimeConfigurationError(
                    LilypondBundle.message("run.error.executable.not.executable", executable),
                )
            }
        }

        if (options.outputDirectory.isNullOrBlank()) {
            throw RuntimeConfigurationError(LilypondBundle.message("run.error.output.directory.missing"))
        }

        val customCommand = options.customViewerCommand?.trim().orEmpty()
        if (options.pdfViewer == PdfViewerMode.CUSTOM_COMMAND && customCommand.isEmpty()) {
            throw RuntimeConfigurationError(LilypondBundle.message("run.error.custom.command.missing"))
        }

        if (!LilypondPaths.isCompilable(mainFile)) {
            throw RuntimeConfigurationWarning(LilypondBundle.message("run.error.main.file.wrong.type", mainFile))
        }
        if (options.pdfViewer == PdfViewerMode.CUSTOM_COMMAND &&
            !customCommand.contains(CustomViewerCommand.PLACEHOLDER)
        ) {
            throw RuntimeConfigurationWarning(
                LilypondBundle.message("run.warning.custom.command.no.placeholder", CustomViewerCommand.PLACEHOLDER),
            )
        }
    }
}
