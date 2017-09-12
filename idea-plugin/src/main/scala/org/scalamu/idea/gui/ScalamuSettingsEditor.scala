package org.scalamu.idea.gui

import javax.swing.JComponent

import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import org.scalamu.idea.configuration.ScalamuRunConfiguration

class ScalamuSettingsEditor(val project: Project, val configuration: ScalamuRunConfiguration)
    extends SettingsEditor[ScalamuRunConfiguration] {
  
  private[this] val form = new ScalamuConfigurationForm(project, configuration)

  override def createEditor(): JComponent = form.getMainPanel
  override def applyEditorTo(s: ScalamuRunConfiguration): Unit = ()
  override def resetEditorFrom(s: ScalamuRunConfiguration): Unit = form(s)
}
