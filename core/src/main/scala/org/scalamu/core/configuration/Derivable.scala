package org.scalamu.core.configuration

/**
 * Represents capability of an entity to be derived from [[org.scalamu.core.configuration.ScalamuConfig]]
 *
 * @tparam T type of an entity
 */
trait Derivable[T] {
  def fromConfig(config: ScalamuConfig): T
}

object Derivable {
  def apply[T: Derivable]: Derivable[T] = implicitly[Derivable[T]]
}
