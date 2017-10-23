package org.scalamu.idea.runner
package ui

import java.util

import com.intellij.execution.util.ListTableWithButtons
import com.intellij.execution.util.ListTableWithButtons.ElementsColumnInfoBase
import com.intellij.util.ui.ListTableModel

class ScalamuFilterTable(emptyText: String) extends ListTableWithButtons[RegexFilter] {
  getTableView.getEmptyText.setText(emptyText)

  override def canDeleteElement(selection: RegexFilter): Boolean = true
  override def isEmpty(element: RegexFilter): Boolean            = element.filter.isEmpty
  override def cloneElement(variable: RegexFilter): RegexFilter  = RegexFilter(variable.filter)
  override def createElement(): RegexFilter                      = RegexFilter("")

  override def createListModel(): ListTableModel[RegexFilter] = {
    val filter = new ElementsColumnInfoBase[RegexFilter]("Filters") {
      override def isCellEditable(item: RegexFilter): Boolean   = true
      override def getDescription(element: RegexFilter): String = null
      override def valueOf(item: RegexFilter): String           = item.toUnescapedString

      override def setValue(item: RegexFilter, value: String): Unit =
        if (!value.equals(item.filter)) {
          item.filter = value
          setModified()
        }
    }

    new ListTableModel[RegexFilter](filter)
  }

  def toList: util.List[RegexFilter] = getElements
}
