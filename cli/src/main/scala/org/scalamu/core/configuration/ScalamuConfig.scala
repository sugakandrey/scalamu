package org.scalamu.core.configuration

import java.nio.file.{Files, Path, Paths}

import org.scalamu.plugin.{Mutator, ScalamuPluginConfig}
import org.scalamu.testapi.TestingFramework
import scopt.OptionParser

import scala.util.matching.Regex

/**
 * Encapsulates all internal app configuration.
 *
 * @param reportDir a directory, to create mutation analysis reports in
 * @param sourceDirs directories containing scala sources to be mutated
 * @param testClassDirs directories containing compiled test classes
 * @param classPath list of classpath elements for applications "compile" config
 * @param testClassPath list of classpath elements for application "test" config
 * @param vmParameters arguments, passed to spawned JVMs
 * @param activeMutators set of active mutation operators
 * @param targetOwners filters, used to only include certain source files into mutation process
 * @param targetTests filters, used to only run certain test classes
 * @param ignoreSymbols ignore symbols with their fullname matching provided regexes
 * @param testingOptions options to pass to framework's test runner
 * @param scalacParameters options to be passed to scalac
 * @param timeoutFactor a factor to apply to normal test duration before considering an inf. loop
 * @param timeoutConst additional flat amount of allowed time for tests to run (applied after timeoutFactor)
 * @param parallelism number of runners to be used for mutation analysis
 * @param verbose if true, be verbose about every step
 * @param recompileOnly Only run project recompilation (for internal testing)
 */
final case class ScalamuConfig(
  reportDir: Path                     = Paths.get("."),
  sourceDirs: Set[Path]               = Set.empty,
  testClassDirs: Set[Path]            = Set.empty,
  classPath: Set[Path]                = Set.empty,
  testClassPath: Set[Path]            = Set.empty,
  vmParameters: String                = "",
  activeMutators: Seq[Mutator]        = ScalamuPluginConfig.allMutators,
  targetOwners: Seq[Regex]            = Seq.empty,
  targetTests: Seq[Regex]             = Seq.empty,
  ignoreSymbols: Seq[Regex]           = Seq.empty,
  testingOptions: Map[String, String] = Map.empty,
  scalacParameters: String            = "",
  timeoutFactor: Double               = 1.5,
  timeoutConst: Long                  = 2000,
  parallelism: Int                    = 1,
  verbose: Boolean                    = false,
  recompileOnly: Boolean              = false
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
      .text("""list of "compile" classpath elements""")
      .action((cp, config) => config.copy(classPath = cp.toSet))

    opt[Seq[Path]]("tcp")
      .text("""list of "test" classpath elements""")
      .action((tcp, config) => config.copy(testClassPath = tcp.toSet))

    opt[String]("vmParameters")
      .text("arguments for forked JVM running tests")
      .action((jvmOpts, config) => config.copy(vmParameters = jvmOpts))

    opt[Seq[String]]("mutators")
      .text("set of active mutators")
      .action(
        (mutations, config) => config.copy(activeMutators = mutations.map(ScalamuPluginConfig.mutatorsByName))
      )

    opt[Seq[Regex]]("targetOwners")
      .valueName("<regex1>,<regex2>..")
      .text("only mutate trees with certain owners")
      .action((filters, config) => config.copy(targetOwners = filters))

    opt[Seq[Regex]]("targetTests")
      .valueName("<regex1>,<regex2>..")
      .text("only run certain test classes")
      .action((filters, config) => config.copy(targetTests = filters))

    opt[Seq[Regex]]("ignoreSymbols")
      .valueName("<regex1>,<regex2>..")
      .text("ignore trees with certain owner names")
      .action((ignoreSymbols, config) => config.copy(ignoreSymbols = ignoreSymbols))

    opt[Map[String, String]]("testOptions")
      .valueName("framework1=optionString1, framework2=optionString2...")
      .text("per framework test runner options")
      .validate(
        options =>
          if (!options.keys.forall(TestingFramework.frameworkNames.contains))
            failure("Unsupported framework name.")
          else
          success
      )
      .action((options, config) => config.copy(testingOptions = options))

    opt[String]("scalacParameters")
      .text("options to be passed to scalac")
      .action((scalacOptions, config) => config.copy(scalacParameters = scalacOptions))

    opt[Double]("timeoutFactor")
      .text("factor to apply to normal test duration before considering being stuck in a loop")
      .validate(tf => if (tf < 1) failure(s"Timeout factor must be >= 1.") else success)
      .action((tf, config) => config.copy(timeoutFactor = tf))

    opt[Long]("timeoutConst")
      .text("flat amount of additional time for mutation analysis test runs")
      .validate(tc => if (tc < 0) failure(s"Timeout const value must be >= 0.") else success)
      .action((tc, config) => config.copy(timeoutConst = tc))

    opt[Int]("parallelism")
      .text("number of runners used to perform mutation analysis")
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
      .text("do not perform mutation analysis (internal testing option)")
      .action((_, config) => config.copy(recompileOnly = true))
  }

  def parseConfig[T](args: Seq[String]): ScalamuConfig =
    parser.parse(args, ScalamuConfig()) match {
      case Some(config) => config
      case None         => System.exit(1); ???
    }
}
