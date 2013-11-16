/* ----------------- sse-breaker ----------------- *\
 * Licensed under the Apache License, Version 2.0. *
 * Author: Spiros Tzavellas                        *
\* ----------------------------------------------- */
package com.tzavellas.sse.breaker

trait SynchronousCircuitDriver extends CircuitDriver {

  def makeNormalCall(circuitIsOpen: Boolean = false) = {
    try {
      executor(normalOperation)
    } catch {
      case e: OpenCircuitException =>
        if (circuitIsOpen) throw e
        else throw new AssertionError("Unexpected OpenCircuitException!", e)
    }
  }

  def makeCallWithNonLocalReturn(): Int = executor { return 43 }

  def generateFaults(numOfFaults: Int) {
    for (i <- 0 until numOfFaults)
      try executor(faultyOperation) catch { case _: Exception => () }
  }

  def normalOperation = 42

  def faultyOperation = throw new IllegalStateException
}