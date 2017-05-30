package org.scalamu.utils

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.scalamu.core.{ClassInfo, ClassName}
import org.scalamu.testutil.ScalamuSpec

class ASMUtilsSpec extends ScalamuSpec {
  "ASMUtils" should "return valid ClassInfo for Java SE classes" in {
    val classInfo = classInfoForName("java/util/Set.class")
    inside(classInfo) {
      case ClassInfo(
          ClassName("java.util.Set"),
          superclasses,
          annotations,
          false,
          false,
          true,
          _
          ) =>
        annotations shouldBe empty
        superclasses should contain theSameElementsAs Set(
          ClassName("java.util.Collection"),
          ClassName("java.lang.Object"),
          ClassName("java.lang.Iterable")
        )
    }
  }

  it should "return valid ClassInfo for Scala classes" in {
    val classInfo = classInfoForName("org/scalatest/FlatSpecLike.class")
    classInfo.annotations should contain allElementsOf Seq(
      ClassName("org.scalatest.Finders"),
      ClassName("scala.reflect.ScalaSignature")
    )
    classInfo.superClasses should contain(ClassName("org.scalatest.Suite"))
  }

  it should "collect method annotations" in {
    val annotatedMethodsInfo =
      classInfoForName("org/scalamu/utils/ASMUtilsSpec$AnnotatedMethods$.class")
    annotatedMethodsInfo.annotations should contain(ClassName("org.junit.Test"))
  }

  it should "collect annotations from superclasses, but not supertraits" in {
    val barInfo = classInfoForName("org/scalamu/utils/ASMUtilsSpec$Bar.class")
    barInfo.annotations should contain(ClassName("org.junit.Test"))

    val bazInfo = classInfoForName("org/scalamu/utils/ASMUtilsSpec$Baz.class")
    bazInfo.annotations should contain(ClassName("org.junit.Test"))
    bazInfo.annotations should not contain ClassName("org.junit.runner.RunWith")

    val quxInfo = classInfoForName("org/scalamu/utils/ASMUtilsSpec$Qux.class")
    quxInfo.annotations should contain(ClassName("org.junit.Test"))
  }

  @RunWith(classOf[Suite])
  private trait Trait
  @Test
  private class Foo
  private class Bar extends Foo
  private class Baz extends Foo with Trait
  private class Qux extends Bar with Trait
  private object AnnotatedMethods {
    @Test
    def foo(): Unit = ???
  }
}
