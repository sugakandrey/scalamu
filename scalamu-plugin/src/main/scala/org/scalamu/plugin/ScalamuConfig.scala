package org.scalamu.plugin

import org.scalamu.plugin.mutations._

/**
  * Created by sugakandrey.
  */
object ScalamuConfig {
  val allMutations: Seq[Mutation] = Seq(
    NegateConditional,
    RemoveUnitMethodCall
  )
}
