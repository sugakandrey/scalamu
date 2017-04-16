package org.scalamu.plugin.testutil

import org.scalamu.plugin.{MutantInfo, MutationReporter}

import scala.reflect.internal.util.{BatchSourceFile, NoFile, SourceFile}
import scala.tools.nsc.Global

trait CompilationUtils {
  protected case class NamedSnippet(filename: String, code: String)

  protected def compileFiles(files: Seq[SourceFile])(implicit global: Global): Int = {
    val run = new global.Run
    val id  = global.currentRunId
    run.compileSources(files.toList)
    id
  }

  protected def compile(
    code: NamedSnippet,
    rest: NamedSnippet*
  )(implicit global: Global): Int =
    compileFiles(
      (code +: rest).map {
        case NamedSnippet(name, contents) => new BatchSourceFile(name, contents)
      }
    )

  protected def compile(
    code: String
  )(implicit global: Global): Int =
    compileFiles(List(new BatchSourceFile(NoFile, code)))

  def mutantsFor(
    code: String
  )(
    implicit global: Global,
    reporter: MutationReporter
  ): Set[MutantInfo] =
    reporter.mutantsForRunId(compile(code))

  def mutantsFor(
    namedSnippet: NamedSnippet
  )(
    implicit global: Global,
    reporter: MutationReporter
  ): Set[MutantInfo] =
    reporter.mutantsForRunId(compile(namedSnippet))
}
