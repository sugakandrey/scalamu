package org.scalamu.utils

import org.scalamu.core.{ClassFileInfo, ClassName}
import org.scalamu.testutil.ScalamuSpec

class ASMUtilsSpec extends ScalamuSpec with ASMUtils {
  "ASMUtils" should "return valid ClassFileInfo for Java SE classes" in {
    val is = getClass.getClassLoader.getResourceAsStream("java/util/Set.class")
    val classFileInfo = loadClassFileInfo(is)
    classFileInfo should ===(ClassFileInfo(
      ClassName("java.util.Set"),
      Set(
        ClassName("java.util.Collection"),
        ClassName("java.lang.Object"),
        ClassName("java.lang.Iterable")
      ),
      Set.empty,
      isModule = false,
      Some("Set.java")
    ))
  }
  
  it should "return valid ClassFileInfo for Scala classes" in {
    val is = getClass.getClassLoader.getResourceAsStream("org/scalatest/FlatSpecLike.class")
    val classFileInfo = loadClassFileInfo(is)
    classFileInfo.annotations should contain allElementsOf Seq(
      ClassName("org.scalatest.Finders"),
      ClassName("scala.reflect.ScalaSignature")
    )
    classFileInfo.superClasses should contain (ClassName("org.scalatest.Suite"))
  }
}
