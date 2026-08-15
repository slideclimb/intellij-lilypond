package nl.abbyberkers.lilypond.run.pdf

import com.intellij.openapi.fileEditor.ex.FileEditorProviderManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

object PdfViewerAvailability {
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
}
