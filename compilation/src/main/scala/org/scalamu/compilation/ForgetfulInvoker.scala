package org.scalamu.compilation

import java.util.concurrent.ConcurrentHashMap
import scala.collection.JavaConverters._
import scala.collection.Set

object ForgetfulInvoker {
  private[this] val ids = ConcurrentHashMap.newKeySet[Int](1000)

  def invoked(id: Int): Unit      = ids.add(id)
  def invokedStatements: Set[Int] = ids.asScala
  def forget(): Unit              = ids.clear()
}
