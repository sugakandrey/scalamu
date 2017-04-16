package org.baz.qux

import org.specs2.mutable.Specification

class FibsSpecs2 extends Specification {
  override def is = s2"""
  This is a specification for the Fibs module
  
  It should 
    calculate 11th fibonacci number iteratively       $e1
    calculate 12th fibonacci number recursively       $e2
                                                      """
  def e1          = Fibs.fibsIterative(11) === 89
  def e2          = Fibs.fibsRecursive(12) === 144
}
