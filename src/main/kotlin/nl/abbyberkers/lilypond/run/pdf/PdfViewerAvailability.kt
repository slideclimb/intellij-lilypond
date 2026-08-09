package nl.abbyberkers.lilypond.run.pdf

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.fileEditor.ex.FileEditorProviderManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

object PdfViewerAvailability {
    const val PLUGIN_ID = "com.firsttimeinforever.intellij.pdf.viewer.intellij-pdf-viewer"

    /**
     * Whether the IDE can render a PDF itself. The plugin is never a compile-time dependency: it
     * registers a `fileEditorProvider` for PDFs, so opening the file is enough. The provider check is
     * the generous fallback, so a fork or a rebranded viewer counts too.
     *
     * Needs a read lock; call it off the EDT before switching to it.
     */
    fun isBuiltInViewerAvailable(project: Project, pdf: VirtualFile): Boolean {
        val pluginId = PluginId.getId(PLUGIN_ID)
        if (PluginManagerCore.getPlugin(pluginId) != null && !PluginManagerCore.isDisabled(pluginId)) return true

        return FileEditorProviderManager.getInstance().getProviderList(project, pdf).isNotEmpty()
    }
}
