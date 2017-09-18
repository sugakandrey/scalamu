package org.scalamu.idea.configuration

import com.intellij.ide.browsers.{BrowserFamily, WebBrowser, WebBrowserManager}

import scala.util.matching.Regex

object ScalamuDefaultSettings {
  val timeoutConst: Int           = 2000
  val timeoutFactor: Double       = 1.5
  val targetTests: Seq[Regex]     = Seq.empty
  val targetSources: Seq[Regex]   = Seq.empty
  val parallelism: Int            = 1
  val verboseLogging: Boolean     = false
  val openInBrowser: Boolean      = false
  val browser: Option[WebBrowser] = Option(WebBrowserManager.getInstance().getFirstBrowser(BrowserFamily.CHROME))
}
