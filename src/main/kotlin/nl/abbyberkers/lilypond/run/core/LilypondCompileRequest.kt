package nl.abbyberkers.lilypond.run.core

/**
 * A fully resolved compile request: every path is absolute and every placeholder already expanded.
 */
data class LilypondCompileRequest(
    val executablePath: String,
    val mainFilePath: String,
    val outputDirectory: String,
    val extraArguments: String = "",
) {
    /**
     * `song.ly` becomes `song`: the `-o` basename, and the prefix of every PDF the run produces.
     */
    val outputBasename: String get() = LilypondPaths.basenameOf(mainFilePath)

    val workingDirectory: String get() = LilypondPaths.parentOf(mainFilePath)
}
