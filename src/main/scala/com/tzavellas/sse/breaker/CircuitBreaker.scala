/* ----------------- sse-breaker ----------------- *\
 * Licensed under the Apache License, Version 2.0. *
 * Author: Spiros Tzavellas                        *
\* ----------------------------------------------- */
package com.tzavellas.sse.breaker

import java.util.concurrent.atomic.{AtomicInteger, AtomicLong}

/**
 * Holds the state of a circuit-breaker.
 *
 * <p>Instances of this class are thread-safe.</p>
 * 
 * @see CircuitExecutor
 */
class CircuitBreaker(
  val name: String,
  initConf: CircuitConfiguration,
  listener: CircuitStateChangeListener) {
  
  @volatile
  private[this] var conf = initConf
  
  private[this] val currentFailures = new AtomicInteger
  private[this] val openTimestamp = new AtomicLong
  private[this] val firstCurrentFailureTimestamp = new AtomicLong

  private[this] val calls = new AtomicInteger
  private[this] val failures = new AtomicInteger
  private[this] val timesOpened = new AtomicInteger
  
  private[breaker] def recordCall() {
    calls.incrementAndGet()
  }
  
  private[breaker] def recordFailure() {
    failures.incrementAndGet()
    initFirstFailureTimeStampIfNeeded()
    var tmpCurrentFailures = 0
    if (conf.failureCountTimeout.hasPastSince(firstCurrentFailureTimestamp.get)) {
      resetFailures()
      tmpCurrentFailures = 1
    } else {
      tmpCurrentFailures = currentFailures.incrementAndGet()
    }
    if (tmpCurrentFailures >= conf.maxFailures)
        open()
  }
  
  private def resetFailures() {
    currentFailures.set(1)
    firstCurrentFailureTimestamp.set(System.nanoTime)
  }
  
  private def initFirstFailureTimeStampIfNeeded() {
    firstCurrentFailureTimestamp.compareAndSet(0, System.nanoTime)
  }

  def isClosed = !isOpen
  
  def isOpen = currentFailures.get >= conf.maxFailures && !isHalfOpen
  
  def isHalfOpen = {
    val timestampt = openTimestamp.get 
    timestampt != 0 && timestampt + conf.openCircuitTimeout.toMillis <= System.currentTimeMillis
  }
  
  def close() {
    currentFailures.set(0)
    openTimestamp.set(0)
    listener.onClose(this)
  }
  
  def open() {
    timesOpened.incrementAndGet()
    openTimestamp.set(System.currentTimeMillis)  
    currentFailures.set(conf.maxFailures)
    listener.onOpen(this)
  }

  def configuration = conf
  
  def reconfigure(newConf: CircuitConfiguration) {
    conf = newConf
  }
  
  def openedTimestamp = openTimestamp.get
  
  def numberOfCurrentFailures = currentFailures.get
  def numberOfFailedOperations = failures.get
  def numberOfOperations = calls.get
  def numberOfTimesOpened = timesOpened.get
  
  def resetStatistics() {
    failures.set(0)
    calls.set(0)
    timesOpened.set(0)
  }
}
