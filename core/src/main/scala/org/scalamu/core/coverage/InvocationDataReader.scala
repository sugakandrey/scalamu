package org.scalamu.core.coverage

import scala.collection.Set

trait InvocationDataReader {
  def invokedStatements(): Set[Int]
  def clearData(): Unit
}
