package org.scalamu.idea.configuration

import java.util

import com.intellij.execution.configurations._
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.runners.{ExecutionEnvironment, ProgramRunner}
import com.intellij.execution.{ExecutionBundle, ExecutionResult, Executor, JavaRunConfigurationExtensionManager}
import com.intellij.openapi.module.Module
import com.intellij.openapi.options.{SettingsEditor, SettingsEditorGroup}
import com.intellij.openapi.project.Project
import org.jetbrains.plugins.scala.project._
import org.scalamu.idea.gui.{ScalamuSettingsEditor}

import scala.collection.JavaConverters._

class ScalamuRunConfiguration(
  val name: String,
  val project: Project,
  val configurationFactory: ConfigurationFactory
) extends ModuleBasedConfiguration[RunConfigurationModule](
      name,
      new RunConfigurationModule(project),
      configurationFactory
    ) {

  override def getValidModules: util.Collection[Module] = project.modulesWithScala.asJava

  override def getConfigurationEditor: SettingsEditor[_ <: RunConfiguration] = {
    val group = new SettingsEditorGroup[ScalamuRunConfiguration]

    group.addEditor(
      ExecutionBundle.message("run.configuration.configuration.tab.title"),
      new ScalamuSettingsEditor(project, this)
    )

    JavaRunConfigurationExtensionManager.getInstance().appendEditors(this, group)
    group
  }

  override def getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState =
    new JavaCommandLineState(environment) {
      override def createJavaParameters(): JavaParameters = ???
      override def startProcess(): OSProcessHandler = super.startProcess()
      override def execute(executor: Executor, runner: ProgramRunner[_ <: RunnerSettings]): ExecutionResult =
        super.execute(executor, runner)
    }

  private[idea] def baseDir: String = project.getBaseDir.getPath
}
