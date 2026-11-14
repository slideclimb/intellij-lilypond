package nl.abbyberkers.lilypond.run

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class LilypondRunLineMarkerContributorTest : BasePlatformTestCase() {
    private val contributor = LilypondRunLineMarkerContributor()

    // The files are added rather than opened in an editor: getInfo works on PSI alone.

    /**
     * The platform offers the contributor every element in the file, so the test has to do the same to
     * see how many markers the gutter would end up with.
     */
    private fun markedElements(file: PsiFile): List<PsiElement> =
        (listOf(file) + PsiTreeUtil.collectElements(file) { true })
            .filter { contributor.getInfo(it) != null }

    fun testContributesASingleMarkerToTheFirstLeafOfALilypondFile() {
        val file = myFixture.addFileToProject("song.ly", "\\relative c' { c4 }\nc4\n")

        val marked = markedElements(file)

        assertEquals("Exactly one element may claim the arrow", 1, marked.size)
        assertSame(PsiTreeUtil.firstChild(file), marked.single())
    }

    fun testContributesAnActionableInfo() {
        val file = myFixture.addFileToProject("song.ly", "\\relative c' { c4 }")

        val info = contributor.getInfo(PsiTreeUtil.firstChild(file))!!

        assertNotNull(info.icon)
        assertTrue("Without executor actions the arrow opens an empty popup", info.actions.isNotEmpty())
    }

    /**
     * An `.ily` file is an include by convention, but compiling one on its own is useful while that
     * single file is being worked on.
     */
    fun testContributesASingleMarkerToIncludeFiles() {
        val file = myFixture.addFileToProject("library.ily", "\\version \"2.24.0\"\nfoo = { c4 }\n")

        val marked = markedElements(file)

        assertEquals("Exactly one element may claim the arrow", 1, marked.size)
        assertSame(PsiTreeUtil.firstChild(file), marked.single())
    }

    fun testRefusesOtherFileTypes() {
        assertEmpty(markedElements(myFixture.addFileToProject("readme.txt", "not a score")))
    }
}
