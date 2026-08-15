package nl.abbyberkers.lilypond.run.core

import com.intellij.util.execution.ParametersListUtil

data class LilypondInvocation(
    val executablePath: String,
    val parameters: List<String>,
    val workingDirectory: String,
)

object LilypondCommandLineBuilder {
    fun build(request: LilypondCompileRequest): LilypondInvocation {
        val parameters = buildList {
            add("--pdf")
            // --output takes a basename; LilyPond appends the extension itself, so passing a path
            // ending in .pdf would produce song.pdf.pdf. Naming the basename explicitly (rather than
            // passing the directory, which LilyPond only honours if it already exists) keeps the
            // produced name a pure function of the input path.
            add("--output=${request.outputDirectory}/${request.outputBasename}")
            // User arguments last, so a later -o of theirs wins over ours.
            addAll(ParametersListUtil.parse(request.extraArguments))
            // Absolute, so LilyPond's diagnostics are absolute too and the console filter can resolve them.
            add(request.mainFilePath)
        }
        return LilypondInvocation(request.executablePath, parameters, request.workingDirectory)
    }
}
