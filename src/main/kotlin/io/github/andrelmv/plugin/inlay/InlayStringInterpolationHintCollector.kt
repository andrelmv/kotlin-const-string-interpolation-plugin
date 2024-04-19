package io.github.andrelmv.plugin.inlay

import com.intellij.codeInsight.hints.FactoryInlayHintsCollector
import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.idea.caches.resolve.analyze
import org.jetbrains.kotlin.idea.intentions.loopToCallChain.isConstant
import org.jetbrains.kotlin.idea.structuralsearch.visitor.KotlinRecursiveElementVisitor
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtReferenceExpression
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.kotlin.psi.psiUtil.isPlain
import org.jetbrains.kotlin.psi.psiUtil.isSingleQuoted
import org.jetbrains.kotlin.resolve.constants.evaluate.ConstantExpressionEvaluator
import org.jetbrains.kotlin.resolve.lazy.BodyResolveMode
import org.jetbrains.kotlin.types.TypeUtils
import java.util.concurrent.atomic.AtomicInteger

@Suppress("UnstableApiUsage")
class InlayStringInterpolationHintCollector(
    private val settings: InlayStringInterpolationSettings,
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

                    if (settings.state.withStringInterpolationHint && element.isStringTemplateConstant()) {
                        (element as KtStringTemplateExpression).getValue()
                            ?.let {
                                offset.set(element.lastChild.textOffset)
                                val base = factory.text(it)
                                val inlayPresentation = factory.roundWithBackground(base)
                                sink.addInlineElement(offset.get(), true, inlayPresentation, true)
                            }
                        return
                    } else if (settings.state.withStringConstantHint && element.isStringConstant()) {
                        (element as KtNameReferenceExpression).getValue()
                            ?.let {
                                offset.set(element.getReferencedNameElement().text.length + element.textOffset)
                                val base = factory.text(it)
                                val inlayPresentation = factory.roundWithBackground(base)
                                sink.addInlineElement(offset.get(), true, inlayPresentation, false)
                            }
                    }
                }
            }
        )
        return true
    }
}

private fun PsiElement.isStringConstant(): Boolean {
    return this is KtReferenceExpression
            && ((this.context is KtValueArgument && this.isConstant())
                    || this.context is KtNamedFunction)
}

private fun PsiElement.isStringTemplateConstant(): Boolean {
    return this is KtStringTemplateExpression
            && this.isConstant() && this.isPlain().not()
            && this.hasInterpolation()
            && this.isSingleQuoted()
}

private fun KtExpression.getValue() =
    ConstantExpressionEvaluator.getConstant(this, analyze(BodyResolveMode.PARTIAL))
        ?.let { it.getValue(TypeUtils.DONT_CARE) as String }
