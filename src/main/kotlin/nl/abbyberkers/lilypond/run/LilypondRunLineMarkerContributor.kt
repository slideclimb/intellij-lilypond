package nl.abbyberkers.lilypond.run

import com.intellij.execution.lineMarker.ExecutorAction
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import nl.abbyberkers.lilypond.language.LilypondFileType

/**
 * Puts a single run arrow in the gutter of the first line of every compilable LilyPond file.
 *
 * [DumbAware] because the decision is a file-name test and needs no indexes: without it
 * `RunLineMarkerProvider` filters this contributor out while the IDE indexes, so the arrow would be
 * missing for the first minutes of every session.
 */
class LilypondRunLineMarkerContributor : RunLineMarkerContributor(), DumbAware {
    override fun getInfo(element: PsiElement): Info? {
        // The platform offers every element in the file. Markers must be created for leaves only —
        // a marker on a composite blinks as the file is scrolled — and exactly one leaf per file may
        // claim it, otherwise the arrow is repeated on every line.
        if (element.firstChild != null) return null
        val file = element.containingFile ?: return null
        if (PsiTreeUtil.firstChild(file) !== element) return null
        // `.ily` includes get an arrow too — compiling one on its own is useful while it is being
        // worked on — and they carry this file type, so the registration decides rather than a
        // list here. LilypondRunConfigurationProducer accepts the same set the same way.
        if (file.fileType != LilypondFileType) return null

        // The platform derives the tooltip from the actions themselves, so it reads "Run 'song.ly'"
        // without a bundle key of its own.
        return Info(AllIcons.RunConfigurations.TestState.Run, ExecutorAction.getActions(0))
    }
}
