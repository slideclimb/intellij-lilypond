package nl.abbyberkers.lilypond.run.pdf

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.VirtualFile
import nl.abbyberkers.lilypond.notification.LilypondNotifications
import nl.abbyberkers.lilypond.run.core.CustomViewerCommand
import java.awt.Desktop
import java.nio.file.Path

object PdfOpener {
    private val log = logger<PdfOpener>()

    /**
     * Must be called on the EDT. [builtInAvailable] is passed in rather than computed here because
     * determining it needs a read lock.
     */
    fun open(
        project: Project,
        pdf: Path,
        virtualFile: VirtualFile?,
        settings: PdfViewerSettings,
        builtInAvailable: Boolean,
    ) {
        when (settings.mode) {
            PdfViewerMode.NONE -> return

            PdfViewerMode.BUILT_IN ->
                if (virtualFile != null && builtInAvailable) {
                    FileEditorManager.getInstance(project).openFile(virtualFile, false)
                } else {
                    LilypondNotifications.builtInPdfViewerUnavailable(project)
                    openWithSystemViewer(pdf)
                }

            PdfViewerMode.SYSTEM_DEFAULT -> openWithSystemViewer(pdf)

            PdfViewerMode.CUSTOM_COMMAND -> runCustomCommand(settings.customCommand, pdf)
        }
    }

    private fun openWithSystemViewer(pdf: Path) {
        // Deliberately not RevealFileAction (which shows the file in the file manager) or
        // BrowserUtil.browse (which forces a browser rather than the registered PDF handler).
        val command = when {
            SystemInfo.isMac -> listOf("open", pdf.toString())
            SystemInfo.isWindows -> listOf("rundll32", "url.dll,FileProtocolHandler", pdf.toString())
            else -> listOf("xdg-open", pdf.toString())
        }
        runDetached(command) { Desktop.getDesktop().open(pdf.toFile()) }
    }

    private fun runCustomCommand(customCommand: String?, pdf: Path) {
        val command = CustomViewerCommand.buildArgv(customCommand, pdf.toString())
        if (command == null) {
            log.warn("No custom PDF viewer command configured, not opening ${pdf.fileName}")
            return
        }
        runDetached(command) {}
    }

    /**
     * These calls can block for seconds, and a viewer that fails to start must not fail a build that
     * otherwise succeeded.
     */
    private fun runDetached(command: List<String>, fallback: () -> Unit) {
        ApplicationManager.getApplication().executeOnPooledThread {
            runCatching { GeneralCommandLine(command).createProcess() }
                .onFailure { failure ->
                    log.warn("Could not run ${command.first()}", failure)
                    runCatching(fallback).onFailure { log.warn("Could not open the PDF", it) }
                }
        }
    }
}
