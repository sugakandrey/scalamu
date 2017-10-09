package org.scalamu.core

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.{PatternLayout, Logger => LogbackLogger}
import ch.qos.logback.core.ConsoleAppender
import org.slf4j.{LoggerFactory, Logger => SL4JLogger}

object LoggerConfiguration {
  def configureLoggingForName(name: String): Unit = {
    val logger = LoggerFactory.getLogger(SL4JLogger.ROOT_LOGGER_NAME).asInstanceOf[LogbackLogger]
    logger.detachAndStopAllAppenders()

    val pl  = new PatternLayout
    val ctx = logger.getLoggerContext
    pl.setContext(ctx)
    pl.setPattern(s"%d{HH:mm:ss.SSS} [$name - %thread{5}] %-5level %logger{10} - %msg%n")
    pl.start()

    val stdoutAppender = new ConsoleAppender[ILoggingEvent]
    stdoutAppender.setContext(ctx)
    stdoutAppender.setLayout(pl)
    stdoutAppender.start()
    logger.addAppender(stdoutAppender)
  }
}
