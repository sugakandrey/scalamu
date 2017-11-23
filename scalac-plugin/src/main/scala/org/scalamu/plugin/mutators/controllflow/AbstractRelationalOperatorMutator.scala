package org.scalamu.plugin.mutators.controllflow

import org.scalamu.plugin.ScalamuScalacConfig
import org.scalamu.plugin.mutators.{AbstractBinaryOperatorMutator, NumericTypesSupport}

import scala.tools.nsc.Global

trait AbstractRelationalOperatorMutator extends AbstractBinaryOperatorMutator {
  protected abstract class RelationalOperatorTransformer(config: ScalamuScalacConfig)(override val global: Global)
      extends BinaryOperatorTransformer(config)(global)
      with NumericTypesSupport
}
