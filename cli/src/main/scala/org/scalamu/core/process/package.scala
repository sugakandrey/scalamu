package org.scalamu.core

package object process {
  def testTimeLimit(completionTimeMillis: Long, factor: Double, const: Long): Long =
    (completionTimeMillis * factor).toLong + const
}
