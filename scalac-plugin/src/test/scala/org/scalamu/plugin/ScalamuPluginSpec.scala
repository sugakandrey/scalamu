package org.scalamu.plugin

import org.scalamu.common.filtering.{InverseRegexFilter, NameFilter}
import org.scalamu.plugin.fixtures.IsolatedScalamuCompilerFixture
import org.scalamu.plugin.mutators.controllflow.{NegateConditionals, NeverExecuteConditionals, ReplaceCaseWithWildcard}
import org.scalamu.plugin.mutators.methodcalls.ReplaceWithIdentityFunction
import org.scalamu.plugin.testutil.MutationTestRunner

class ScalamuPluginSpec extends MutationTestRunner with IsolatedScalamuCompilerFixture {
  override val guard: MutationGuard = FqnGuard(
    s"${ScalamuPluginConfig.mutationGuardPrefix}.FooGuard.enabledMutation"
  )

  override val mutators: Seq[Mutator] = ScalamuPluginConfig.allMutators
  override val sanitizeTrees: Boolean = true
  override val verifyTrees: Boolean   = true
  override val filter: NameFilter     = InverseRegexFilter(".*ignored.*".r)

  private val guards =
    s"""
       |package ${ScalamuPluginConfig.mutationGuardPrefix}
       |
       |object FooGuard {
       |  def enabledMutation: Int = 1
       |}
    """.stripMargin

  "ScalamuPlugin" should "insert all possible mutants" in withScalamuCompiler { (global, reporter) =>
    val code =
      """
        |object Foo {
        |  val xs = List(1, 2, 3)
        |  val length = xs match {
        |    case _ if xs.isEmpty => 0
        |    case named @ _       => -10
        |  }
        |
        |  if (xs.forall(_ + 1 > 0)) {
        |    println("length = " + length)
        |  }
        |
        |  val x = 123
        |  val y = 456d
        |
        |  val z = (x * y + x / y) - -y
        |
        |  (1 until 100 by 10).map(x => List(x))
        |
        |  type Maybe[T] = Option[T]
        |  def toOption[T](x: T): Maybe[T] = Option(x)
        |
        |  val t = true
        |  val f = t && false
        |
        |  def ignored(): Unit = println("I am ignored")
        |
        |  (xs.toSet | Set(4)).foreach(x => ignored())
        |}
      """.stripMargin
    compile(NamedSnippet("Guards.scala", guards))(global)
    val mutantsInfo = mutantsFor(NamedSnippet("Foo.scala", code))(global, reporter)
    mutantsInfo should have size 41
  }

  it should "ignore macro bodies" in withScalamuCompiler { (global, reporter) =>
    val code =
      """
        |object Macro {
        |
        |  import scala.language.experimental.macros
        |  import scala.reflect.macros.blackbox
        |
        |  def test: Int = macro testImpl
        |
        |  def testImpl(c: blackbox.Context): c.Tree = {
        |    import c.universe._
        |    val a = 10
        |    val expr = if (a >= 1) a * -100 else -a
        |    q"$expr"
        |  }
        |}
      """.stripMargin
    val mutantsInfo = mutantsFor(NamedSnippet("Macro.scala", code))(global, reporter)
    mutantsInfo shouldBe empty
  }

  it should "ignore macro expansions" in withScalamuCompiler { (global, reporter) =>
    val macroDef =
      """
        |object Macro {
        |
        |  import scala.language.experimental.macros
        |  import scala.reflect.macros.blackbox
        |
        |  def test(cond: Boolean): Int = macro testImpl
        |
        |  def testImpl(c: blackbox.Context)(cond: c.Expr[Boolean]): c.Tree = {
        |    import c.universe._
        |    q"123 + (if ($cond) -10 else 71)"
        |  }
        |}
      """.stripMargin
    val code =
      """
        |object Foo {
        |  import Macro._
        |  val a = test(1 > 10)
        |}
      """.stripMargin
    compile(
      NamedSnippet("Macro.scala", macroDef),
      NamedSnippet("Guards.scala", guards)
    )(global)
    val mutantsInfo = mutantsFor(NamedSnippet("Foo.scala", code))(global, reporter)
    mutantsInfo shouldBe empty
  }

