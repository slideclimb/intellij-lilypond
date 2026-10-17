package nl.abbyberkers.lilypond.run

import com.intellij.openapi.util.SystemInfo
import com.intellij.execution.configurations.PathEnvironmentVariableUtil
import java.io.File

object LilypondExecutable {
    val fileName: String get() = if (SystemInfo.isWindows) "lilypond.exe" else "lilypond"

    fun findOnPath(): File? = PathEnvironmentVariableUtil.findInPath(fileName)
}
