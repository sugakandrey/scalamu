package org.scalamu.plugin.testutil

import scala.reflect.internal.util.{BatchSourceFile, NoFile}
import scala.tools.nsc.Global

trait CompilationUtils { self: PluginRunner =>
  protected case class NamedSnippet(filename: String, code: String)

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

}
