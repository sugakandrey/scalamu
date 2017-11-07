package org.scalamu.idea.runner

import java.io.BufferedOutputStream
import java.nio.file.{Files, Path, Paths}

import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.io.HttpRequests
import com.intellij.util.net.NetUtils
import org.jetbrains.plugins.scala.extensions._
import org.jetbrains.plugins.scala.project.ScalaModule

import scala.util.{Properties, Success, Try}

object ScalamuJarFetcher {
  private[this] val organization = "io.github.sugakandrey"
  private[this] val name         = "scalamu"
  private[this] val version      = "0.1.0-SNAPSHOT"
  private[this] val artifactType = "jars"

  private def artifactUrl(scalaBinaryVersion: String): String =
    s"https://oss.sonatype.org/service/local/repositories/snapshots/content/io/github/sugakandrey/scalamu_$scalaBinaryVersion/0.1-SNAPSHOT/scalamu_$scalaBinaryVersion-0.1-SNAPSHOT-assembly.jar"

  private def ivyCachePath: Path = {
    val ivyHome = Properties.propOrElse("ivy.home", Properties.userHome + "/.ivy2")
    val ivyPath = Paths.get(ivyHome)
    ivyPath.resolve("cache")
  }

  private def defaultArtifactName(scalaBinaryVersion: String): String =
    s"scalamu_$scalaBinaryVersion-assembly.jar"

  private def scalamuIvyArtifactPath(scalaBinaryVersion: String): Path =
    ivyCachePath
      .resolve(organization)
      .resolve(s"${name}_$scalaBinaryVersion")
      .resolve(version)
      .resolve(artifactType)
      .resolve(defaultArtifactName(scalaBinaryVersion))

  private def downloadWithProgressIndicator(title: String, to: Path, url: String): Try[Unit] =
    withProgressSynchronouslyTry(title) { _ =>
      val outputStream = new BufferedOutputStream(Files.newOutputStream(to))
      val indicator    = ProgressManager.getInstance().getProgressIndicator

      HttpRequests.request(url).connect { request =>
        val contentSize = request.getConnection.getContentLength
        NetUtils.copyStreamContent(indicator, request.getInputStream, outputStream, contentSize)
      }
    }

  def getCachedScalamuJar(forModule: Module): Option[Path] = {
    val scalaModule        = new ScalaModule(forModule)
    val scalaBinaryVersion = scalaModule.sdk.languageLevel.version
    val path               = scalamuIvyArtifactPath(scalaBinaryVersion)

    Some(path).filter(Files.exists(_))
  }

  def downloadScalamuJar(forModule: Module): Try[Path] = {
    val scalaModule        = new ScalaModule(forModule)
    val scalaBinaryVersion = scalaModule.sdk.languageLevel.version
    val path               = scalamuIvyArtifactPath(scalaBinaryVersion)

    if (Files.exists(path)) Success(path)
    else {
      FileUtil.createParentDirs(path.toFile)
      downloadWithProgressIndicator("Downloading Scalamu Jar", path, artifactUrl(scalaBinaryVersion))
        .map(Function.const(path))
    }
  }
}
