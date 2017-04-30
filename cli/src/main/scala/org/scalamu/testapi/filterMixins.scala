package org.scalamu.testapi

import cats.instances.function._
import cats.syntax.cartesian._
import org.scalamu.core.ClassInfo
import org.scalamu.plugin.NameFilter

trait TestClassFilterMixin extends TestClassFilter {
  protected def additionalReq: ClassInfo => Boolean

  override abstract lazy val predicate: (ClassInfo) => Boolean =
    (additionalReq |@| super.predicate).map { _ && _ }
}

trait HasNoArgConstructor extends TestClassFilterMixin {
  override protected val additionalReq: (ClassInfo) => Boolean = _.hasNoArgConstructor
}

trait NotAModule extends TestClassFilterMixin {
  override protected val additionalReq: (ClassInfo) => Boolean = !_.isModule
}

trait IsAModule extends TestClassFilterMixin {
  override protected val additionalReq: (ClassInfo) => Boolean = _.isModule
}

trait HasAppropriateName extends TestClassFilter {
  val nameFilter: NameFilter

  override def apply(info: ClassInfo): Option[TestClassInfo] =
    super.apply(info).filter(test => !nameFilter(test.info.name.fullName))
}
