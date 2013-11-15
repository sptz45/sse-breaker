package com.tzavellas.sse.breaker

import scala.util.Success
import scala.util.Failure
import scala.concurrent.ExecutionContext

trait AsynchronousCircuitDriver extends CircuitDriver {

  implicit def testExecutor = CircuitExecutor.currentThreadExecutor

  def makeNormalCall(circuitIsOpen: Boolean = false) = {
    val f = executor.async(normalOperation)
    f.value.get match {
      case Success(i)                    => i
      case Failure(e) if (circuitIsOpen) => throw e
      case Failure(e)                    => throw new AssertionError("Unexpected exception!", e)
    }
  }

  def makeCallWithNonLocalReturn(): Int = {
    val f = executor.async { return 43 }
    f.value.get match {
      case Success(i) => i
      case Failure(e) => throw new AssertionError("Unexpected exception!", e)
    }
  }

  def generateFaults(numOfFaults: Int) {
    for (i <- 0 until numOfFaults) executor.async(faultyOperation)
  }

  def normalOperation = 42
  def faultyOperation = throw new IllegalStateException
}