package nl.abbyberkers.lilypond.run.pdf

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.ex.FileEditorProviderManager
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.LightVirtualFile

object PdfViewerAvailability {
    private const val PROBE_NAME = "preview.pdf"

    /**
     * Whether the IDE can render a PDF itself. A viewer is never a compile-time dependency: whichever
     * plugin provides one registers a `fileEditorProvider` for PDFs, so asking for the providers answers
     * the question directly — and answers it for a fork, a rebranded viewer or a future built-in one too.
     *
     * Asking by plugin id instead would need [com.intellij.ide.plugins.PluginManager]'s lookups, which are
     * `@ApiStatus.Internal` as of 2026.2; the only public one, `isPluginInstalled`, also reports plugins the
     * user has disabled, which register no provider and cannot open anything.
     *
     * Needs a read lock; call it off the EDT before switching to it.
     */
    fun isBuiltInViewerAvailable(project: Project, pdf: VirtualFile): Boolean =
        FileEditorProviderManager.getInstance().getProviderList(project, pdf).isNotEmpty()

    /**
     * The same question asked before any score has been compiled, for the settings UI and for the defaults
     * of a fresh configuration.
     *
     * The stand-in file carries the file type registered for `.pdf` rather than letting the platform detect
     * one from content, so the answer turns on which plugins are loaded and not on what an empty file looks
     * like. With no viewer installed that type is [com.intellij.openapi.fileTypes.UnknownFileType], which no
     * provider accepts.
     *
     * Takes its own read lock, because unlike the overload above this is called from the EDT while a
     * configuration is being built or its editor opened.
     */
    fun isBuiltInViewerAvailable(project: Project): Boolean {
        val fileType = FileTypeManager.getInstance().getFileTypeByFileName(PROBE_NAME)
        val probe = LightVirtualFile(PROBE_NAME, fileType, "")
        return ApplicationManager.getApplication().runReadAction(
            Computable { isBuiltInViewerAvailable(project, probe) },
        )
    }
}
