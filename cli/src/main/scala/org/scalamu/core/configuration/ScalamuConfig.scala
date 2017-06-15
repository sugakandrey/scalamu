package org.scalamu.core.configuration

import java.nio.file.{Files, Path, Paths}

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
 * @param timeoutFactor a factor to apply to normal test duration before considering an inf. loop
 * @param timeoutConst additional flat amount of allowed time for tests to run (applied after timeoutFactor)
 * @param threads number of threads to be used for mutation analysis
 * @param verbose if true, be verbose about every step
 */
final case class ScalamuConfig(
  reportDir: Path = Paths.get("."),
  sourceDirs: Set[Path] = Set.empty,
  testClassDirs: Set[Path] = Set.empty,
  classPath: Set[Path] = Set.empty,
  scalaPath: String = "",
  jvmArgs: Seq[String] = Seq.empty,
  mutations: Seq[Mutation] = ScalamuPluginConfig.allMutations,
  excludeSources: Seq[Regex] = Seq.empty,
  excludeTestsClasses: Seq[Regex] = Seq.empty,
  timeoutFactor: Double = 1.5,
  timeoutConst: Long = 2000,
  threads: Int = 1,
  verbose: Boolean = false
) {
  require(
    testClassDirs.forall(Files.exists(_)),
    s"Test class directories must exist, but ${testClassDirs.filterNot(Files.exists(_))} were non-existent."
  )

  require(
    classPath.forall(Files.exists(_)),
    s"All classpath entries must point to existing files, but ${classPath.filterNot(Files.exists(_))} don't."
  )

  require(
    sourceDirs.forall(Files.exists(_)),
    s"Source file directories must exist, but ${sourceDirs.filterNot(Files.exists(_))} were non-existent."
  )

  require({
    val executablePath = Paths.get(scalaPath)
    Files.exists(executablePath) && Files.isExecutable(executablePath)
  }, "File at scalaPath must exist and be executable.")

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
      .action((sourceDirs, config) => config.copy(sourceDirs = sourceDirs.toSet))

    arg[Seq[Path]]("<testClassDirs>")
      .text("list of test class directories")
      .action((testClassPath, config) => config.copy(testClassDirs = testClassPath.toSet))

    arg[String]("<scalaPath>")
      .text("path to scala executable")
      .action((scalaPath, config) => config.copy(scalaPath = scalaPath))

    opt[Seq[Path]]("cp")
      .text("list of classpath elements")
      .action((cp, config) => config.copy(classPath = cp.toSet))

    opt[Seq[String]]("jvmArgs")
      .text("list of jvm args used by tests")
      .action((jvmArgs, config) => config.copy(jvmArgs = jvmArgs))

    opt[Seq[String]]('m', "mutations")
      .text("set of mutation operators")
      .action(
        (mutations, config) =>
          config.copy(mutations = mutations.map(ScalamuPluginConfig.mutationByName))
      )

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

    opt[Double]("timeoutFactor")
      .text("factor to apply to normal test duration before considering being stuck in a loop")
      .action((tf, config) => config.copy(timeoutFactor = tf))

    opt[Long]("timeoutConst")
      .text("flat amount of additional time for mutation analysis test runs")
    .action((tc, config) => config.copy(timeoutConst = tc))

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

  def parseConfig[T](args: Seq[String]): ScalamuConfig =
    parser.parse(args, ScalamuConfig()) match {
      case Some(config) => config
      case None         => System.exit(1); ???
    }
}
