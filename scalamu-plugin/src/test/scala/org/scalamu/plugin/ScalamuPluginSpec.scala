package org.scalamu.plugin

import org.scalamu.plugin.fixtures.SharedScalamuCompilerFixture
import org.scalamu.plugin.mutations.{NegateConditionals, RemoveUnitMethodCalls}
import org.scalatest.FlatSpec
import org.scalatest.Matchers._

import scala.tools.nsc.Settings
import scala.tools.nsc.reporters.ConsoleReporter

/**
  * Created by sugakandrey.
  */
class ScalamuPluginSpec extends FlatSpec with PluginRunner with SharedScalamuCompilerFixture {

  override val settings = new Settings() {
    stopAfter.value = List("mutating-transform")
    usejavacp.value = true
  }
  override val reporter                 = new ConsoleReporter(settings)
  override val mutationReporter         = new TestingReporter()
  override val mutations: Seq[Mutation] = ScalamuConfig.allMutations

  "ScalamuCompiler" should "introduce mutations" in withScalamuCompiler { implicit global =>
    val script =
      """
        |object Foo {
        | val a = 123
        | val b = 123
        | if (a < b) {
        |   println(a == b)
        | }
        |}
        """.stripMargin
    val runId                    = compile(script)
    val mutationsInfo            = mutationReporter.mutationsForRunId(runId)
    val mutations: Seq[Mutation] = mutationsInfo.map(_.mutation)(collection.breakOut)
    mutations should contain theSameElementsAs Seq(
      NegateConditionals,
      NegateConditionals,
      RemoveUnitMethodCalls
    )

  }
}
