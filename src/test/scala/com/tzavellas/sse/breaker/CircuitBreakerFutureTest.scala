/* ----------------- sse-breaker ----------------- *\
 * Licensed under the Apache License, Version 2.0. *
 * Author: Spiros Tzavellas                        *
\* ----------------------------------------------- */
package com.tzavellas.sse.breaker

import org.junit.Test
import scala.util.Success
import scala.util.Failure
import scala.concurrent.Future

class CircuitBreakerFutureTest extends AbstractCircuitBreakerTest with CircuitDriver {

  @Test
  def dummy_test(): Unit = { }

  // -- CircuitDriver implementation ------------------------------------------

  def makeNormalCall(circuitIsOpen: Boolean = false) = {
    val f = executor { normalOperation }
    f.value.get match {
      case Success(i)                  => i
      case Failure(e) if circuitIsOpen => throw e
      case Failure(e)                  => throw new AssertionError("Unexpected exception!", e)
    }
  }

  def makeCallWithNonLocalReturn(): Any = executor { return Future.successful(43) }

  def generateFaults(numOfFaults: Int): Unit =
    for (_ <- 0 until numOfFaults) executor { faultyOperation }

  def normalOperation: Future[Int] = Future.successful(42)
  def faultyOperation: Future[Int] = throw new IllegalStateException
}