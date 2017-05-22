/* ----------------- sse-breaker ----------------- *\
 * Licensed under the Apache License, Version 2.0. *
 * Author: Spiros Tzavellas                        *
\* ----------------------------------------------- */
package com.tzavellas.sse.breaker

import scala.concurrent.duration._

trait CircuitDriver {

  val executor: CircuitExecutor

  lazy val defaults = DefaultTestConfiguration

  def circuit: CircuitBreaker = executor.circuitBreaker
  def config: CircuitConfiguration = circuit.configuration

  def reconfigureWith(
    maxFailures: Int = defaults.maxFailures,
    openCircuitTimeout: Duration = defaults.openCircuitTimeout,
    failureCountTimeout: Duration = defaults.failureCountTimeout,
    maxMethodDuration: Duration = defaults.maxMethodDuration,
    isFailure: Throwable => Boolean = defaults.isFailure): Unit = {
    circuit.reconfigureWith(
      CircuitConfiguration(
        maxFailures,
        openCircuitTimeout,
        failureCountTimeout,
        maxMethodDuration,
        isFailure))
  }

  def normalOperation: Any
  def faultyOperation: Any

  def makeNormalCall(circuitIsOpen: Boolean = false): Any
  def makeCallWithNonLocalReturn(): Any
  def generateFaults(numOfFaults: Int): Unit

  def makeSlowCall(): Unit = {
    val previous = config.maxMethodDuration
    circuit.reconfigureWith(config.copy(maxMethodDuration = 1.nano))
    makeNormalCall()
    circuit.reconfigureWith(config.copy(maxMethodDuration = previous))
  }

  def generateFaultsToOpen(): Unit = generateFaults(config.maxFailures)
}