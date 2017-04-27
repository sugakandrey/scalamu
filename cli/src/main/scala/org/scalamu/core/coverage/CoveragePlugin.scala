package org.scalamu.core.coverage

import scoverage.{ScoverageInstrumentationComponent, ScoveragePlugin}

import scala.tools.nsc.Global

class CoveragePlugin(override val global: Global, listener: InstrumentationReporter)
    extends ScoveragePlugin(global) {

  override val instrumentationComponent: ScoverageInstrumentationComponent =
    new InMemoryScoverageComponent(global)

  override def processOptions(opts: List[String], error: (String) => Unit): Unit = ()

  class InMemoryScoverageComponent(override val global: Global)
      extends ScoverageInstrumentationComponent(global, None, None) {
    
    override def newPhase(prev: scala.tools.nsc.Phase): Phase = new Phase(prev) {
      override def run(): Unit = {
        super.run()
        listener.onInstrumentationFinished(coverage)
      }
    }
  }
}
