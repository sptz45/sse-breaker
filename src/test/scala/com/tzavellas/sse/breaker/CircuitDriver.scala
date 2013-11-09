/* ----------------- sse-breaker ----------------- *\
 * Licensed under the Apache License, Version 2.0. *
 * Author: Spiros Tzavellas                        *
\* ----------------------------------------------- */
package com.tzavellas.sse.breaker

import scala.concurrent.duration._

trait CircuitDriver {
  
  val defaults: CircuitConfiguration
  val executor: CircuitExecutor
  
  def circuit = executor.circuitBreaker
  def config  = circuit.configuration
  
  
  def reconfigureWith(
    maxFailures: Int = defaults.maxFailures,
    openCircuitTimeout: Duration = defaults.openCircuitTimeout,
    failureCountTimeout: Duration = defaults.failureCountTimeout) {
    circuit.reconfigureWith(
      new CircuitConfiguration(
        maxFailures,
        openCircuitTimeout,
        failureCountTimeout))
  }
  
  def makeNormalCall(circuitIsOpen: Boolean = false) = {
    try {
      executor(normalOperation)
    } catch {
      case e: OpenCircuitException => 
        if (circuitIsOpen) throw e
        else throw new AssertionError("Unexpected OpenCircuitException!", e)
    }
  }
  
  def makeSlowCall() {
    val previous = config.maxMethodDuration
    circuit.reconfigureWith(config.copy(maxMethodDuration = 1 nano))
    makeNormalCall()
    circuit.reconfigureWith(config.copy(maxMethodDuration = previous))
  }
  
  def generateFaultsToOpen() {
    generateFaults(config.maxFailures)
  }
  
  def generateFaults(numOfFaults: Int) {
    for (i <- 0 until numOfFaults)
      try executor(faultyOperation) catch { case _: Exception => () }
  }
  
  def normalOperation = 42 
  def faultyOperation = throw new IllegalStateException
  
  def makeCallWithNonLocalReturn(): Int = executor { return 43 }
}