package org.scalamu.idea.runner

import com.intellij.openapi.compiler.CompilerPaths
import com.intellij.openapi.module.Module
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.VirtualFile

import scala.collection.mutable
import scala.collection.Set

case class ModuleInfoExtractor(module: Module, aggregate: Boolean = true) {
  private[this] val manager    = ModuleRootManager.getInstance(module)
  private[this] val enumerator = manager.orderEntries().recursively()
  private[this] val modules    = if (aggregate) getModuleAndDependencies else Set(module)

  def sourcePaths: Set[VirtualFile]        = modules.flatMap(ModuleRootManager.getInstance(_).getSourceRoots(false))
  def testTarget: Set[VirtualFile]         = modules.flatMap(m => Option(CompilerPaths.getModuleOutputDirectory(m, true)))
  def compileClassPath: Array[VirtualFile] = enumerator.productionOnly().compileOnly().getClassesRoots
  def runClassPath: Array[VirtualFile]     = enumerator.runtimeOnly().getClassesRoots

  private def getModuleAndDependencies: Set[Module] = {
    val deps = new mutable.HashSet[Module]
    enumerator.forEachModule(m => { deps += m; true })
    deps
  }
}
