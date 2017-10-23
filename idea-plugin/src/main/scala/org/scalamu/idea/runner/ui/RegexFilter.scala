package org.scalamu.idea.runner.ui

import org.apache.commons.lang3.StringEscapeUtils

class RegexFilter(var filter: String) {
  override def toString: String = filter
  
  def toUnescapedString: String = StringEscapeUtils.unescapeJava(toString)
}

object RegexFilter {
  def apply(filter: String): RegexFilter = {
    val builder = StringBuilder.newBuilder
    filter.foreach { c =>
      if (c == '.') builder ++= "\\"
      builder += c
    }
    new RegexFilter(builder.mkString)
  }
}
