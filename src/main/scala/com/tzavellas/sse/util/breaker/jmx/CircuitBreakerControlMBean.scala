/* ----------------- sse-breaker ----------------- *\
 * Licensed under the Apache License, Version 2.0. *
 * Author: Spiros Tzavellas                        *
\* ----------------------------------------------- */
package com.tzavellas.sse.util.breaker.jmx

import java.util.Date

trait CircuitBreakerControlMBean {
  
  def getName: String
  
  def getMaxFailures: Int
  def setMaxFailures(max: Int)
  
  def getOpenCircuitTimeout: String
  def setOpenCircuitTimeout(timeout: String)
  
  def getFailureCountTimeout: String
  def setFailureCountTimeout(timeout: String)
  
  def getOpenedTimestamp: Date 
  def getCurrentFailures: Int
  def getFailedOperations: Int
  def getTotalOperations: Int
  def getNumberOfTimesOpened: Int
  
  def getMaxMethodDuration: String
  def setMaxMethodDuration(duration: String)
  
  def getIgnoredExceptions: String
  def ignoreException(exceptionClass: String)
  def stopIgnoringException(exceptionClass: String)
}