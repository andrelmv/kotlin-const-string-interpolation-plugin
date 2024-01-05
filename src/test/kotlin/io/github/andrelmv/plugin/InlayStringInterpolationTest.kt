package io.github.andrelmv.plugin

import com.intellij.codeInsight.hints.NoSettings
import com.intellij.testFramework.utils.inlays.InlayHintsProviderTestCase
import io.github.andrelmv.plugin.inlay.InlayStringInterpolationProvider

@Suppress("UnstableApiUsage")
class InlayStringInterpolationTest : InlayHintsProviderTestCase() {

    fun `test given a const interpolated string when evaluating hints then return inlay hint`() {
        val text = "const val ONE_STR = \"one\" \n" +
                "const val TWO_STR = \"\$ONE_STR two/*<# one two #>*/\""

        testInlayHint(text)
    }

    fun `test given a const not interpolated string when evaluating hints then return no inlay hint`() {
        val text = "const val ONE_STR = \"one\""

        testInlayHint(text)
    }

    private fun testInlayHint(
        text: String
    ) {
        doTestProvider(
            fileName = "Test.kt",
            expectedText = text,
            provider = InlayStringInterpolationProvider(),
            settings = NoSettings(),
            verifyHintPresence = false
        )
    }
}
