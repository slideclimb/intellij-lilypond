package nl.abbyberkers.lilypond.run.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LilypondPathsTest {
    private val base = "/home/me/project"

    @Test
    fun `expands the project directory placeholder`() {
        assertEquals("/home/me/project/out", LilypondPaths.expand("{projectDir}/out", base))
    }

    @Test
    fun `expands a bare project directory placeholder`() {
        assertEquals(base, LilypondPaths.expand("{projectDir}", base))
    }

    @Test
    fun `normalises a trailing separator on the base`() {
        assertEquals("/home/me/project/out", LilypondPaths.expand("{projectDir}/out", "/home/me/project/"))
    }

    @Test
    fun `passes absolute paths through`() {
        assertEquals("/var/scores", LilypondPaths.expand("/var/scores", base))
    }

    @Test
    fun `resolves relative paths against the base`() {
        assertEquals("/home/me/project/build/pdf", LilypondPaths.expand("build/pdf", base))
    }

    @Test
    fun `falls back to the fallback base when there is no project base`() {
        assertEquals("/scores/out", LilypondPaths.expand("out", null, fallbackBase = "/scores"))
    }

    @Test
    fun `returns null for blank input`() {
        assertNull(LilypondPaths.expand(null, base))
        assertNull(LilypondPaths.expand("   ", base))
    }

    @Test
    fun `returns null when nothing can resolve a relative path`() {
        assertNull(LilypondPaths.expand("out", null))
        assertNull(LilypondPaths.expand("{projectDir}/out", null))
    }

    @Test
    fun `collapses paths under the project`() {
        assertEquals("{projectDir}/scores/a.ly", LilypondPaths.collapse("/home/me/project/scores/a.ly", base))
        assertEquals("{projectDir}", LilypondPaths.collapse(base, base))
    }

    @Test
    fun `leaves paths outside the project absolute`() {
        assertEquals("/elsewhere/a.ly", LilypondPaths.collapse("/elsewhere/a.ly", base))
    }

    @Test
    fun `collapse and expand round trip`() {
        val absolute = "/home/me/project/scores/a.ly"
        assertEquals(absolute, LilypondPaths.expand(LilypondPaths.collapse(absolute, base), base))
    }

    @Test
    fun `derives the output basename`() {
        assertEquals("song", LilypondPaths.basenameOf("/scores/song.ly"))
        assertEquals("song", LilypondPaths.basenameOf("/scores/song.ily"))
        assertEquals("Chorale.No.4", LilypondPaths.basenameOf("/scores/Chorale.No.4.ly"))
        assertEquals("song", LilypondPaths.basenameOf("/scores/song"))
    }

    @Test
    fun `derives the parent directory`() {
        assertEquals("/scores", LilypondPaths.parentOf("/scores/song.ly"))
        assertEquals("/", LilypondPaths.parentOf("/song.ly"))
    }
}
