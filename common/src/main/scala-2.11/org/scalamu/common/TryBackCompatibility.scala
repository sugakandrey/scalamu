package org.scalamu.common

import scala.util.{Failure, Success, Try}

trait TryBackCompatibility {
  implicit class RichTry[T](val t: Try[T]) {
    def fold[R](fa: Throwable => R, fb: T => R): R = t match {
      case Failure(a) => fa(a)
      case Success(b) => fb(b)
    }
  }
}
