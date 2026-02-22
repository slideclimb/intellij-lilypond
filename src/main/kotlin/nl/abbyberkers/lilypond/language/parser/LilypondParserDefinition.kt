package nl.abbyberkers.lilypond.language.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import nl.abbyberkers.lilypond.language.LilypondLanguage
import nl.abbyberkers.lilypond.language.psi.LilypondTypes

class LilypondParserDefinition : ParserDefinition {
    override fun createLexer(project: Project) = LilypondLexerAdapter()

    override fun createParser(project: Project): PsiParser = LilypondParser()

    override fun getFileNodeType(): IFileElementType = FILE

    override fun getCommentTokens(): TokenSet = LilypondTokenSets.COMMENTS

    override fun getStringLiteralElements(): TokenSet = LilypondTokenSets.STRING_LITERALS

    override fun createElement(node: ASTNode): PsiElement = LilypondTypes.Factory.createElement(node)

    override fun createFile(viewProvider: FileViewProvider): PsiFile = LilypondFile(viewProvider)

}

val FILE = IFileElementType(LilypondLanguage)