package org.scalamu.core

import java.time.{Instant, ZoneId}
import java.time.format.DateTimeFormatter

import scribe.formatter.FormatterBuilder
import scribe.{Level, LogHandler, Logger}

object LoggerConfiguration {
  def configureLoggingForName(name: String, verbose: Boolean = false): Unit = {
    val level = if (verbose) Level.Debug else Level.Info

    val formatter = FormatterBuilder()
      .add(
        record =>
          DateTimeFormatter
            .ofPattern("HH:mm:ss.SSS")
            .format(Instant.ofEpochMilli(record.timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime)
      )
      .string(s" [$name - ").threadName.string("] ")
      .levelPaddedRight.string(" ")
      .positionAbbreviated
      .string(" - ").message.newLine

    val handler = LogHandler(level, formatter)

    Logger.root.clearHandlers()
    Logger.root.addHandler(handler)
  }
}
