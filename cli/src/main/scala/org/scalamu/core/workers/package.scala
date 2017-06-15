package org.scalamu.core

package object workers {
  def testTimeLimit(completionTimeMillis: Long, factor: Double, const: Long): Long =
    (completionTimeMillis * factor).toLong + const

  def die(code: ExitCode): Nothing = sys.exit(code.code)
}
