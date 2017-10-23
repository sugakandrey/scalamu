package org.scalamu.idea.runner

import com.intellij.execution.configurations.{ConfigurationFactory, ConfigurationType, RunConfiguration}
import com.intellij.openapi.project.Project
import org.jetbrains.plugins.scala.project._

class ScalamuRunConfigurationFactory(tpe: ConfigurationType) extends ConfigurationFactory(tpe) {

  override def createTemplateConfiguration(project: Project): RunConfiguration =
    new ScalamuRunConfiguration("Scalamu Run Configuration", project, this)

  override def isApplicable(project: Project): Boolean = project.hasScala
}
