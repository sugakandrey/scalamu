package org.scalamu.core.coverage

import org.scalamu.core.compilation.ScalamuMutationPhase
import scoverage.{ScoverageInstrumentationComponent, ScoveragePlugin}

import scala.tools.nsc.Global

class CoveragePlugin(override val global: Global, listener: InstrumentationReporter)
    extends ScoveragePlugin(global) {

  override val components: List[InMemoryScoverageComponent] = List(
    new InMemoryScoverageComponent(global)
  )

  override def processOptions(opts: List[String], error: (String) => Unit): Unit = ()

  class InMemoryScoverageComponent(override val global: Global)
      extends ScoverageInstrumentationComponent(global, None, Some(ScalamuMutationPhase.name)) {

    override def newPhase(prev: scala.tools.nsc.Phase): Phase = new Phase(prev) {
      override def run(): Unit = {
        super.run()
        listener.onInstrumentationFinished(coverage)
      }
    }
  }
}
