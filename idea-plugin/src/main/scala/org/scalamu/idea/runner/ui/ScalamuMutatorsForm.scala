package org.scalamu.idea.runner
package ui

import java.awt.GridLayout
import javax.swing.JPanel

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.ui._
import com.intellij.ui.components.JBList
import org.scalamu.idea.ScalamuBundle

import scala.collection.JavaConverters._

class ScalamuMutatorsForm(project: Project) {
  private[this] val listModel      = new CollectionListModel[String]()
  private[this] val activeMutators = new JBList[String](listModel)

  ScalamuDefaultSettings.activeMutators.asJava.forEach(listModel.add(_))

  val mainPanel: JPanel = {
    val panel     = new JPanel()
    val decorator = ToolbarDecorator.createDecorator(activeMutators)

    val mutatorsPanel = decorator
      .setAddAction(new AnActionButtonRunnable {
        override def run(t: AnActionButton): Unit = {
          val result = Messages.showInputDialog(
            project,
            ScalamuBundle.getMessage("run.configuration.dialog.mutator.add"),
            ScalamuBundle.getMessage("run.configuration.dialog.mutator.title"),
            null
          )

          if (result != null && ScalamuDefaultSettings.activeMutators.contains(result)) {
            listModel.add(result)
          } else
            Messages.showErrorDialog(
              project,
              ScalamuBundle.getMessage("run.configuration.dialog.invalid.mutator"),
              ScalamuBundle.getMessage("run.configuration.dialog.invalid.title")
            )
        }
      })
      .setRemoveAction(new AnActionButtonRunnable {
        override def run(t: AnActionButton): Unit = ListUtil.removeSelectedItems(activeMutators)
      })
      .createPanel()

    panel.setLayout(new GridLayout(0, 1))
    panel.add(mutatorsPanel)
    panel
  }

  def apply(configurationForm: ScalamuRunConfiguration): Unit =
    listModel.replaceAll(configurationForm.activeMutators.asJava)

  def getActiveMutators: Seq[String] = listModel.toList.asScala
}
