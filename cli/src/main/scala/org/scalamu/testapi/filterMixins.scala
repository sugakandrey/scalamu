package org.scalamu.testapi

import cats.instances.function._
import cats.syntax.cartesian._

import org.scalamu.core.ClassFileInfo

trait TestClassFilterMixin extends TestClassFilter {
  protected def additionalReq: ClassFileInfo => Boolean

  override abstract val predicate: (ClassFileInfo) => Boolean =
    (additionalReq |@| super.predicate).map { _ && _ }
}

trait HasNoArgConstructor extends TestClassFilterMixin {
  override protected val additionalReq: ClassFileInfo => Boolean = _.hasNoArgConstructor
}

trait NotAModule extends TestClassFilterMixin {
  override protected val additionalReq: (ClassFileInfo) => Boolean = !_.isModule
}

trait IsAModule extends TestClassFilterMixin {
  override protected val additionalReq: (ClassFileInfo) => Boolean = _.isModule
}
