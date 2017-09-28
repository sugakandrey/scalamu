package org.scalamu.idea.configuration

import com.intellij.ide.browsers.{BrowserFamily, WebBrowser, WebBrowserManager}

import scala.util.matching.Regex

object ScalamuDefaultSettings {
  val parallelism: Int            = 1
  val timeoutConst: Int           = 2000
  val timeoutFactor: Double       = 1.5
  val verboseLogging: Boolean     = false
  val openInBrowser: Boolean      = false
  val scalacParameters: String    = ""
  val vmParameters: String        = ""
  val browser: Option[WebBrowser] = Option(WebBrowserManager.getInstance().getFirstBrowser(BrowserFamily.CHROME))
  val targetTests: Seq[Regex]     = Seq.empty
  val targetSources: Seq[Regex]   = Seq.empty
  
  val activeMutators: Seq[String] = Seq(
    "ReplaceCaseWithWildcard",
    "ReplaceMathOperators",
    "ReplaceWithIdentityFunction",
    "InvertNegations",
    "AlwaysExecuteConditionals",
    "NeverExecuteConditionals",
    "ReplaceConditionalBoundaries",
    "NegateConditionals",
    "RemoveUnitMethodCalls",
    "ChangeRangeBoundary",
    "ReplaceLogicalOperators",
    "ReplaceWithNone",
    "ReplaceWithNil"
  )
}
