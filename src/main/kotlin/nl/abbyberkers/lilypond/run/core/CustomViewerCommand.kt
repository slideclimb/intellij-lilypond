package nl.abbyberkers.lilypond.run.core

import com.intellij.util.execution.ParametersListUtil

object CustomViewerCommand {
    const val PLACEHOLDER = "{pdf}"

    /**
     * Turns a user-typed viewer command into an argument vector, substituting [PLACEHOLDER] with the
     * PDF path. The substitution happens inside tokens, so `zathura --synctex-forward=1:1:{pdf}`
     * works; a command without the placeholder gets the path appended.
     *
     * @return null when the command is blank.
     */
    fun buildArgVector(command: String, pdfPath: String): List<String> {
        val tokens = ParametersListUtil.parse(command)
        val substituted = tokens.map { it.replace(PLACEHOLDER, pdfPath) }
        return if (tokens.none { it.contains(PLACEHOLDER) }) substituted + pdfPath else substituted
    }
}
