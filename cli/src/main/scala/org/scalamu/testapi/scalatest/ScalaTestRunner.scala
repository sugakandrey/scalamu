package org.scalamu.testapi
package scalatest

import org.scalamu.common.TryBackCompatibility
import org.scalamu.core.ClassName
import org.scalatest._
import org.scalatest.tools.ScalaTestInteractionLayer

import scala.util.{Failure, Try}

class ScalaTestRunner(override val arguments: String) extends TestRunner[Status] with TryBackCompatibility {

  override protected val converter: ScalaTestConverters = new ScalaTestConverters

  private def resolveRunnerClass(testClass: Class[_]): Try[Suite] =
    testClass.getAnnotation(classOf[WrapWith]) match {
      case null => Try(testClass.newInstance().asInstanceOf[Suite])
      case ann =>
        val suiteClass      = ann.value()
        val constructorList = suiteClass.getDeclaredConstructors
        val constructor = constructorList.find { c =>
          val types = c.getParameterTypes
          types.length == 1 && types(0) == classOf[java.lang.Class[_]]
        }
        constructor match {
          case Some(cons) => Try(cons.newInstance(testClass).asInstanceOf[Suite])
          case _ =>
            Failure(
              new IllegalArgumentException(
                s"Class specified in the value of @WrapWith " +
                  s"must extend org.scalatest.Suite and declare a constructor taking Class[_]."
              )
            )
        }
    }

  override def run(suite: ClassName): TestSuiteResult = {
    val tryLoadTestClass = suite.loadFromContextClassLoader
    val suiteClass       = tryLoadTestClass.flatMap(resolveRunnerClass)

    val parseArgs   = ScalaTestInteractionLayer.parseArgumentsString(arguments)
    val defaultArgs = Args(converter.reporter, converter.stopper)

    val args = parseArgs.right.map {
      case ScalaTestArgs(configMap, include, exclude, scale) =>
        val filter = Filter(if (include.isEmpty) None else Some(include), exclude)
        ScalaTestInteractionLayer.setSpanScaleFactor(scale)

        Args(
          converter.reporter,
          converter.stopper,
          filter,
          configMap
        )
    }.right.getOrElse(defaultArgs)

    suiteClass.fold(
      SuiteExecutionAborted(suite, _),
      converter.fromResult(suite) _ compose { _.run(None, args) }
    )
  }
}
