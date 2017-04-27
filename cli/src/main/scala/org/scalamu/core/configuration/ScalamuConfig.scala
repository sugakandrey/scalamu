package org.scalamu.core.configuration

import java.nio.file.{Path, Paths}

import org.scalamu.plugin.{Mutation, ScalamuPluginConfig}
import scopt.OptionParser

import scala.util.matching.Regex

/**
 * Encapsulates all internal app configuration.
 *
 * @param reportDir a directory, to create mutation analysis reports in
 * @param sourceDirs directories containing scala sources to be mutated
 * @param testClassDirs directories containing compiled test classes
 * @param classPath list of classpath elements for applications "test" config
 * @param scalaPath path to scala executable
 * @param jvmArgs arguments, passed to spawned JVMs
 * @param mutations set of active mutation operators
 * @param excludeSources filters, used to exclude certain source files from being mutated
 * @param excludeTestsClasses filters, used to exclude certain test classes from being run
 * @param threads number of threads to be used for mutation analysis
 * @param verbose if true, be verbose about every step
 */
final case class ScalamuConfig(
  reportDir: Path = Paths.get("."),
  sourceDirs: Seq[Path] = Seq.empty,
  testClassDirs: Seq[Path] = Seq.empty,
  classPath: Seq[Path] = Seq.empty,
  scalaPath: Path = Paths.get("."),
  jvmArgs: Seq[String] = Seq.empty,
  mutations: Seq[Mutation] = ScalamuPluginConfig.allMutations,
  excludeSources: Seq[Regex] = Seq.empty,
  excludeTestsClasses: Seq[Regex] = Seq.empty,
  threads: Int = Runtime.getRuntime.availableProcessors(),
  verbose: Boolean = false
) {
  def derive[T: Derivable]: T = Derivable[T].fromConfig(this)
}

object ScalamuConfig {
  val parser: OptionParser[ScalamuConfig] = new scopt.OptionParser[ScalamuConfig]("scalamu-cli") {
    head("scalamu")

    arg[Path]("<reportDir>")
      .text("directory to create reports in")
      .action((path, config) => config.copy(reportDir = path))

    arg[Seq[Path]]("<sourceDirs>")
      .text("list of source directories")
      .action((sourceDirs, config) => config.copy(sourceDirs = sourceDirs))

    arg[Seq[Path]]("<testClassDirs>")
      .text("list of test class directories")
      .action((testClassPath, config) => config.copy(testClassDirs = testClassPath))

    arg[Seq[Path]]("<classPath>")
      .text("list of classpath elements")
      .action((cp, config) => config.copy(classPath = cp))

    arg[Path]("<scalaPath>")
      .text("path to scala executable")
      .action((scalaPath, config) => config.copy(scalaPath = scalaPath))

    arg[Seq[String]]("<jvmArgs>")
      .text("list of jvm args used by tests")
      .action((jvmArgs, config) => config.copy(jvmArgs = jvmArgs))

    //@TODO: add a way to configure a set of mutation operators used
    opt[String]('m', "mutations")
      .text("set of mutation operators")
      .action((mutations, config) => config.copy())

    opt[Seq[Regex]]("excludeSource")
      .abbr("es")
      .valueName("<regex1>,<regex2>..")
      .text("list of filters for ignored source files")
      .action((filters, config) => config.copy(excludeSources = filters))

    opt[Seq[Regex]]("excludeTestClasses")
      .abbr("et")
      .valueName("<regex1>,<regex2>..")
      .text("list of filters for ignored test classes")
      .action((filters, config) => config.copy(excludeTestsClasses = filters))

    opt[Int]("threads")
      .text("number of threads used to run tests")
      .validate(
        threads =>
          if (threads < 1) failure("Option --threads must have value >= 1.")
          else success
      )
      .action((threads, config) => config.copy(threads = threads))

    opt[Unit]("verbose")
      .text("be verbose about every step")
      .action((_, config) => config.copy(verbose = true))
  }
}
