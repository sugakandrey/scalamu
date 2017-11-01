package org.scalamu.core.utils

import java.io.IOException
import java.nio.file.{Files, Path, StandardOpenOption}

import org.scalamu.testutil.ScalamuSpec

class FileSystemUtilsSpec extends ScalamuSpec {
  "FileSystemUtils" should "correctly identify .jar and .zip files" in {
    val isJarOrZip = Seq(
      createTempFile("aJar", ".jar"),
      createTempFile("almostAJar", ".jar"),
      createTempFile("namingIsHard", ".zip")
    )
    forAll(isJarOrZip)(_ should be a 'jarOrZip)

    val notJarOrZip = Seq(
      createTempDirectory("dir"),
      createTempFile("notAJar", ".jarr"),
      createTempFile("archive", ".rar")
    )
    forAll(notJarOrZip)(_ should not be 'jarOrZip)
  }

  it should "correctly identify directories" in {
    val directories = Seq(
      createTempDirectory("aDirectory"),
      createTempDirectory("directory99"),
      createTempDirectory("1233")
    )
    forAll(directories)(_ should be a 'directory)

    val notADirectory = Seq(
      createTempFile("temp", ".tmp"),
      createTempFile("someFile", ".ext")
    )
    forAll(notADirectory)(_ should not be 'directory)
  }

  it should "correctly identify .class files" in {
    val classFiles = Seq(
      createTempFile("Foo", ".class"),
      createTempFile("SomeObject4", ".class"),
      createTempFile("org.spring.AbstractBeanPostProcessor", ".class")
    )
    forAll(classFiles)(_ should be a 'classFile)

    val notAClassFile = Seq(
      createTempDirectory("dir"),
      createTempFile("jarFile", ".jar"),
      createTempFile("sourceFile", ".scala")
    )
    forAll(notAClassFile)(_ should not be 'classFile)
  }

  it should "correctly identify .scala files" in {
    val isJarOrZip = Seq(
      createTempFile("foo.bar.Baz", ".scala"),
      createTempFile("Unit", ".scala"),
      createTempFile("AlNum123", ".scala")
    )
    forAll(isJarOrZip)(_ should be a 'sourceFile)

    val notASourceFile = Seq(
      createTempDirectory("dir"),
      createTempFile("JavaSource", ".java"),
      createTempFile("NotASource", ".scal")
    )
    forAll(notASourceFile)(_ should not be 'sourceFile)
  }

  private def createZipFile(name: String): Path = {
    val f = createTempFile(name, ".zip").path
    import java.util.zip.{ZipEntry, ZipOutputStream}

    val out = new ZipOutputStream(Files.newOutputStream(f, StandardOpenOption.WRITE))
    val buf = Array.ofDim[Byte](1024)

    try {
      (1 to 100).foreach { i =>
        val file = createTempFile(s"file$i", ".class").path
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
    val f = createTempFile("Foo", ".class")
    f.path.filter(_.isClassFile) should have size 1

    val dir = createTempDirectory("directory").path
    (1 to 10).foreach { i =>
      val file = Files.createTempFile(dir, s"file$i", ".scala")
      file.toFile.deleteOnExit()
    }
    dir.filter(_.isSourceFile) should have size 10

    val zip = createZipFile("zipArchive")
    zip.filter(_.isClassFile) should have size 100
  }
}
