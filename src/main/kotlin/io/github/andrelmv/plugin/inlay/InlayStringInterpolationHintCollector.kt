package io.github.andrelmv.plugin.inlay

import com.intellij.codeInsight.hints.FactoryInlayHintsCollector
import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.idea.caches.resolve.analyze
import org.jetbrains.kotlin.idea.intentions.loopToCallChain.isConstant
import org.jetbrains.kotlin.idea.structuralsearch.visitor.KotlinRecursiveElementVisitor
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import org.jetbrains.kotlin.psi.psiUtil.isPlain
import org.jetbrains.kotlin.resolve.constants.evaluate.ConstantExpressionEvaluator
import org.jetbrains.kotlin.resolve.lazy.BodyResolveMode
import org.jetbrains.kotlin.types.TypeUtils
import java.util.concurrent.atomic.AtomicInteger

@Suppress("UnstableApiUsage")
class InlayStringInterpolationHintCollector(
    editor: Editor
) : FactoryInlayHintsCollector(editor) {

    override fun collect(
        element: PsiElement,
        editor: Editor,
        sink: InlayHintsSink
    ): Boolean {
        element.accept(
            object : KotlinRecursiveElementVisitor() {
                override fun visitElement(
                    element: PsiElement
                ) {
                    val offset = AtomicInteger()

                    if (element is KtStringTemplateExpression
                        && element.isConstant()
                        && element.isPlain().not()
                        && element.hasInterpolation()
                    ) {
                        element.getValue()
                            ?.let {
                                offset.set(element.lastChild.textOffset)
                                val base = factory.text(it)
                                val inlayPresentation = factory.roundWithBackground(base)
                                sink.addInlineElement(offset.get(), true, inlayPresentation, true)
                            }
                    }
                }
            }
        )
        return true
    }
}

private fun KtExpression.getValue() =
    ConstantExpressionEvaluator.getConstant(this, analyze(BodyResolveMode.PARTIAL))
        ?.let { it.getValue(TypeUtils.DONT_CARE) as String }
