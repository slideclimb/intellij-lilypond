package nl.abbyberkers.lilypond.run.pdf

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
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
                    openInEditor(project, virtualFile)
                } else {
                    LilypondNotifications.builtInPdfViewerUnavailable(project)
                    openWithSystemViewer(pdf)
                }

            PdfViewerMode.SYSTEM_DEFAULT -> openWithSystemViewer(pdf)

            PdfViewerMode.CUSTOM_COMMAND -> runCustomCommand(settings.customCommand, pdf)
        }
    }

    /**
     * Splitting is the last resort: a pane that is already showing a score is where the next one belongs.
     *
     * No branch requests focus, since the user is reading the score rather than the PDF. For the split
     * that is also load-bearing: `openInRightSplit` splits whichever window has focus, so focus in the
     * PDF pane would make the next compile split *that* pane, nesting splitters without end.
     *
     * The [runCatching] is not defensive noise, and it wraps the window search as well as the opening:
     * `splitters` throws outright in headless tests (`TestEditorManagerImpl`), and `openInRightSplit`
     * returns null when there is no window to split. A plain tab is the right answer to both.
     */
    private fun openInEditor(project: Project, pdf: VirtualFile) {
        val manager = FileEditorManagerEx.getInstanceEx(project)
        val placed = runCatching {
            val host = selectHostWindow(manager.windows.asList(), pdf.path) { window ->
                window.fileList.map { it.path }
            }
            if (host == null) {
                manager.splitters.openInRightSplit(pdf, false) != null
            } else {
                // openFile aims at the current window, and the overload taking an explicit window needs
                // an @ApiStatus.Internal options class, so point the current window at the host instead.
                // Only the pointer moves; focus stays where it is.
                host.setAsCurrentWindow(false)
                manager.openFile(pdf, false)
                true
            }
        }
            .onFailure { log.warn("Could not open ${pdf.name} beside the score", it) }
            .getOrDefault(false)

        if (!placed) manager.openFile(pdf, false)
    }

    /**
     * The already-open window that should show [pdfPath], or null when a fresh split is needed.
     *
     * The same score wins over any other PDF, so recompiling one of two open scores updates its own pane
     * instead of stealing the other's. Paths are compared rather than [VirtualFile] identities because
     * LilyPond rewrites the file: the instance in the tab can be an earlier one for the same path.
     *
     * Recognising PDFs by extension and not by file type is deliberate — the PDF file type belongs to the
     * third-party viewer plugin, so there is no type to compare against when it is missing.
     */
    internal fun <W> selectHostWindow(windows: List<W>, pdfPath: String, pathsIn: (W) -> List<String>): W? {
        var windowWithSomePdf: W? = null
        for (window in windows) {
            val paths = pathsIn(window)
            if (paths.any { it == pdfPath }) return window
            if (windowWithSomePdf == null && paths.any { it.endsWith(".pdf", ignoreCase = true) }) {
                windowWithSomePdf = window
            }
        }
        return windowWithSomePdf
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
