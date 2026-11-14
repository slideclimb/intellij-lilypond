package nl.abbyberkers.lilypond.notification

import com.intellij.ide.BrowserUtil
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import nl.abbyberkers.lilypond.LilypondBundle
import java.util.concurrent.atomic.AtomicBoolean

object LilypondNotifications {
    private const val GROUP_ID = "LilyPond"

    private const val PDF_VIEWER_PLUGIN_URL =
        "https://plugins.jetbrains.com/plugin/14494-pdf-viewer"

    // Once per IDE session. Suppressing it permanently would leave someone who installs the PDF
    // Viewer plugin later, then disables it again, without an explanation.
    private val builtInViewerWarningShown = AtomicBoolean(false)

    fun builtInPdfViewerUnavailable(project: Project) {
        if (!builtInViewerWarningShown.compareAndSet(false, true)) return

        NotificationGroupManager.getInstance()
            .getNotificationGroup(GROUP_ID)
            .createNotification(
                LilypondBundle.message("notification.pdf.viewer.missing.title"),
                LilypondBundle.message("notification.pdf.viewer.missing.content"),
                NotificationType.INFORMATION,
            )
            .addAction(
                NotificationAction.createSimpleExpiring(
                    LilypondBundle.message("notification.pdf.viewer.missing.action"),
                ) { BrowserUtil.browse(PDF_VIEWER_PLUGIN_URL) },
            )
            .notify(project)
    }
}
