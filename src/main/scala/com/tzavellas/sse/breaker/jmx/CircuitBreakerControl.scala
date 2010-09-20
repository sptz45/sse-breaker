/* ----------------- sse-breaker ----------------- *\
 * Licensed under the Apache License, Version 2.0. *
 * Author: Spiros Tzavellas                        *
\* ----------------------------------------------- */
package com.tzavellas.sse.breaker
package jmx

import java.util.Date

class CircuitBreakerControl(val executor: CircuitExecutor)
  extends CircuitBreakerControlMBean {

  def circuit = executor.circuitBreaker
  def config = circuit.configuration
  
  def getName = circuit.name
  
  def isOpen = circuit.isOpen
  def open()  { circuit.open()  }
  def close() { circuit.close() }
  
  def getMaxFailures = config.maxFailures
  def setMaxFailures(max: Int) {
    circuit.reconfigureWith(config.copy(maxFailures=max))
  }
  
  def getOpenCircuitTimeout = config.openCircuitTimeout.toString
  def setOpenCircuitTimeout(timeout: String) {
    circuit.reconfigureWith(config.copy(openCircuitTimeout=Duration.valueOf(timeout)))
    
  }
  
  def getFailureCountTimeout = config.failureCountTimeout.toString
  def setFailureCountTimeout(timeout: String) {
    circuit.reconfigureWith(config.copy(failureCountTimeout=Duration.valueOf(timeout)))
  }
  
  def getOpenedTimestamp = new Date(circuit.openedTimestamp)
  
  def getCurrentFailures = circuit.numberOfCurrentFailures
  def getFailedOperations = circuit.numberOfFailedOperations
  def getTotalOperations = circuit.numberOfOperations
  def getNumberOfTimesOpened = circuit.numberOfTimesOpened
  
  def resetStatistics() {
    circuit.resetStatistics()
  }

  def getMaxMethodDuration = executor.maxMethodDuration.toString
  def setMaxMethodDuration(duration: String) {
    executor.maxMethodDuration = Duration.valueOf(duration)
  }
  
  def getIgnoredExceptions = executor.ignoredExceptionsSeq.map(_.getName).mkString("\n") 
  
  def ignoreException(exceptionClass: String) {
    val eclass = Class.forName(exceptionClass).asInstanceOf[Class[Exception]]
    executor.ignoreException(eclass)
  }
  
  def stopIgnoringException(exceptionClass: String) {
    val eclass = Class.forName(exceptionClass).asInstanceOf[Class[Exception]]
    executor.stopIgnoringException(eclass)
  }
}