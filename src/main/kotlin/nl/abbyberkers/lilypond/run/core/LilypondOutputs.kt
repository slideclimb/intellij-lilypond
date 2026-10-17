package nl.abbyberkers.lilypond.run.core

object LilypondOutputs {
    /**
     * Picks the PDFs a run produced out of the names present in its output directory.
     *
     * The produced files are selected rather than predicted, because the number of them depends on
     * the score: one `\book` gives `song.pdf`, and further books add `song-1.pdf`, `song-2.pdf`, ….
     *
     * @return `song.pdf` first, then the numbered files in numeric order.
     */
    fun selectProducedPdfs(basename: String, namesInOutputDirectory: List<String>): List<String> {
        val pattern = Regex("^${Regex.escape(basename)}(?:-(\\d+))?\\.pdf$")
        return namesInOutputDirectory
            .mapNotNull { name ->
                pattern.matchEntire(name)?.let { name to (it.groupValues[1].toIntOrNull() ?: -1) }
            }
            .sortedBy { it.second }
            .map { it.first }
    }
}
