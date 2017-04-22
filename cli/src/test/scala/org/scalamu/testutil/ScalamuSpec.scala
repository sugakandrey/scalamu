package org.scalamu.testutil

import com.typesafe.scalalogging.StrictLogging
import org.scalamu.core.ClassInfo
import org.scalamu.utils.{ASMUtils, ClassLoadingUtils, FileSystemUtils}
import org.scalatest._

trait ScalamuSpec
    extends FlatSpec
    with Matchers
    with Inspectors
    with Inside
    with OptionValues
    with EitherValues
    with TryValues
    with FileSystemUtils
    with ASMUtils
    with ClassLoadingUtils
    with StrictLogging
    with TestSuiteResultMatchers {

  def classInfoForName(
    name: String
  )(implicit
    loader: ClassLoader = Thread.currentThread().getContextClassLoader,
    pos: org.scalactic.source.Position): ClassInfo =
    loadClassFileInfo(loader.getResourceAsStream(name)).success.value
}
