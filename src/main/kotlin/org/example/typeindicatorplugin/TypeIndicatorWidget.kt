package org.example.typeindicatorplugin

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidget.TextPresentation
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.jetbrains.python.psi.PyAssignmentStatement
import com.jetbrains.python.psi.PyReferenceExpression
import com.jetbrains.python.psi.PyTargetExpression
import com.jetbrains.python.psi.types.TypeEvalContext
import java.awt.Component

class TypeIndicatorWidget(private val project: Project) : StatusBarWidget, TextPresentation {

    private var typeText: String = "Unknown"  // Default text
    private var statusBar: StatusBar? = null

    init {
        setupCaretListener()
    }

    override fun ID(): String = "TypeIndicatorWidget"

    override fun getAlignment(): Float = Component.LEFT_ALIGNMENT

    override fun getText(): String = typeText

    override fun getTooltipText(): String = "Displays the type of the variable under the caret"

    override fun install(statusBar: StatusBar) {
        this.statusBar = statusBar
        // Initialize for the first open editor
        val editor = EditorFactory.getInstance().allEditors.firstOrNull { it.project == project }
        editor?.let { updateTypeInfo(it) }
    }

    override fun dispose() {
        statusBar = null
    }

    override fun getPresentation(): StatusBarWidget.WidgetPresentation = this

    private fun setupCaretListener() {
        EditorFactory.getInstance().eventMulticaster.addCaretListener(object : CaretListener {
            override fun caretPositionChanged(event: CaretEvent) {
                if (event.editor.project == project) {
                    updateTypeInfo(event.editor)
                }
            }
        }, this)
    }

    private fun updateTypeInfo(editor: Editor) {
        val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.document) ?: return
        val offset = editor.caretModel.offset
        val element = psiFile.findElementAt(offset) ?: return

        val type = resolveVariableType(element)
        println(type)
        setTypeText(type ?: "Unknown")
    }

    private fun resolveVariableType(element: PsiElement): String? {
        val context = TypeEvalContext.userInitiated(element.project, element.containingFile)

        val parent = element.parent
        // The type of element is:
        println(element)

        // Try to resolve if it's a reference expression "print(var)", var is the reference expression
        if (parent is PyReferenceExpression) {
            println("Resolving reference")
            println(parent)
            val resolved = (parent as PyReferenceExpression).reference.resolve()
            if (resolved is PyTargetExpression) {
                return context.getType(resolved)?.name
            }
        }

        // Resolving assignments (var = "hello world")
        val assignment = parent.parent
        println("Resolving assignment")
        println(assignment)
        if (assignment is PyAssignmentStatement) {
            val assignedValue = assignment.assignedValue
            if (assignedValue != null) {
                return context.getType(assignedValue)?.name
            }
        }

        // Otherwise, we can't resolve the type (Not implemented it yet)
        return null
    }

    private fun setTypeText(text: String) {
        if (text != typeText) {
            typeText = text
            statusBar?.updateWidget(ID())
        }
    }
}
