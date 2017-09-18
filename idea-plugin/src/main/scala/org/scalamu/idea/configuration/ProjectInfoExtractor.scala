package org.scalamu.idea.configuration

import com.intellij.openapi.compiler.CompilerPaths
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.{ModuleRootManager, OrderEntry}
import com.intellij.openapi.util.Condition
import com.intellij.openapi.vfs.VirtualFile

class ProjectInfoExtractor(project: Project, module: Module) {
  private[this] val manager    = ModuleRootManager.getInstance(module)
  private[this] val enumerator = manager.orderEntries()

  def sourcePaths: Array[VirtualFile]      = manager.getSourceRoots(false)
  enumerator.productionOnly()
  def testTarget: Option[VirtualFile]      = Option(CompilerPaths.getModuleOutputDirectory(module, true))
  def compileClassPath: Array[VirtualFile] = enumerator.recursively().compileOnly().getAllLibrariesAndSdkClassesRoots
  def runClassPath: Array[VirtualFile]     = enumerator.recursively().runtimeOnly().getClassesRoots
}
