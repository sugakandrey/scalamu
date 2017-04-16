package org.scalamu.testutil

import org.scalatest._
import org.scalatest.concurrent.ScalaFutures

trait ScalamuSpec
    extends FlatSpec
    with Matchers
    with Inspectors
    with Inside
    with OptionValues
    with EitherValues
    with ScalaFutures
