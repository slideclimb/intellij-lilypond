package nl.abbyberkers.lilypond.run.core

import com.intellij.openapi.util.io.FileUtil

/**
 * Path handling for LilyPond run configurations.
 *
 * Paths are stored with [PROJECT_DIR] standing in for the project root, so that a run configuration
 * committed to VCS resolves on every machine. Everything here is pure — the project root is passed
 * in — so it can be tested without an IDE fixture.
 */
object LilypondPaths {
    const val PROJECT_DIR = "{projectDir}"

    const val DEFAULT_OUTPUT_DIRECTORY = "$PROJECT_DIR/out"

    /**
     * Resolves a stored path to an absolute, system-independent one.
     *
     * [fallbackBase] stands in for [projectBasePath] when there is none: `Project.getBasePath()` is
     * null for the default project, which is exactly where run configuration templates are edited.
     *
     * @return null when [raw] is blank or no base is available to resolve it against.
     */
    fun expand(raw: String?, projectBasePath: String?, fallbackBase: String? = null): String? {
        val path = raw?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        val base = normalizeOrNull(projectBasePath) ?: normalizeOrNull(fallbackBase)

        return when {
            path == PROJECT_DIR -> base
            path.startsWith("$PROJECT_DIR/") -> base?.let { normalize("$it/${path.removePrefix("$PROJECT_DIR/")}") }
            FileUtil.isAbsolute(path) -> normalize(path)
            else -> base?.let { normalize("$it/$path") }
        }
    }

    /**
     * Inverse of [expand]: rewrites a path under the project root back to the [PROJECT_DIR] form.
     * Paths outside the project are returned unchanged, so they stay absolute.
     */
    fun collapse(absolutePath: String, projectBasePath: String?): String {
        val path = normalize(absolutePath)
        val base = normalizeOrNull(projectBasePath) ?: return path

        return when {
            path == base -> PROJECT_DIR
            path.startsWith("$base/") -> "$PROJECT_DIR/${path.removePrefix("$base/")}"
            else -> path
        }
    }

    /**
     * The `-o` basename for a source file: its name with the extension dropped, so `Chorale.No.4.ly`
     * keeps its dots and a name with no extension is left alone.
     *
     * Only the last extension goes, whichever it is, rather than a known LilyPond one: this is reached
     * with a file already established as LilyPond, so the extension is by definition one of that type's,
     * and matching a hardcoded list here would be a second place recording what plugin.xml registers.
     */
    fun basenameOf(mainFilePath: String): String =
        normalize(mainFilePath)
            .substringAfterLast('/')
            .let { it.substringBeforeLast('.', missingDelimiterValue = it) }

    fun parentOf(path: String): String {
        val normalized = normalize(path)
        val parent = normalized.substringBeforeLast('/', missingDelimiterValue = "")
        return parent.ifEmpty { if (normalized.startsWith("/")) "/" else normalized }
    }

    private fun normalizeOrNull(path: String?): String? = path?.trim()?.takeIf { it.isNotEmpty() }?.let(::normalize)

    private fun normalize(path: String): String = FileUtil.toCanonicalPath(FileUtil.toSystemIndependentName(path.trim()))
}
