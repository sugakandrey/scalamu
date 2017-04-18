package org.scalamu.utils

import java.io.IOException
import java.nio.file.{Files, Path, StandardOpenOption}

import org.scalamu.testutil.ScalamuSpec

class FileSystemUtilsSpec extends ScalamuSpec {
  def createTempFile(prefix: String, suffix: Option[String] = None): RichPath = {
    // if no suffix - create directory
    val file = suffix.fold(Files.createTempDirectory(prefix))(
      Files.createTempFile(prefix, _)
    )
    file.toFile.deleteOnExit()
    file
  }

  "FileSystemUtils" should "correctly identify .jar and .zip files" in {
    val isJarOrZip = Seq(
      createTempFile("aJar", Some(".jar")),
      createTempFile("almostAJar", Some(".jar")),
      createTempFile("namingIsHard", Some(".zip"))
    )
    forAll(isJarOrZip)(_ should be a 'jarOrZip)

    val notJarOrZip = Seq(
      createTempFile("dir"),
      createTempFile("notAJar", Some(".jarr")),
      createTempFile("archive", Some(".rar"))
    )
    forAll(notJarOrZip)(_ should not be 'jarOrZip)
  }

  it should "correctly identify directories" in {
    val directories = Seq(
      createTempFile("aDirectory"),
      createTempFile("directory99"),
      createTempFile("1233")
    )
    forAll(directories)(_ should be a 'directory)

    val notADirectory = Seq(
      createTempFile("temp", Some(".tmp")),
      createTempFile("someFile", Some(".ext"))
    )
    forAll(notADirectory)(_ should not be 'directory)
  }

  it should "correctly identify .class files" in {
    val classFiles = Seq(
      createTempFile("Foo", Some(".class")),
      createTempFile("SomeObject4", Some(".class")),
      createTempFile("org.spring.AbstractBeanPostProcessor", Some(".class"))
    )
    forAll(classFiles)(_ should be a 'classFile)

    val notAClassFile = Seq(
      createTempFile("dir"),
      createTempFile("jarFile", Some(".jar")),
      createTempFile("sourceFile", Some(".scala"))
    )
    forAll(notAClassFile)(_ should not be 'classFile)
  }

  it should "correctly identify .scala files" in {
    val isJarOrZip = Seq(
      createTempFile("foo.bar.Baz", Some(".scala")),
      createTempFile("Unit", Some(".scala")),
      createTempFile("AlNum123", Some(".scala"))
    )
    forAll(isJarOrZip)(_ should be a 'sourceFile)

    val notASourceFile = Seq(
      createTempFile("dir"),
      createTempFile("JavaSource", Some(".java")),
      createTempFile("NotASource", Some(".scal"))
    )
    forAll(notASourceFile)(_ should not be 'sourceFile)
  }

  private def createZipFile(name: String): Path = {
    val f = createTempFile(name, Some(".zip")).path
    import java.util.zip.{ZipEntry, ZipOutputStream}

    val out = new ZipOutputStream(Files.newOutputStream(f, StandardOpenOption.WRITE))
    val buf = Array.ofDim[Byte](1024)

    try {
      (1 to 100).foreach { i =>
        val file = createTempFile(s"file$i", Some(".class")).path
        try {
          val in = Files.newInputStream(file)
          out.putNextEntry(new ZipEntry(s"file$i.class"))

          var len = in.read(buf)
          while (len > 0) {
            out.write(buf, 0, len)
            len = in.read(buf)
          }
          out.closeEntry()
        } catch { case e: IOException => }
      }
    } finally out.close()
    f
  }

  it should "correctly transform path to traversable" in {
    val f = createTempFile("Foo", Some(".class"))
    f.path.filter(_.isClassFile) should have size 1

    val dir = createTempFile("directory").path
    (1 to 10).foreach { i =>
      val file = Files.createTempFile(dir, s"file$i", ".scala")
      file.toFile.deleteOnExit()
    }
    dir.filter(_.isSourceFile) should have size 10

    val zip = createZipFile("zipArchive")
    zip.filter(_.isClassFile) should have size 100
  }
}
