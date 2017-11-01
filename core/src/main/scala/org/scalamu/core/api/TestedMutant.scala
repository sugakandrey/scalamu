package org.scalamu.core.api

import org.scalamu.plugin.MutantInfo

final case class TestedMutant(info: MutantInfo, status: DetectionStatus) {
  def reportMessage: String = s"${info.description.formatted("%-50s")} -> ${status.toString}"
  
  def render: String =
    s"""
       |Original: <pre><code>${info.oldTree}</code></pre>
       |<br>
       |Mutated: <pre><code>${info.mutated}</code></pre>
     """.stripMargin
}
