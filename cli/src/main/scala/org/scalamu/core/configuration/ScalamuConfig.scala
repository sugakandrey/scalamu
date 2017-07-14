package org.scalamu.core.configuration

import java.nio.file.{Files, Path, Paths}

import org.scalamu.plugin.{Mutation, ScalamuPluginConfig}
import org.scalamu.testapi.TestingFramework
import scopt.OptionParser

import scala.util.matching.Regex

/**
 * Encapsulates all internal app configuration.
 *
 * @param reportDir a directory, to create mutation analysis reports in
 * @param sourceDirs directories containing scala sources to be mutated
 * @param testClassDirs directories containing compiled test classes
 * @param classPath list of classpath elements for applications "test" config
 * @param jvmOpts arguments, passed to spawned JVMs
 * @param mutations set of active mutation operators
 * @param excludeSources filters, used to exclude certain source files from being mutated
 * @param excludeTestsClasses filters, used to exclude certain test classes from being run
 * @param testingOptions options to pass to framework's test runner
 * @param scalacOptions options to be passed to scalac
 * @param timeoutFactor a factor to apply to normal test duration before considering an inf. loop
 * @param timeoutConst additional flat amount of allowed time for tests to run (applied after timeoutFactor)
 * @param parallelism number of runners to be used for mutation analysis
 * @param verbose if true, be verbose about every step
 * @param recompileOnly Only run project recompilation (for internal testing)
 */
final case class ScalamuConfig(
  reportDir: Path = Paths.get("."),
  sourceDirs: Set[Path] = Set.empty,
  testClassDirs: Set[Path] = Set.empty,
  classPath: Set[Path] = Set.empty,
  jvmOpts: String = "",
  mutations: Seq[Mutation] = ScalamuPluginConfig.allMutations,
  excludeSources: Seq[Regex] = Seq.empty,
  excludeTestsClasses: Seq[Regex] = Seq.empty,
  testingOptions: Map[String, String] = Map.empty,
  scalacOptions: String = "",
  timeoutFactor: Double = 1.5,
  timeoutConst: Long = 2000,
  parallelism: Int = 1,
  verbose: Boolean = false,
  recompileOnly: Boolean = false
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
      .validate { sdirs =>
        if (!sdirs.exists(Files.exists(_)))
          failure(
            s"Source file directories must exist, but all provided directories were non-existent."
          )
        else success
      }
      .action((sourceDirs, config) => config.copy(sourceDirs = sourceDirs.toSet))

    arg[Seq[Path]]("<testClassDirs>")
      .text("list of test class directories")
      .validate { tdirs =>
        if (!tdirs.exists(Files.exists(_)))
          failure(
            s"Test class directories must exist, but all provided directories were non-existent."
          )
        else success
      }
      .action((testClassPath, config) => config.copy(testClassDirs = testClassPath.toSet))

    opt[Seq[Path]]("cp")
      .text("list of classpath elements")
      .validate { classpath =>
        if (classpath.exists(!Files.exists(_)))
          failure(
            s"All classpath entries must point to existing files, but ${classpath.filterNot(Files.exists(_))} don't."
          )
        else success
      }
      .action((cp, config) => config.copy(classPath = cp.toSet))

    opt[String]("jvmOpts")
      .text("jvm args used by tests")
      .action((jvmOpts, config) => config.copy(jvmOpts = jvmOpts))

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

    opt[Map[String, String]]("testOptions")
      .abbr("to")
      .valueName("framework1=optionString1, framework2=optionString2...")
      .text("Per framework test runner options")
      .validate(
        options =>
          if (!options.keys.forall(TestingFramework.frameworkNames.contains))
            failure("Unsupported framework name.")
          else
          success
      )
      .action((options, config) => config.copy(testingOptions = options))

    opt[String]("scalacOptions")
      .text("Options to be passed to scalac")
      .action((scalacOptions, config) => config.copy(scalacOptions = scalacOptions))

    opt[Double]("timeoutFactor")
      .text("factor to apply to normal test duration before considering being stuck in a loop")
      .validate(tf => if (tf < 1) failure(s"Timeout factor must be >= 1.") else success)
      .action((tf, config) => config.copy(timeoutFactor = tf))

    opt[Long]("timeoutConst")
      .text("flat amount of additional time for mutation analysis test runs")
      .validate(tc => if (tc < 0) failure(s"Timeout const value must be >= 0.") else success)
      .action((tc, config) => config.copy(timeoutConst = tc))

    opt[Int]('p', "parallelism")
      .text("Number of runners used to perform mutation analysis")
      .validate(
        threads =>
          if (threads < 1) failure("Option --parallelism must have value >= 1.")
          else success
      )
      .action((parallelism, config) => config.copy(parallelism = parallelism))

    opt[Unit]("verbose")
      .text("be verbose about every step")
      .action((_, config) => config.copy(verbose = true))

    opt[Unit]("recompileOnly")
      .text("Do not perform mutation analysis (internal testing option)")
      .action((_, config) => config.copy(recompileOnly = true))
  }

  def parseConfig[T](args: Seq[String]): ScalamuConfig =
    parser.parse(args, ScalamuConfig()) match {
      case Some(config) => config
      case None         => System.exit(1); ???
    }
}
