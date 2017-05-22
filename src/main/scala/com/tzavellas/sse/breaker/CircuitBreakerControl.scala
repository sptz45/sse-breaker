/* ----------------- sse-breaker ----------------- *\
 * Licensed under the Apache License, Version 2.0. *
 * Author: Spiros Tzavellas                        *
\* ----------------------------------------------- */
package com.tzavellas.sse.breaker

import java.util.Date
import scala.concurrent.duration.Duration

private class CircuitBreakerControl(circuit: CircuitBreaker) extends CircuitBreakerControlMBean {

  def config: CircuitConfiguration = circuit.configuration
  
  def getName: String = circuit.name
  
  def isOpen: Boolean = circuit.isOpen
  def open(): Unit = circuit.open()
  def close(): Unit = circuit.close()
  
  def getMaxFailures: Int = config.maxFailures
  def setMaxFailures(max: Int): Unit = {
    circuit.reconfigureWith(config.copy(maxFailures=max))
  }
  
  def getOpenCircuitTimeout: String = config.openCircuitTimeout.toString
  def setOpenCircuitTimeout(timeout: String): Unit = {
    circuit.reconfigureWith(config.copy(openCircuitTimeout=Duration(timeout)))
  }
  
  def getFailureCountTimeout: String = config.failureCountTimeout.toString
  def setFailureCountTimeout(timeout: String): Unit = {
    circuit.reconfigureWith(config.copy(failureCountTimeout=Duration(timeout)))
  }
  
  def getOpenedTimestamp = new Date(circuit.openedTimestamp)
  
  def getCurrentFailures: Int = circuit.numberOfCurrentFailures
  def getFailedOperations: Int = circuit.numberOfFailedOperations
  def getTotalOperations: Int = circuit.numberOfOperations
  def getNumberOfTimesOpened: Int = circuit.numberOfTimesOpened
  
  def resetStatistics(): Unit = circuit.resetStatistics()

  def getMaxMethodDuration: String = config.maxMethodDuration.toString
  def setMaxMethodDuration(duration: String): Unit = {
    circuit.reconfigureWith(config.copy(maxMethodDuration = Duration(duration)))
  }
}