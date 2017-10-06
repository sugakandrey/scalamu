package org.scalamu.idea.configuration

import java.nio.file.Paths

import com.intellij.execution.configurations.{JavaCommandLineState, JavaParameters}
import com.intellij.execution.process.{OSProcessHandler, ProcessAdapter, ProcessEvent}
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.ide.BrowserUtil
import com.intellij.ide.browsers.BrowserLauncher
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import org.jetbrains.plugins.scala.extensions.invokeLater

class ScalamuCommandLineState(
  configuration: ScalamuRunConfiguration,
  env: ExecutionEnvironment
) extends JavaCommandLineState(env) {
  private[this] val mainClass            = "org.scalamu.entry.EntryPoint"
  private[this] val scalamuVMParameters  = ""
  private[this] val reportOverviewPath   = Paths.get(configuration.reportDir).resolve("overview.html")

  private class ScalamuProcessAdapter extends ProcessAdapter {
    override def processTerminated(event: ProcessEvent): Unit = {
      super.processTerminated(event)
      if (configuration.openInBrowser) {
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
  }

  override def createJavaParameters(): JavaParameters = {
    val parameters       = new JavaParameters
    val project          = configuration.project
    val workingDir       = Option(project.getBaseDir).fold(VfsUtil.getUserHomeDir.getPath)(_.getPath)
    val pathToScalamuJar = configuration.pathToJar
    val arguments        = buildScalamuArgumentsString(project)
    val module           = configuration.getConfigurationModule.getModule

    parameters.configureByModule(module, JavaParameters.JDK_ONLY)
    parameters.setJarPath(pathToScalamuJar)
    parameters.setWorkingDirectory(workingDir)
    parameters.setMainClass(mainClass)
    parameters.setEnv(configuration.envVariables)
    parameters.getVMParametersList.addParametersString(scalamuVMParameters)
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

  private def buildScalamuArgumentsString(project: Project): String = {
    val extractor        = ModuleInfoExtractor(configuration.getConfigurationModule.getModule)
    val sourceDirsString = extractor.sourcePaths.mkString(",")
    val testDirsString   = extractor.testTarget.mkString(",")

    val arguments = Seq(
      configuration.reportDir,
      sourceDirsString,
      testDirsString
    )

    val verbose       = if (configuration.verboseLogging) "--verbose" else ""
    val timeoutFactor = s"--timeoutFactor ${configuration.timeoutFactor}"
    val timeoutConst  = s"--timeoutConst ${configuration.timeoutConst}"
    val parallelism   = s"--parallelism ${configuration.parallelism}"
    val scalacOptions = optionString(configuration.scalacParameters, "", "scalacParameters")
    val vmParameters  = optionString(configuration.vmParameters, "", "vmParameters")
    val targetTests   = optionString(configuration.targetTests, ",", "targetTests")
    val targetClasses = optionString(configuration.targetClasses, ",", "targetClasses")
    val mutators      = optionString(configuration.activeMutators, ",", "activeMutators")
    val classPath     = optionString(extractor.compileClassPath.map(_.getPath), ",", "cp")
    val testClassPath = optionString(extractor.runClassPath.map(_.getPath), ",", "tcp")

    (Seq(
      verbose,
      timeoutFactor,
      timeoutConst,
      parallelism,
      scalacOptions,
      vmParameters,
      targetClasses,
      targetTests,
      mutators,
      classPath,
      testClassPath
    ) ++ arguments).mkString(" ")
  }
}
