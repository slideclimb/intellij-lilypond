package nl.abbyberkers.lilypond.run.pdf

import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.io.File

/**
 * Guards the assumption [PdfViewerAvailability] rests on: the editor providers registered for a file are a
 * real signal rather than a blanket yes. No PDF viewer plugin is loaded in the fixture, so a PDF has to come
 * back unavailable — otherwise every run would try the built-in viewer and open nothing.
 *
 * The fixture cannot cover the opposite case, since it has no viewer plugin to install.
 */
class PdfViewerAvailabilityTest : BasePlatformTestCase() {
    fun testReportsNoViewerForAPdfWhenNothingProvidesOne() {
        assertFalse(isAvailableFor(".pdf", PDF_BYTES))
    }

    fun testReportsAViewerForAFileTheIdeItselfCanOpen() {
        // Proves the check is able to answer yes, so the negative above is a real absence and not a
        // lookup that never finds anything.
        assertTrue(isAvailableFor(".txt", "just text".toByteArray()))
    }

    /**
     * The file-less overload has to agree with the one taking a real PDF, since the dropdown label and the
     * fallback that happens after a compile are meant to be the same answer. Its stand-in file has no
     * content, so this is what would catch it being taken for an empty text file the IDE can happily open.
     */
    fun testReportsNoViewerWithoutAPdfToAskAbout() {
        assertFalse(PdfViewerAvailability.isBuiltInViewerAvailable(project))
    }

    /**
     * The file is written to disk rather than into the in-memory fixture, because the file type is decided
     * partly by content and a PDF has to look binary to be treated as one.
     */
    private fun isAvailableFor(suffix: String, content: ByteArray): Boolean {
        val file = File.createTempFile("song", suffix).apply {
            deleteOnExit()
            writeBytes(content)
        }
        val virtualFile = requireNotNull(LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file))
        // The EDT the fixture runs on always has read access, so no explicit read action is needed here.
        return PdfViewerAvailability.isBuiltInViewerAvailable(project, virtualFile)
    }

    private companion object {
        // A header and the binary marker comment that real PDFs carry so tools do not take them for text.
        val PDF_BYTES =
            "%PDF-1.7\n%".toByteArray() +
                byteArrayOf(0xE2.toByte(), 0xE3.toByte(), 0xCF.toByte(), 0xD3.toByte(), 0x00) +
                "\n".toByteArray()
    }
}
