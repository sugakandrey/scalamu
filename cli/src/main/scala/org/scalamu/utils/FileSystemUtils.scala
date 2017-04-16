package org.scalamu.utils

import java.io.IOException
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes

import org.slf4j.LoggerFactory

import scala.util.control.NonFatal

trait FileSystemUtils {

  private val log = LoggerFactory.getLogger(this.getClass)

  implicit class RichPath(val path: Path) {
    private[this] val fileSystem        = FileSystems.getDefault
    private[this] val archiveMatcher    = fileSystem.getPathMatcher("glob:**.{jar,zip}")
    private[this] val classFileMatcher  = fileSystem.getPathMatcher("glob:**.class")
    private[this] val sourceFileMatcher = fileSystem.getPathMatcher("glob:**.scala")

    def isJarOrZip: Boolean   = path != null && archiveMatcher.matches(path)
    def isClassFile: Boolean  = path != null && classFileMatcher.matches(path)
    def isSourceFile: Boolean = path != null && sourceFileMatcher.matches(path)
    def isDirectory: Boolean  = path != null && Files.isDirectory(path)

    override def toString: String = path.toString
  }

  private[this] case class FileVisitor[T](f: Path => T) extends SimpleFileVisitor[Path] {
    override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
      f(file)
      FileVisitResult.CONTINUE
    }

    override def visitFileFailed(file: Path, exc: IOException): FileVisitResult = {
      log.error(s"Failed to visit file $file. Cause: ${exc.getMessage}.")
      FileVisitResult.CONTINUE
    }
  }

  implicit def path2Traversable(path: Path): Traversable[Path] = new Traversable[Path] {
    override def foreach[U](f: (Path) => U): Unit = path match {
      case p if p.isJarOrZip =>
        try {
          val fs = FileSystems.newFileSystem(p, null)
          Files.walkFileTree(fs.getPath("/"), FileVisitor(f))
        } catch {
          case NonFatal(e) =>
            println(e.getMessage)
            log.error(s"Error creating FileSystem for archive. Cause: ${e.getMessage}")
        }
      case p if p.isDirectory => Files.walkFileTree(p, FileVisitor(f))
      case p                  => f(p)
    }
  }
}

object FileSystemUtils extends FileSystemUtils
