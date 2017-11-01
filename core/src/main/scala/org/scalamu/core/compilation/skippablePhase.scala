package org.scalamu.core.compilation

/**
 * A scalac phase, introduced by a plugin.
 *
 * @param name internal name of the scalac phase
 */
sealed abstract class PluginPhase(val name: String)

case object ScoverageInstrumentationPhase extends PluginPhase("scoverage-instrumentation")
case object ScalamuMutationPhase          extends PluginPhase("mutating-transform")
