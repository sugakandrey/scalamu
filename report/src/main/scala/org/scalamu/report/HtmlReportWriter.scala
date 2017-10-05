package org.scalamu.report

import java.nio.file.{Files, Path, Paths}

import org.scalamu.core.configuration.ScalamuConfig
import org.scalamu.core.runners._
import org.scalamu.utils.FileSystemUtils._

import scala.io.Source

object HtmlReportWriter {
  def generateFromProjectSummary(summary: ProjectSummary, config: ScalamuConfig, dir: Path): Unit = {
    val projectOverview = html.projectOverview(summary)

    val css =
      Source.fromInputStream(getClass.getClassLoader.getResourceAsStream("style.css")).mkString

    tryWith(Files.newBufferedWriter(dir / "overview.html")) { writer =>
      writer.write(projectOverview.body)
    }

    summary.packages.foreach { p =>
      val nameSegments    = p.name.split("\\.")
      val reportPath      = Paths.get(dir.toString, nameSegments: _*)
      val packageOverview = html.packageOverview(p)
      Files.createDirectories(reportPath)
      tryWith(Files.newBufferedWriter(reportPath / "overview.html")) { writer =>
        writer.write(packageOverview.body)
      }

      p.sourceFiles.foreach { sf =>
        val sourceFileOverview = html.sourceFileOverview(sf, config, css)
        tryWith(Files.newBufferedWriter(reportPath / s"${sf.name}.html")) { writer =>
          writer.write(sourceFileOverview.body)
        }
      }
    }
  }
}
