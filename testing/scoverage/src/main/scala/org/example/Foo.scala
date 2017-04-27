package org.example

case class Foo(a: Int, xs: List[Int] = List(-1, 0, 2), s: Option[String] = Some("Hello")) {
  println("Hello World!")

  def foo(): Int = -a

  def bar(x: Int, y: Int): Int =
    if (s.isDefined)
      x * a - 10 / y
    else
      -1

  def qux(): List[Int] = xs.collect { case x if x >= 2 => x * 10 }.drop(1).filter(_ > 100)
  
  def notCalled(): Unit = println("123")
}
