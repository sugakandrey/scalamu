package org.scalamu.testutil

import com.typesafe.scalalogging.StrictLogging
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