  it should "work when arrays are involved" in withPluginConfig { cfg =>
    withScalamuCompiler(Seq(NegateConditionals), cfg) { (global, _) =>
      val code =
        """
          |object Foo {
          |  val a = 10
          |
          |  def foo(): Unit = {
          |    val lengths = Array(10)
          |    if (lengths.apply(10) == 10) {
          |       ???
          |    }
          |  }
          |}
        """.stripMargin
      compile(
        NamedSnippet("Guards.scala", guards),
        NamedSnippet("Foo.scala", code)
      )(global)
    }
  }

  it should "test case where NeverExecuteConditionals fail on LambdaLift" in withPluginConfig { cfg =>
    withScalamuCompiler(Seq(NeverExecuteConditionals), cfg) { (global, _) =>
      val code =
        """
          |object Foo {
          |  type Occurrence = (Int, Int)
          |  private case class Stacked(idx1: Int, idx2: Int, next: Option[Stacked]) {
          |    lazy val chain: List[(Int, Int)] = ???
          |  }
          |
          |  val l: List[(Occurrence, Int)] = ???
          |  if (l.length > 0) {
          |    Nil
          |  } else {
          |    def sort(l: List[(Occurrence, Int)]): List[List[Stacked]] =
          |      l.foldLeft(List[List[Stacked]]()) {
          |        case (acc, ((_, idx1), idx2)) => acc
          |      }
          |    Nil
          |  }
          |}
        """.stripMargin
      compile(
        NamedSnippet("Guards.scala", guards),
        NamedSnippet("Foo.scala", code)
      )(global)
    }
  }

  it should "correctly work when splicing original tree changes its owner" in withPluginConfig { cfg =>
    withScalamuCompiler(Seq(ReplaceWithIdentityFunction), cfg) { (global, _) =>
      val code =
        """
          |object Foo {
          |  Some(1).filter { v => val f = v; v > 0 }.filter { v => val f = v; v > 0 }.filter { v => val f = v; v > 0 }
          |}
        """.stripMargin
      compile(
        NamedSnippet("Guards.scala", guards),
        NamedSnippet("Foo.scala", code)
      )(global)
    }
  }

  it should "not fail when using GuardedMutant.unapply" in withScalamuCompiler { (global, _) =>
    val code =
      """
        |object Foo {
        |  val x = 1
        |  if (1 == x) {
        |    println(123)
        |  }
        |}
      """.stripMargin
    compile(
      NamedSnippet("Guards.scala", guards),
      NamedSnippet("Foo.scala", code)
    )(global)
  }

  it should "not mutate case patterns" in withScalamuCompiler { (global, _) =>
    val code =
      """
        |object Foo {
        | val x = 123
        | 
        | object Bar { def unapply(b: Int): Option[Boolean] = Some(true) }
        | 
        | val y = x match {
        |   case -1 => 1
        |   case _  => 2
        | }
        |}
      """.stripMargin
    compile(
      NamedSnippet("Guards.scala", guards),
      NamedSnippet("Foo.scala", code)
    )(global)
  }

  it should "work with anonymous classes" in withPluginConfig { cfg =>
    withScalamuCompiler(Seq(ReplaceCaseWithWildcard), cfg) { (global, r) =>
      val code =
        """
          |object Foo {
          |  val x = 123
          |  
          |  x match {
          |    case 0 => ???
          |    case _ => val v = new Runnable { def run(): Unit = println(123) }
          |  }
          |}
        """.stripMargin
      compile(
        NamedSnippet("Guards.scala", guards),
        NamedSnippet("Foo.scala", code)
      )(global)
    }
  }
}
