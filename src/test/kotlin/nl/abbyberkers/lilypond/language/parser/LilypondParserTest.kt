package nl.abbyberkers.lilypond.language.parser

import com.intellij.openapi.util.io.FileUtil
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.impl.DebugUtil
import com.intellij.testFramework.FileBasedTestCaseHelperEx
import com.intellij.testFramework.LightPlatformCodeInsightTestCase
import com.intellij.testFramework.Parameterized
import com.jetbrains.rd.util.assert
import nl.abbyberkers.lilypond.language.LilypondLanguage
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File


@RunWith(Parameterized::class)
sealed class LilypondParserTest(val path: String) : LightPlatformCodeInsightTestCase(), FileBasedTestCaseHelperEx {
    override fun getTestDataPath(): String {
        return path
    }

    override fun getFileSuffix(filename: String): String? {
        return if (filename.endsWith(".ly")) filename else null
    }

    @Test
    fun testNoParseErrors() {
        val text = FileUtil.loadFile(File(myTestDataPath, myFileSuffix))
        val psiFile = PsiFileFactory.getInstance(project).createFileFromText(LilypondLanguage, text)
        val psiText = DebugUtil.psiToString(psiFile, true)
        assert(!psiText.contains("PsiError")) {
            psiText.lines().find { it.contains("PsiError") }!!
        }
    }

    override fun getRelativeBasePath(): String {
        return ""
    }
}

/**
 * Check that all snippets (from https://github.com/lilypond/lilypond/tree/master/Documentation/snippets) can be parsed without parse errors.
 */
class SnippetsTest : LilypondParserTest("src/test/resources/language/parser/snippets")

/**
 * Check that all snippets (from https://github.com/lilypond/lilypond/tree/master/input/regression) can be parsed without parse errors.
 */
class RegressionTest : LilypondParserTest("src/test/resources/language/parser/regression")