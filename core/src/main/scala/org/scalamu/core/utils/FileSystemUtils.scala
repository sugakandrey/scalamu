package org.scalamu.core.utils

import java.io.{IOException, InputStream, OutputStream}
import java.nio.charset.Charset
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes

import scala.collection.JavaConverters._
import scala.reflect.io.AbstractFile
import scala.util.Try
import scala.util.control.NonFatal

trait FileSystemUtils {
  implicit class RichAbstractFile(val f: AbstractFile) {
    def isClassFile: Boolean = f.name.endsWith(".class")

    def traverseFiles: Set[AbstractFile] =
      f.flatMap {
        case file if !file.isDirectory => Set(file)
        case dir                       => dir.traverseFiles
      }(collection.breakOut)
  }

  implicit class RichPath(val path: Path) {
    private[this] val fileSystem        = FileSystems.getDefault
    private[this] val archiveMatcher    = fileSystem.getPathMatcher("glob:**.{jar,zip}")
    private[this] val classFileMatcher  = fileSystem.getPathMatcher("glob:**.class")
    private[this] val sourceFileMatcher = fileSystem.getPathMatcher("glob:**.{java,scala}")

    def isJarOrZip: Boolean   = path != null && archiveMatcher.matches(path)
    def isClassFile: Boolean  = path != null && classFileMatcher.matches(path)
    def isSourceFile: Boolean = path != null && sourceFileMatcher.matches(path)
    def isDirectory: Boolean  = path != null && Files.isDirectory(path)

    def /(other: String): Path = path.resolve(other)
    def /(other: Path): Path   = path.resolve(other)

    def toInputStream: Try[InputStream]   = Try(Files.newInputStream(path))
    def toOutputStream: Try[OutputStream] = Try(Files.newOutputStream(path))

    def writeLines(lines: Seq[String])(implicit cs: Charset): Unit =
      Files.write(path, lines.mkString("", "\n", "\n").getBytes(cs))

    def exists: Boolean = Files.exists(path)

    def readLines(): List[String] = Files.readAllLines(path).asScala.toList

    override def toString: String = path.toString
  }

  private[this] case class FileVisitor[T](f: Path => T) extends SimpleFileVisitor[Path] {
    override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
      f(file)
      FileVisitResult.CONTINUE
    }

    override def visitFileFailed(file: Path, exc: IOException): FileVisitResult = {
      scribe.error(s"Failed to visit file $file. Cause: ${exc.getMessage}.")
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
            scribe.error(s"Error creating FileSystem for archive. Cause: ${e.getMessage}")
        }
      case p if p.isDirectory => Files.walkFileTree(p, FileVisitor(f))
      case p                  => f(p)
    }
  }
}

object FileSystemUtils extends FileSystemUtils
