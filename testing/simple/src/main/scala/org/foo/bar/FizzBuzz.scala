package org.foo.bar

case class FizzBuzz(i: Int) {
  def fizzBuzz(): Seq[String] =
    (1 to i).map(
      x =>
        (x % 3, x % 5) match {
          case (0, 0) => "FizzBuzz"
          case (0, _) => "Fizz"
          case (_, 0) => "Buzz"
          case _      => x.toString
      }
    )
}
