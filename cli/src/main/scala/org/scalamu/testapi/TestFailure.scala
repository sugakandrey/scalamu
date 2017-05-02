package org.scalamu.testapi

/**
 * Represents a single failed test in a suite.
 * @param description In most cases - the name of the failed test.
 * @param errorMessage Exception thrown.
 */
final case class TestFailure(description: String, errorMessage: Option[String] = None)
