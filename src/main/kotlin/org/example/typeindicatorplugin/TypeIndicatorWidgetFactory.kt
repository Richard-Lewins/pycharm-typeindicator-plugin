package org.example.typeindicatorplugin

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory

class TypeIndicatorWidgetFactory : StatusBarWidgetFactory {
    override fun getId(): String = "VariableTypeStatusBarWidgetFactory"
    override fun getDisplayName(): String = "Variable Type Status Bar Widget"
    override fun isAvailable(project: Project): Boolean = true

    override fun createWidget(project: Project): StatusBarWidget = TypeIndicatorWidget(project)

    override fun disposeWidget(widget: StatusBarWidget) {
        widget.dispose()
    }

    override fun canBeEnabledOn(statusBar: StatusBar): Boolean = true
}
