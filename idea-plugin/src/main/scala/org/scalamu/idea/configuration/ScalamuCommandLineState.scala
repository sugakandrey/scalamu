package org.scalamu.idea.configuration

import com.intellij.execution.configurations.{JavaCommandLineState, JavaParameters}
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.JavaSdk
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable
import com.intellij.openapi.vfs.VfsUtil

class ScalamuCommandLineState(configuration: ScalamuRunConfiguration, env: ExecutionEnvironment)
    extends JavaCommandLineState(env) {
  private[this] val mainClass            = "org.scalamu.entry.EntryPoint"
  private[this] val compatibleJDKVersion = "1.8"
  private[this] val scalamuVMParameters  = ""

  override def createJavaParameters(): JavaParameters = {
    val parameters       = new JavaParameters
    val project          = configuration.project
    val workingDir       = Option(project.getBaseDir).fold(VfsUtil.getUserHomeDir.getPath)(_.getPath)
    val sdks             = ProjectStructureConfigurable.getInstance(project).getProjectJdksModel.getSdks
    val pathToScalamuJar = configuration.pathToJar
    val arguments        = buildScalamuArgumentsString(project)

    val maybeJDK = sdks.find { sdk =>
      sdk.getSdkType == JavaSdk.getInstance() && sdk.getVersionString.contains(compatibleJDKVersion)
    }

    maybeJDK match {
      case Some(jdk) =>
        parameters.configureByProject(project, JavaParameters.JDK_ONLY, jdk)
        parameters.setWorkingDirectory(workingDir)
        parameters.setMainClass(mainClass)
        parameters.getClassPath.add(pathToScalamuJar)
        parameters.setEnv(configuration.envVariables)
        parameters.getVMParametersList.addParametersString(scalamuVMParameters)
        parameters.getProgramParametersList.addParametersString(arguments)
        parameters
      case _ => null
    }
  }

  private def optionString[T](
    args: Traversable[T],
    separator: String,
    name: String
  ): String =
    if (args.isEmpty) ""
    else s"--$name ${args.mkString(separator)}"

  private def buildScalamuArgumentsString(project: Project): String = {
    val extractor = new ProjectInfoExtractor(project, configuration.getConfigurationModule.getModule)
    val sourceDirsString = extractor.sourcePaths.mkString(",")
    val testDirsString = extractor.testTarget.mkString(",")
    
    val arguments = Seq(
      configuration.reportDir,
      sourceDirsString,
      testDirsString
    )
    
    
    val verbose       = if (configuration.verboseLogging) "--verbose" else ""
    val timeoutFactor = s"--timeoutFactor ${configuration.timeoutFactor}"
    val timeoutConst  = s"--timeoutConst ${configuration.timeoutConst}"
    val parallelisn   = s"--parallelisn ${configuration.parallelism}"
    val scalacOptions = optionString(configuration.scalacParameters, "", "scalacParameters")
    val vmParameters  = optionString(configuration.vmParameters, "", "vmParameters")
    val targetTests   = optionString(configuration.targetTests, ",", "targetTests")
    val targetSources = optionString(configuration.targetSources, ",", "targetSources")
    val mutations =  optionString(configuration.activeMutators, ",", "activeMutators")
    ???
  }
}
