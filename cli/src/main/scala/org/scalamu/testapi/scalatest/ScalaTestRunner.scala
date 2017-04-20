package org.scalamu.testapi
package scalatest

import org.scalamu.core.ClassInfo
import org.scalatest._

import scala.util.{Failure, Try}

class ScalaTestRunner extends TestRunner[Status] {
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
                  s"must declare a constructor taking Class[_]."
              )
            )
        }
    }

  override def run(info: ClassInfo): TestSuiteResult = {
    val suite            = info.name
    val tryLoadTestClass = suite.loadFromContextClassLoader
    val suiteClass       = tryLoadTestClass.flatMap(resolveRunnerClass)
    suiteClass.fold(
      TestSuiteResult.Aborted(suite, _),
      converter.fromResult(suite) _ compose { _.run(None, Args(converter.reporter)) }
    )
  }
}
