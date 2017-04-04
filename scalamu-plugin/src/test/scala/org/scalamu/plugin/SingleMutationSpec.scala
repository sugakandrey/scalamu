package org.scalamu.plugin

import org.scalamu.plugin.fixtures.SharedScalamuCompilerFixture
import org.scalatest.{FlatSpec, Matchers}

trait SingleMutationSpec
    extends FlatSpec
    with Matchers
    with MutationOnlyRunner
    with SharedScalamuCompilerFixture {

  def mutation: Mutation
  override final def mutations: Seq[Mutation] = List(mutation)
}
