package org.scalamu.utils

import java.util

import org.scalamu.core.{ClassInfo, ClassName}
import org.scalamu.testutil.ScalamuSpec

class ASMUtilsSpec extends ScalamuSpec {
  "ASMUtils" should "return valid ClassInfo for Java SE classes" in {
    val is            = getClass.getClassLoader.getResourceAsStream("java/util/Set.class")
    val classFileInfo = loadClassFileInfo(is)
    classFileInfo.success.value should ===(
      ClassInfo(
        ClassName("java.util.Set"),
        Set(
          ClassName("java.util.Collection"),
          ClassName("java.lang.Object"),
          ClassName("java.lang.Iterable")
        ),
        Set.empty,
        isModule = false,
        hasNoArgConstructor = false,
        Some("Set.java")
      )
    )
  }

  it should "return valid ClassInfo for Scala classes" in {
    val is            = getClass.getClassLoader.getResourceAsStream("org/scalatest/FlatSpecLike.class")
    val classFileInfo = loadClassFileInfo(is)
    classFileInfo.success.value.annotations should contain allElementsOf Seq(
      ClassName("org.scalatest.Finders"),
      ClassName("scala.reflect.ScalaSignature")
    )
    classFileInfo.success.value.superClasses should contain(ClassName("org.scalatest.Suite"))
  }

  it should "collect class and method annotaitons" in {}
}
