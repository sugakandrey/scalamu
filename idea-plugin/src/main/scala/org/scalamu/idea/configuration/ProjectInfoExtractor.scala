package org.scalamu.idea.configuration

import com.intellij.openapi.module.Module
import com.intellij.openapi.projectRoots.JdkUtil
import com.intellij.openapi.roots.OrderEnumerator
import com.intellij.util.PathUtil
import org.jetbrains.jps.incremental.scala.Client
import org.jetbrains.plugins.scala.project._
import org.jetbrains.plugins.scala.util.ScalaUtil

object ProjectInfoExtractor {
  def extract(module: Module): Any = {
    val classPath = module.scalaSdk.get.compilerClasspath
    ScalaUtil.runnersPath()
    PathUtil.getJarPathForClass(classOf[Client])
    
  }
}
