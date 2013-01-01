/* ----------------- sse-breaker ----------------- *\
 * Licensed under the Apache License, Version 2.0. *
 * Author: Spiros Tzavellas                        *
\* ----------------------------------------------- */
package com.tzavellas.sse.breaker

import java.util.Date

private trait CircuitBreakerControlMBean {
  
  def getName: String
  
  def isOpen: Boolean
  def open()
  def close()
  
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
  
  def resetStatistics()
  
  def getMaxMethodDuration: String
  def setMaxMethodDuration(duration: String)
}