package org.scalamu.idea.runner

import java.nio.file.{Files, Paths}
import java.util

import com.intellij.execution.configurations.{JavaCommandLineState, JavaParameters}
import com.intellij.execution.process.{OSProcessHandler, ProcessAdapter, ProcessEvent}
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.ide.BrowserUtil
import com.intellij.ide.browsers.BrowserLauncher
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.{VfsUtil, VirtualFile}
import com.intellij.util.EnvironmentUtil
import org.jetbrains.plugins.scala.extensions.invokeLater

class ScalamuCommandLineState(
  configuration: ScalamuRunConfiguration,
  env: ExecutionEnvironment
) extends JavaCommandLineState(env) {
  private[this] val reportOverviewPath  = Paths.get(configuration.reportDir).resolve("overview.html")
  private[this] val project             = configuration.project
  private[this] val module              = configuration.getConfigurationModule.getModule

  private class ScalamuProcessAdapter extends ProcessAdapter {
    override def processTerminated(event: ProcessEvent): Unit = {
      super.processTerminated(event)
      if (configuration.openInBrowser && Files.exists(reportOverviewPath)) {
        invokeLater {
          configuration.browser.fold(BrowserUtil.browse(reportOverviewPath.toFile))(
            BrowserLauncher.getInstance().browse(reportOverviewPath.toUri.toString, _)
          )
        }
      }
    }
  }

  override def startProcess(): OSProcessHandler = {
    val processHandler = super.startProcess()
    processHandler.addProcessListener(new ScalamuProcessAdapter)
    processHandler
  }

  override def createJavaParameters(): JavaParameters = {
    val parameters       = new JavaParameters
    val workingDir       = Option(project.getBaseDir).fold(VfsUtil.getUserHomeDir.getPath)(_.getPath)
    val pathToScalamuJar = configuration.pathToJar
    val arguments        = buildScalamuArgumentsString(project)

    val combinedEnv = new util.HashMap[String, String]() {
      {
        putAll(configuration.envVariables)
        val parentEnv = EnvironmentUtil.getEnvironmentMap
        parentEnv.forEach((k, v) => if (k != "CLASSPATH") put(k, v))
      }
    }

    parameters.configureByModule(module, JavaParameters.JDK_ONLY)
    parameters.setJarPath(pathToScalamuJar)
    parameters.setWorkingDirectory(workingDir)
    parameters.setPassParentEnvs(false)
    parameters.setEnv(combinedEnv)
    parameters.getVMParametersList.addParametersString(configuration.scalamuRunnerVmParams)
    parameters.getProgramParametersList.addParametersString(arguments)
    parameters
  }

  private def optionString[T](
    args: Traversable[T],
    separator: String,
    name: String
  ): String =
    if (args.isEmpty) ""
    else s"--$name ${args.mkString(separator)}"

  private def wrapClassRoot(vf: VirtualFile): String = {
    val path = vf.getPath
    val arg  = if (path.endsWith("!/")) path.dropRight(2) else path
    wrapArg(arg)
  }

  private def wrapArg(arg: String): String = s""""$arg""""

  private def buildScalamuArgumentsString(project: Project): String = {
    val extractor        = ModuleInfoExtractor(module)
    val sourceDirsString = extractor.sourcePaths.map(wrapClassRoot).mkString(",")
    val testDirsString   = extractor.testTarget.map(wrapClassRoot).mkString(",")

    val arguments = Seq(
      wrapArg(configuration.reportDir),
      sourceDirsString,
      testDirsString
    )

    val classPath     = optionString(extractor.compileClassPath.map(wrapClassRoot), ",", "cp")
    val testClassPath = optionString(extractor.runClassPath.map(wrapClassRoot), ",", "tcp")
    val vmParameters  = optionString(configuration.vmParameters, " ", "vmParameters")
    val scalacOptions = optionString(configuration.scalacParameters, " ", "scalacParameters")
    val targetOwners  = optionString(configuration.targetOwners.map(_.toRegex.toString), ",", "targetOwners")
    val targetTests   = optionString(configuration.targetTests.map(_.toRegex.toString), ",", "targetTests")
    val mutators      = optionString(configuration.activeMutators, ",", "mutators")
    val ignoreSymbols = optionString(configuration.ignoreSymbols.map(_.toRegex.toString), ",", "ignoreSymbols")
    val verbose       = if (configuration.verboseLogging) "--verbose" else ""
    val timeoutFactor = s"--timeoutFactor ${configuration.timeoutFactor}"
    val timeoutConst  = s"--timeoutConst ${configuration.timeoutConst}"
    val parallelism   = s"--parallelism ${configuration.parallelism}"

    (Seq(
      verbose,
      timeoutFactor,
      timeoutConst,
      parallelism,
      scalacOptions,
      vmParameters,
      targetOwners,
      targetTests,
      mutators,
      ignoreSymbols,
      classPath,
      testClassPath
    ) ++ arguments).mkString(" ")
  }
}
