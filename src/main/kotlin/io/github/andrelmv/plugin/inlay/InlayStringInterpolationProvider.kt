package io.github.andrelmv.plugin.inlay

import com.intellij.codeInsight.hints.ChangeListener
import com.intellij.codeInsight.hints.ImmediateConfigurable
import com.intellij.codeInsight.hints.InlayHintsCollector
import com.intellij.codeInsight.hints.InlayHintsProvider
import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.NoSettings
import com.intellij.codeInsight.hints.SettingsKey
import com.intellij.lang.Language
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import com.intellij.ui.components.JBTextField
import org.jetbrains.kotlin.idea.KotlinLanguage
import javax.swing.JComponent

@Suppress("UnstableApiUsage")
class InlayStringInterpolationProvider : InlayHintsProvider<NoSettings> {

    private val settingsKey: SettingsKey<NoSettings> = SettingsKey(STR_INTERPOLATION_HINT)

    override val key: SettingsKey<NoSettings>
        get() = settingsKey

    override val name: String
        get() = STR_INTERPOLATION_HINT

    override val previewText: String
        get() = "const val ONE_STR = \"one\" \n" +
                "const val TWO_STR = \"\$ONE_STR two\""

    override fun createConfigurable(
        settings: NoSettings
    ): ImmediateConfigurable = object : ImmediateConfigurable {
        override fun createComponent(listener: ChangeListener): JComponent =
            JBTextField("Provides inlay hints for constant interpolated strings")
    }

    override fun isLanguageSupported(
        language: Language
    ): Boolean = language == KotlinLanguage.INSTANCE

    override fun createSettings(): NoSettings = NoSettings()
    override fun getCollectorFor(
        file: PsiFile,
        editor: Editor,
        settings: NoSettings,
        sink: InlayHintsSink
    ): InlayHintsCollector = InlayStringInterpolationHintCollector(editor)
}

private const val STR_INTERPOLATION_HINT = "String Interpolation Hint"