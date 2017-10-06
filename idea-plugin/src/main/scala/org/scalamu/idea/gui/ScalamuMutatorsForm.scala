package org.scalamu.idea.gui

import java.awt.GridLayout
import javax.swing.JPanel

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.ui._
import com.intellij.ui.components.JBList
import org.scalamu.idea.configuration.{ScalamuDefaultSettings, ScalamuRunConfiguration}

import scala.collection.JavaConverters._

class ScalamuMutatorsForm(project: Project) {
  private[this] val listModel = new CollectionListModel[String]()
  private[this] val activeMutators = new JBList[String](listModel)
  
  listModel.add(ScalamuDefaultSettings.activeMutators.asJava)

  val mainPanel: JPanel = {
    val panel = new JPanel()
    val decorator = ToolbarDecorator.createDecorator(activeMutators)

    val mutatorsPanel = decorator
      .setAddAction(new AnActionButtonRunnable {
        override def run(t: AnActionButton): Unit = {
          val result = Messages.showInputDialog(project, "Enter mutator name:", "Add Mutator", null)
          if (result != null) {
            listModel.add(result)
          }
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

  def getActiveMutators: Seq[String] = listModel.getItems.asScala
}
