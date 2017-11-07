package org.scalamu.core.api

import org.scalamu.plugin.MutationInfo

final case class TestedMutant(info: MutationInfo, status: DetectionStatus) {
  def reportMessage: String = s"${info.description.formatted("%-50s")} -> ${status.toString}"
  
  def render: String =
    s"""
       |Original: <pre><code>${info.oldTree}</code></pre>
       |<br>
       |Mutated: <pre><code>${info.mutated}</code></pre>
     """.stripMargin
}
