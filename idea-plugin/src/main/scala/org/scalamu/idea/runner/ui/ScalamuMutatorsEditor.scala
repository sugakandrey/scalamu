package org.scalamu.idea.runner
package ui

import javax.swing.JPanel

import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project

class ScalamuMutatorsEditor(val project: Project) extends SettingsEditor[ScalamuRunConfiguration] {
  private[this] val form = new ScalamuMutatorsForm(project)

  override def createEditor(): JPanel                            = form.mainPanel
  override def applyEditorTo(s: ScalamuRunConfiguration): Unit   = s(form)
  override def resetEditorFrom(s: ScalamuRunConfiguration): Unit = form(s)
}
