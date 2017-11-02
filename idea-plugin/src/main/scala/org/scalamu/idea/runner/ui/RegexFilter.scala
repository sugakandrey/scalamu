package org.scalamu.idea.runner.ui

import scala.util.matching.Regex

case class RegexFilter(var filter: String) {
  override def toString: String = filter
  
  def toRegex: Regex =  {
    val builder = StringBuilder.newBuilder
    filter.foreach {
      case '.' => builder ++= "\\."
      case '*' => builder ++= ".*"
      case ch => builder += ch
    }
    builder.result().r
  }
}
