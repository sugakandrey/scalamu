package org.scalamu.testapi

/**
 * Represents a single failed test in a suite.
 * @param description In most cases - the name of the failed test. 
 * @param throwable Exception thrown.
 */
final case class TestFailure(description: String, throwable: Option[Throwable])
