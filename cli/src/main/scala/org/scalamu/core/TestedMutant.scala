package org.scalamu.core

import org.scalamu.plugin.MutantInfo

final case class TestedMutant(info: MutantInfo, status: DetectionStatus) {
  def reportMessage: String = s"${info.description.formatted("%-50s")} -> ${status.toString}"
}
