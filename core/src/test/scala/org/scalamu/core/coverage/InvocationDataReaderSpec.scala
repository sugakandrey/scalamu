package org.scalamu.core.coverage

import java.nio.charset.{Charset, StandardCharsets}

import org.scalamu.testutil.ScalamuSpec

class InvocationDataReaderSpec extends ScalamuSpec {
  private implicit val charset: Charset = StandardCharsets.UTF_8

  "InvocationDataReader" should "read instrumentation statement invocation data from files" in {
    val dir                 = createTempDirectory("invocationDir")
    val invocationDataFile1 = createTempFile("scoverage.measurements.", ".file1", dir.path)
    val invocationDataFile2 = createTempFile("scoverage.measurements.", ".file2", dir.path)
    val reader              = new InvocationDataReader(dir.path)
    
    invocationDataFile1.writeLines(List("1", "2", "3"))
    invocationDataFile2.writeLines(List("4", "5", "6", "10", "10000"))
    reader.invokedStatements() should ===(Set(1, 2, 3, 4, 5, 6, 10, 10000))
    invocationDataFile1.readLines shouldBe empty
    invocationDataFile2.readLines shouldBe empty
  }

  it should "be able to clean invocation data in case test suite failed" in {
    val dir                 = createTempDirectory("invocationDir")
    val invocationDataFile1 = createTempFile("scoverage.measurements.", ".file1", dir.path)
    val invocationDataFile2 = createTempFile("scoverage.measurements.", ".file2", dir.path)
    val reader              = new InvocationDataReader(dir.path)
    
    invocationDataFile1.writeLines(List("1", "2", "3"))
    invocationDataFile2.writeLines(List("9", "8", "7"))
    reader.clearData()
    reader.invokedStatements() shouldBe empty
  }
}
