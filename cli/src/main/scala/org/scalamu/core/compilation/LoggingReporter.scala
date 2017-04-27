package org.scalamu.core.compilation

import com.typesafe.scalalogging.Logger

import scala.reflect.internal.util.Position
import scala.tools.nsc.Settings
import scala.tools.nsc.reporters.AbstractReporter

/**
 * Wraps given logger into [[scala.tools.nsc.reporters.Reporter]]
 */
class LoggingReporter(logger: Logger, override val settings: Settings) extends AbstractReporter {
  override def display(pos: Position, msg: String, severity: Severity): Unit = {
    val message = Position.formatMessage(pos, msg, shortenFile = true)
    val log: String => Unit = severity match {
      case ERROR   => logger.error(_)
      case INFO    => logger.info(_)
      case WARNING => logger.warn(_)
    }
    log(message)
  }

  override def displayPrompt(): Unit = ()
}
