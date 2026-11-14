package nl.abbyberkers.lilypond.run

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.abbyberkers.lilypond.LilypondBundle

/**
 * The placeholder of the blank executable field names the binary auto-detection picked up, so the
 * messages must actually interpolate their argument rather than drop it.
 */
class LilypondExecutablePlaceholderTest : BasePlatformTestCase() {
    fun testFoundMessageContainsThePath() {
        val path = "/opt/lilypond/bin/lilypond"
        val message = LilypondBundle.message("run.settings.executable.empty.text.found", path)

        assertEquals("Auto-detected on PATH: $path", message)
    }

    fun testNotFoundMessageContainsTheFileName() {
        val message =
            LilypondBundle.message("run.settings.executable.empty.text.not.found", LilypondExecutable.fileName)

        assertEquals("${LilypondExecutable.fileName} not found on PATH", message)
    }
}
