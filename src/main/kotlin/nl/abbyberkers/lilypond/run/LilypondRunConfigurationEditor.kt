package nl.abbyberkers.lilypond.run

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.RawCommandLineEditor
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.TitledSeparator
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.TextComponentEmptyText
import com.intellij.util.ui.UIUtil
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.components.BorderLayoutPanel
import nl.abbyberkers.lilypond.LilypondBundle
import nl.abbyberkers.lilypond.language.LilypondFileType
import nl.abbyberkers.lilypond.run.core.CustomViewerCommand
import nl.abbyberkers.lilypond.run.core.LilypondPaths
import nl.abbyberkers.lilypond.run.pdf.PdfViewerMode
import javax.swing.JComponent

class LilypondRunConfigurationEditor(private val project: Project) : SettingsEditor<LilypondRunConfiguration>() {
    // Fields rather than locals in createEditor(): reset/apply can run before the panel is shown.
    private val mainFileField = TextFieldWithBrowseButton().apply {
        addBrowseFolderListener(
            project,
            FileChooserDescriptorFactory.createSingleFileDescriptor(LilypondFileType)
                .withTitle(LilypondBundle.message("run.settings.main.file.chooser.title")),
        )
    }

    private val executableField = TextFieldWithBrowseButton().apply {
        addBrowseFolderListener(
            project,
            FileChooserDescriptorFactory.singleFileOrAppBundle().withTitle(LilypondBundle.message("run.settings.executable.chooser.title")),
        )
        // Without this the placeholder hides on focus, which is when the user most wants to read it.
        TextComponentEmptyText.setupPlaceholderVisibility(textField)
    }

    private val outputDirectoryField = TextFieldWithBrowseButton().apply {
        addBrowseFolderListener(
            project,
            FileChooserDescriptorFactory.singleDir().withTitle(LilypondBundle.message("run.settings.output.directory.chooser.title")),
        )
    }

    private val extraArgumentsField = RawCommandLineEditor()

    private val pdfViewerCombo = ComboBox(PdfViewerMode.entries.toTypedArray()).apply {
        renderer = SimpleListCellRenderer.create("") { it.displayName }
        addActionListener { syncCustomCommandEnabled() }
    }

    private val customViewerCommandField = RawCommandLineEditor()

    override fun createEditor(): JComponent =
        FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel(LilypondBundle.message("run.settings.main.file")), mainFileField, true)
            .addLabeledComponent(JBLabel(LilypondBundle.message("run.settings.executable")), executableField, true)
            .addLabeledComponent(
                JBLabel(LilypondBundle.message("run.settings.output.directory")),
                outputDirectoryField,
                true,
            )
            .addComponentToRightColumn(
                comment(LilypondBundle.message("run.settings.output.directory.comment", LilypondPaths.PROJECT_DIR)),
            )
            .addLabeledComponent(
                JBLabel(LilypondBundle.message("run.settings.extra.arguments")),
                extraArgumentsField,
                true,
            )
            .addComponent(TitledSeparator(LilypondBundle.message("run.settings.after.compilation")))
            .addLabeledComponent(JBLabel(LilypondBundle.message("run.settings.pdf.viewer")), pdfViewerCombo, true)
            .addLabeledComponent(
                JBLabel(LilypondBundle.message("run.settings.custom.viewer.command")),
                customViewerCommandField,
                true,
            )
            .addComponentToRightColumn(
                comment(
                    LilypondBundle.message(
                        "run.settings.custom.viewer.command.comment",
                        CustomViewerCommand.PLACEHOLDER,
                    ),
                ),
            )
            .addComponentFillVertically(BorderLayoutPanel(), 0)
            .panel

    override fun resetEditorFrom(configuration: LilypondRunConfiguration) {
        val options = configuration.options
        mainFileField.text = options.mainFilePath.orEmpty()
        executableField.text = options.executablePath.orEmpty()
        outputDirectoryField.text = options.outputDirectory.orEmpty()
        extraArgumentsField.text = options.extraArguments.orEmpty()
        pdfViewerCombo.selectedItem = options.pdfViewer
        customViewerCommandField.text = options.customViewerCommand.orEmpty()
        // setSelectedItem does not fire the action listener in every look and feel.
        syncCustomCommandEnabled()
        refreshDetectedExecutable()
    }

    override fun applyEditorTo(configuration: LilypondRunConfiguration) {
        val options = configuration.options
        options.mainFilePath = mainFileField.text.trimToNull()
        options.executablePath = executableField.text.trimToNull()
        options.outputDirectory = outputDirectoryField.text.trimToNull()
        options.extraArguments = extraArgumentsField.text.trimToNull()
        options.pdfViewer = pdfViewerCombo.selectedItem as? PdfViewerMode ?: PdfViewerMode.BUILT_IN
        options.customViewerCommand = customViewerCommandField.text.trimToNull()
    }

    private fun syncCustomCommandEnabled() {
        customViewerCommandField.isEnabled = pdfViewerCombo.selectedItem == PdfViewerMode.CUSTOM_COMMAND
    }

    /**
     * Shows which `lilypond` the blank field would actually run, so auto-detection is not a black box.
     *
     * Resolution is a handful of stat calls over PATH and carries no slow-operation assertion, so the EDT
     * is fine; [LilypondRunConfiguration.checkConfiguration] already resolves on every keystroke anyway.
     * Recomputed on reset rather than cached, because PATH changes between dialog openings.
     */
    private fun refreshDetectedExecutable() {
        val emptyText = (executableField.textField as? JBTextField)?.emptyText ?: return
        val detected = LilypondExecutable.findOnPath()
        if (detected == null) {
            emptyText.setText(
                LilypondBundle.message("run.settings.executable.empty.text.not.found", LilypondExecutable.fileName),
                SimpleTextAttributes.ERROR_ATTRIBUTES,
            )
        } else {
            emptyText.text =
                LilypondBundle.message("run.settings.executable.empty.text.found", detected.absolutePath)
        }
    }

    private fun comment(text: String) = JBLabel(text, UIUtil.ComponentStyle.SMALL, UIUtil.FontColor.BRIGHTER)

    private fun String?.trimToNull() = this?.trim()?.takeIf { it.isNotEmpty() }
}
