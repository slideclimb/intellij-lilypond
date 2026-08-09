package nl.abbyberkers.lilypond.run

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.RuntimeConfigurationError
import com.intellij.execution.filters.RegexpFilter
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import nl.abbyberkers.lilypond.run.core.LilypondCommandLineBuilder
import nl.abbyberkers.lilypond.run.pdf.PdfViewerSettings
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

class LilypondCommandLineState(
    private val configuration: LilypondRunConfiguration,
    environment: ExecutionEnvironment,
) : CommandLineState(environment) {
    init {
        // Must happen here rather than in startProcess(): CommandLineState.execute() creates the
        // console before it starts the process, so filters added later never reach it.
        addConsoleFilters(
            RegexpFilter(
                environment.project,
                "${RegexpFilter.FILE_PATH_MACROS}:${RegexpFilter.LINE_MACROS}:${RegexpFilter.COLUMN_MACROS}",
            ),
        )
    }

    override fun startProcess(): ProcessHandler {
        val request = try {
            configuration.buildCompileRequest()
        } catch (e: RuntimeConfigurationError) {
            throw ExecutionException(e)
        }

        // LilyPond writes into the output directory but will not create it.
        Files.createDirectories(Path.of(request.outputDirectory))

        val invocation = LilypondCommandLineBuilder.build(request)
        val commandLine = GeneralCommandLine(invocation.executablePath)
            .withParameters(invocation.parameters)
            .withWorkingDirectory(Path.of(invocation.workingDirectory))
            .withCharset(StandardCharsets.UTF_8)
            // Without the console environment, a GUI-launched IDE on macOS has a stunted PATH and
            // cannot find a lilypond that the user's terminal finds.
            .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)

        val handler = ProcessHandlerFactory.getInstance().createColoredProcessHandler(commandLine)
        ProcessTerminatedListener.attach(handler)
        handler.addProcessListener(
            LilypondPostRunListener(
                environment.project,
                request,
                PdfViewerSettings(configuration.options.pdfViewer, configuration.options.customViewerCommand),
            ),
        )
        return handler
    }
}
