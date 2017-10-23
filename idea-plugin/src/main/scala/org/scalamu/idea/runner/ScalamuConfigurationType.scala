package org.scalamu.idea.runner

import javax.swing.Icon

import com.intellij.execution.configurations.{ConfigurationFactory, ConfigurationType}
import com.intellij.openapi.util.IconLoader

class ScalamuConfigurationType extends ConfigurationType {
  private[this] val runConfigurationFactory = new ScalamuRunConfigurationFactory(this)

  override def getId: String                                          = "Scalamu"
  override def getDisplayName: String                                 = "Scalamu Runner"
  override def getConfigurationTypeDescription: String                = "Executes Scalamu mutation testing"
  override def getIcon: Icon                                          = IconLoader.getIcon("/scalamu.png")
  override def getConfigurationFactories: Array[ConfigurationFactory] = Array(runConfigurationFactory)
}
