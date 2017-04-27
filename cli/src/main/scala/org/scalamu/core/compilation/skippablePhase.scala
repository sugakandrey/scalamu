package org.scalamu.core.compilation

/**
 * A scalac phase, introduced by plugin, which can be skipped
 *
 * @param name internal name of the scalac phase
 */
sealed abstract class SkippablePhase(val name: String)

case object ScoverageInstrumentationPhase extends SkippablePhase("scoverage-instrumentation")
case object ScalamuMutationPhase          extends SkippablePhase("mutating-transform")
