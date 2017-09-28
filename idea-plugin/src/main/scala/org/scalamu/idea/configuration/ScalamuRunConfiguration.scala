package org.scalamu.idea.configuration

import java.nio.file.{Files, Paths}
import java.util

import com.intellij.execution.configurations._
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.{ExecutionBundle, Executor, JavaRunConfigurationExtensionManager}
import com.intellij.ide.browsers.{WebBrowser, WebBrowserManager}
import com.intellij.openapi.components.PathMacroManager
import com.intellij.openapi.module.{JavaModuleType, Module, ModuleType}
import com.intellij.openapi.options.{SettingsEditor, SettingsEditorGroup}
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.JDOMExternalizer
import com.intellij.psi.{PsiDirectory, PsiElement}
import com.intellij.refactoring.listeners.{RefactoringElementAdapter, RefactoringElementListener}
import org.jdom.Element
import org.jetbrains.plugins.scala.project._
import org.scalamu.idea.ScalamuBundle
import org.scalamu.idea.gui._

import scala.collection.JavaConverters._
import scala.util.matching.Regex

class ScalamuRunConfiguration(
  val name: String,
  val project: Project,
  val configurationFactory: ConfigurationFactory
) extends ModuleBasedConfiguration[RunConfigurationModule](
      name,
      new RunConfigurationModule(project),
      configurationFactory
    )
    with RefactoringListenerProvider {

  private def readCommaSeparatedSeq(source: String): Seq[String] = if (source.isEmpty) Seq.empty else source.split(",")

  private[idea] var targetClasses: Seq[Regex]              = ScalamuDefaultSettings.targetSources
  private[idea] var targetTests: Seq[Regex]                = ScalamuDefaultSettings.targetTests
  private[idea] var verboseLogging: Boolean                = ScalamuDefaultSettings.verboseLogging
  private[idea] var openInBrowser: Boolean                 = ScalamuDefaultSettings.openInBrowser
  private[idea] var vmParameters: String                   = ScalamuDefaultSettings.vmParameters
  private[idea] var scalacParameters: String               = ScalamuDefaultSettings.scalacParameters
  private[idea] var timeoutConst: Int                      = ScalamuDefaultSettings.timeoutConst
  private[idea] var timeoutFactor: Double                  = ScalamuDefaultSettings.timeoutFactor
  private[idea] var parallelism: Int                       = ScalamuDefaultSettings.parallelism
  private[idea] var browser: Option[WebBrowser]            = ScalamuDefaultSettings.browser
  private[idea] var activeMutators: Seq[String]            = ScalamuDefaultSettings.activeMutators
  private[idea] var envVariables: util.Map[String, String] = new util.HashMap[String, String]
  private[idea] var pathToJar: String                      = ""
  private[idea] var reportDir: String                      = ""
  private[idea] var aggregate: Boolean                     = true

  def getTargetClassesAsString: String = targetClasses.map(_.toString).mkString(",")
  def getTargetTestsAsString: String  = targetTests.map(_.toString).mkString(",")

  def apply(form: ScalamuConfigurationForm): Unit = {
    setModule(form.getModule)

    parallelism   = form.getParallelism
    vmParameters  = form.getVMParameters
    pathToJar     = form.getJarPath
    reportDir     = form.getReportDir
    openInBrowser = form.getOpenInBrowser
    browser       = Option(WebBrowserManager.getInstance().findBrowserById(form.getBrowserFamily.getName))
    targetClasses = readCommaSeparatedSeq(form.getTargetClasses).map(_.r)
    targetTests   = readCommaSeparatedSeq(form.getTargetTests).map(_.r)
  }

  def apply(form: ScalamuMutatorsForm): Unit = activeMutators = form.getActiveMutators

  def apply(form: ScalamuAdvancedConfigurationForm): Unit = {
    timeoutFactor    = form.getTimeoutFactor
    timeoutConst     = form.getTimeoutConst
    scalacParameters = form.getScalacParameters
    verboseLogging   = form.getVerboseLogging
    aggregate        = form.getAggregate

    envVariables.clear()
    envVariables.putAll(form.getEnvVariables)

  }

  override def getValidModules: util.Collection[Module] =
    project.modulesWithScala.filter(ModuleType.get(_) == JavaModuleType.getModuleType).asJava

  override def getConfigurationEditor: SettingsEditor[_ <: RunConfiguration] = {
    val group = new SettingsEditorGroup[ScalamuRunConfiguration]

    group.addEditor(
      ExecutionBundle.message("run.configuration.configuration.tab.title"),
      new ScalamuSettingsEditor(project)
    )

    group.addEditor(
      ScalamuBundle.getMessage("run.configuration.tab.advanced"),
      new ScalamuAdvancedEditor(project)
    )

    group.addEditor(
      ScalamuBundle.getMessage("run.configuration.tab.mutators"),
      new ScalamuMutatorsEditor(project)
    )

    JavaRunConfigurationExtensionManager.getInstance().appendEditors(this, group)
    group
  }

  override def getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState =
    try { checkConfiguration(); new ScalamuCommandLineState(this, environment) } catch {
      case _: RuntimeConfigurationException => null
    }

  override def checkConfiguration(): Unit = {
    if (pathToJar.isEmpty || !Files.exists(Paths.get(pathToJar)))
      throw new RuntimeConfigurationError("Path to scalamu jar not specified.")

    if (activeMutators.isEmpty)
      throw new RuntimeConfigurationWarning("No active mutators specified.")

    if (reportDir.isEmpty)
      throw new RuntimeConfigurationWarning("Report directory is not specified.")
  }

  override def getRefactoringElementListener(element: PsiElement): RefactoringElementListener = element match {
    case dir: PsiDirectory =>
      val oldReportDir = reportDir
      val oldPatToJar  = pathToJar
      val path         = Paths.get(dir.getVirtualFile.getPath)

      if (Paths.get(reportDir).startsWith(path) || Paths.get(pathToJar).startsWith(path)) {
        new RefactoringElementAdapter {
          override def elementRenamedOrMoved(newElement: PsiElement): Unit = newElement match {
            case newDir: PsiDirectory =>
              val newPath = newDir.getVirtualFile.getPath
              reportDir = reportDir.replace(path.toString, newPath)
              pathToJar = pathToJar.replace(path.toString, newPath)
            case _ =>
          }

          override def undoElementMovedOrRenamed(newElement: PsiElement, oldQualifiedName: String): Unit = {
            if (reportDir != oldReportDir) reportDir = oldReportDir
            if (pathToJar != oldPatToJar) pathToJar  = oldPatToJar
          }
        }
      } else RefactoringElementListener.DEAF
    case _ => RefactoringElementListener.DEAF
  }

  override def writeExternal(element: Element): Unit = {
    super.writeExternal(element)
    def write[T](name: String, value: T): Unit        = JDOMExternalizer.write(element, name, value.toString)
    def regexSeqToString(regexes: Seq[Regex]): String = regexes.map(_.toString).mkString(",")

    val toBePersisted: Map[String, String] = Map(
      "verboseLogging" -> verboseLogging,
      "timeoutConst"   -> timeoutConst,
      "timeoutFactor"  -> timeoutFactor,
      "openInBrowser"  -> openInBrowser,
      "targetSources"  -> regexSeqToString(targetClasses),
      "targetTests"    -> regexSeqToString(targetTests),
      "vmOptions"      -> vmParameters,
      "scalacOptions"  -> scalacParameters,
      "parallelism"    -> parallelism,
      "reportDir"      -> reportDir,
      "pathToJar"      -> pathToJar,
      "activeMutators" -> activeMutators.mkString(",")
    ).mapValues(_.toString)

    JavaRunConfigurationExtensionManager.getInstance.writeExternal(this, element)
    writeModule(element)
    toBePersisted.foreach(Function.tupled(write))
    browser.foreach(b => JDOMExternalizer.write(element, "browser", b.getId.toString))
    PathMacroManager.getInstance(project).collapsePathsRecursively(element)
  }

  override def readExternal(element: Element): Unit = {
    PathMacroManager.getInstance(project).expandPaths(element)
    super.readExternal(element)
    JavaRunConfigurationExtensionManager.getInstance().readExternal(this, element)
    readModule(element)

    verboseLogging   = JDOMExternalizer.readBoolean(element, "verboseLogging")
    timeoutConst     = JDOMExternalizer.readInteger(element, "timeoutConst", ScalamuDefaultSettings.timeoutConst)
    timeoutFactor    = JDOMExternalizer.readString(element, "timeoutFactor").toDouble
    openInBrowser    = JDOMExternalizer.readBoolean(element, "openInBrowser")
    vmParameters     = JDOMExternalizer.readString(element, "vmOptions")
    scalacParameters = JDOMExternalizer.readString(element, "scalacOptions")
    parallelism      = JDOMExternalizer.readInteger(element, "parallelism", ScalamuDefaultSettings.parallelism)
    browser          = Option(WebBrowserManager.getInstance().findBrowserById(JDOMExternalizer.readString(element, "browser")))
    activeMutators   = readCommaSeparatedSeq(JDOMExternalizer.readString(element, "activeMutators"))
    targetClasses    = readCommaSeparatedSeq(JDOMExternalizer.readString(element, "targetSources")).map(_.r)
    targetTests      = readCommaSeparatedSeq(JDOMExternalizer.readString(element, "targetTests")).map(_.r)
    reportDir        = JDOMExternalizer.readString(element, "reportDir")
    pathToJar        = JDOMExternalizer.readString(element, "pathToJar")
  }
}
