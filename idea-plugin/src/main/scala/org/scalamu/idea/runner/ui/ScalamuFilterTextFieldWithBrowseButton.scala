package org.scalamu.idea.runner.ui

import java.awt.BorderLayout
import java.util
import javax.swing.{JComponent, JPanel}

import com.intellij.openapi.ui.{DialogWrapper, TextFieldWithBrowseButton}

import scala.collection.JavaConverters._
import scala.collection.{mutable => m}

class ScalamuFilterTextFieldWithBrowseButton(val dialogTitle: String, val emptyText: String)
    extends TextFieldWithBrowseButton() { self =>
  
  private[ui] val filters = m.ArrayBuffer.empty[RegexFilter]

  setEditable(false)
  addActionListener(_ => new ScalamuFilterDialog().show())

  def filtersAsString: String = filters.map(_.filter).mkString(", ")

  def setData(data: util.List[RegexFilter]): Unit = {
    filters.clear()
    data.forEach(t => filters += t)
    setText(data.asScala.map(_.toString).mkString(", "))
  }

  private class ScalamuFilterDialog extends DialogWrapper(self, true) {
    private val mainPanel = new JPanel(new BorderLayout)

    private[this] val table = {
      val t = new ScalamuFilterTable(emptyText)
      t.setValues(filters.asJava)
      t
    }

    mainPanel.add(table.getComponent, BorderLayout.CENTER)
    setTitle(dialogTitle)
    init()

    override def createCenterPanel(): JComponent = mainPanel

    override def doOKAction(): Unit = {
      table.stopEditing()
      setData(table.toList)
      super.doOKAction()
    }
  }
}
