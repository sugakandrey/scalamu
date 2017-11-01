package org.scalamu.report

import org.scalamu.core.api._

object HtmlUtils {
  def mutantStatus(mutant: TestedMutant): String = mutant.status match {
    case _: Killed       => "killed"
    case NoTestCoverage  => "no_coverage"
    case Alive           => "alive"
    case Untested        => "untested"
    case TimedOut        => "timed_out"
    case OutOfMemory     => "oom"
    case InternalFailure => "failure"
  }

  def lineStatus(line: Line): LineStatus =
    if (line.mutants.isEmpty) LineStatus.NotApplicable
    else if (line.mutants.forall(_.status.killed)) LineStatus.Covered
    else LineStatus.NotCovered

  def mutantsNumber(line: Line): String =
    if (line.mutants.isEmpty) ""
    else line.mutants.size.toString
}
