package org.scalamu.plugin

import org.scalamu.plugin.fixtures.IsolatedScalamuCompilerFixture
import org.scalamu.plugin.testutil.TestRunner

class ScalamuPluginSpec extends TestRunner with IsolatedScalamuCompilerFixture {
  override val mutations: Seq[Mutation] = ScalamuConfig.allMutations
  override val guard: MutationGuard     = FqnPrefixedGuard(ScalamuConfig.mutationGuardPrefix)
  override val sanitizeTrees: Boolean   = true
  override val verifyTrees: Boolean     = true
  override val filter: RegexBasedFilter = RegexBasedFilter(".*ignored.*".r)

  private val guards =
    s"""
       |package ${ScalamuConfig.mutationGuardPrefix}
       |
       |object FooGuard {
       |  val enabledMutation: Int = 1
       |}
    """.stripMargin

  "ScalamuPlugin" should "insert all possible mutants" in withScalamuCompiler { (global, config) =>
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
    val mutantsInfo = mutantsFor(NamedSnippet("Foo.scala", code))(global, config.reporter)
    mutantsInfo should have size 26
  }

  it should "ignore macro bodies" in withScalamuCompiler { (global, config) =>
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
    val mutantsInfo = mutantsFor(NamedSnippet("Macro.scala", code))(global, config.reporter)
    mutantsInfo shouldBe empty
  }

  it should "ignore macro expansions" in withScalamuCompiler { (global, config) =>
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
        |  val c = true
        |  val a = test(c)
        |}
      """.stripMargin
    compile(
      NamedSnippet("Macro.scala", macroDef),
      NamedSnippet("Guards.scala", guards)
    )(global)
    val mutantsInfo = mutantsFor(NamedSnippet("Foo.scala", code))(global, config.reporter)
    mutantsInfo shouldBe empty
  }
}
