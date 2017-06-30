package org.scalamu.testapi
package specs2

import org.scalamu.core.ClassName
import org.scalamu.utils.ClassLoadingUtils
import org.specs2.control
import org.specs2.main.ArgumentsShortcuts
import org.specs2.reporter.NotifierPrinter
import org.specs2.runner.Runner
import org.specs2.specification.core.{Env, SpecificationStructure}
import org.specs2.specification.process.Stats

import scala.util.Try

final case class InternalSpecs2Error(message: String) extends RuntimeException(message)

class Specs2Runner extends TestRunner[Stats] {
  private val notifier = new Specs2Notifier
  override protected val converter: SuiteResultTypeConverter[Stats] = new Specs2Converters(
    notifier
  )

  override def run(suite: ClassName): TestSuiteResult = {
    val suiteClass = suite.loadFromContextClassLoader
    val spec       = suiteClass.flatMap(cl => Try(cl.newInstance().asInstanceOf[SpecificationStructure]))

    val suiteResult = spec.map { s =>
      val arguments = ArgumentsShortcuts.stopOnFail
      val env       = Env(arguments)

      val errorOrStats = control.runAction(
        Runner.runSpecStructure(
          s.structure(env),
          env,
          ClassLoadingUtils.contextClassLoader,
          List(NotifierPrinter.printer(notifier))
        )
      )

      errorOrStats.fold(
        err =>
          err.fold(
            SuiteExecutionAborted(suite, _),
            description => SuiteExecutionAborted(suite, InternalSpecs2Error(description))
        ),
        converter.fromResult(suite)
      )
    }

    suiteResult.fold(
      SuiteExecutionAborted(suite, _),
      identity
    )
  }
}
