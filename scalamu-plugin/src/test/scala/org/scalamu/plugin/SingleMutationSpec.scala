package org.scalamu.plugin

import org.scalamu.plugin.fixtures.SharedScalamuCompilerFixture
import org.scalatest.{FlatSpec, Matchers}

trait SingleMutationSpec
    extends FlatSpec
    with Matchers
    with MutationOnlyRunner
    with SharedScalamuCompilerFixture
