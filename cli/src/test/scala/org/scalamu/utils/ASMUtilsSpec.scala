package org.scalamu.utils

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.scalamu.core.{ClassInfo, ClassName}
import org.scalamu.testutil.ScalamuSpec

class ASMUtilsSpec extends ScalamuSpec {
  private def classInfoForName(name: String)(implicit pos: org.scalactic.source.Position): ClassInfo =
    loadClassFileInfo(getClass.getClassLoader.getResourceAsStream(name)).success.value

  "ASMUtils" should "return valid ClassInfo for Java SE classes" in {
    val classInfo = classInfoForName("java/util/Set.class")
    classInfo should ===(
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
    val classInfo = classInfoForName("org/scalatest/FlatSpecLike.class")
    classInfo.annotations should contain allElementsOf Seq(
      ClassName("org.scalatest.Finders"),
      ClassName("scala.reflect.ScalaSignature")
    )
    classInfo.superClasses should contain(ClassName("org.scalatest.Suite"))
  }

  it should "collect method annotations" in {
    val annotatedMethodsInfo = classInfoForName("org/scalamu/utils/ASMUtilsSpec$AnnotatedMethods$.class")
    annotatedMethodsInfo.annotations should contain (ClassName("org.junit.Test"))
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
