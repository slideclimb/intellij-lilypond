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
class LilypondParserTest : LightPlatformCodeInsightTestCase(), FileBasedTestCaseHelperEx {
    override fun getTestDataPath(): String {
        return "src/test/resources/language/parser"
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