package org.baz.qux

object Fibs {
  def fibsRecursive(i: Int): Int = i match {
    case 0 | 1 => i
    case _     => fibsRecursive(i - 1) + fibsRecursive(i - 2)
  }

  def fibsIterative(i: Int): Int = {
    var fst  = 0
    var snd  = 1
    var next = fst + snd
    while (i > 0) {
      fst = snd
      snd = next
      next += fst
    }
    fst
  }
}
