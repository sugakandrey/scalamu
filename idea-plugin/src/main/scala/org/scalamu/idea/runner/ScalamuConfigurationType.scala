package org.scalamu.idea.runner

import javax.swing.Icon

import com.intellij.execution.configurations.{ConfigurationFactory, ConfigurationType}
import org.jetbrains.plugins.scala.icons.Icons

class ScalamuConfigurationType extends ConfigurationType {
  private[this] val runConfigurationFactory = new ScalamuRunConfigurationFactory(this)

  override def getId: String                                          = "Scalamu"
  override def getDisplayName: String                                 = "Scalamu Runner"
  override def getConfigurationTypeDescription: String                = "Executes Scalamu mutation testing"
  override def getIcon: Icon                                          = Icons.SCALA_TEST
  override def getConfigurationFactories: Array[ConfigurationFactory] = Array(runConfigurationFactory)
}
