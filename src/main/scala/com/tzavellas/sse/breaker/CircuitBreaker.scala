/* ----------------- sse-breaker ----------------- *\
 * Licensed under the Apache License, Version 2.0. *
 * Author: Spiros Tzavellas                        *
\* ----------------------------------------------- */
package com.tzavellas.sse.breaker

import java.util.concurrent.atomic.{AtomicInteger, AtomicLong}

/**
 * Holds the state of a circuit-breaker.

 * <p>Instances of this class are thread-safe.</p>
 * 
 * @param name       the name of the circuit-breaker (used in the JMX
 *                   {@code ObjectName}, can be used for logging, etc).
 * @param initConfig the initial configuration of the circuit-breaker.
 * @param listener   observes the state changes of this circuit-breaker.
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
  
  private[breaker] def recordExecutionTime(nanos: Long) {
    if (nanos > conf.maxMethodDuration.toNanos) {
      recordFailure(new SlowMethodExecutionException(conf.maxMethodDuration))
    }
    else if (isHalfOpen) {
      close()
    }
  }
  
  private [breaker] def recordException(exception: Exception) {
    if (conf.isFailure(exception)) {
      recordFailure(exception)
    }
  }
  
  private def recordFailure(failure: Exception) {
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
        open(failure)
  }
  
  private def resetFailures() {
    currentFailures.set(1)
    firstCurrentFailureTimestamp.set(System.nanoTime)
  }
  
  private def initFirstFailureTimeStampIfNeeded() {
    firstCurrentFailureTimestamp.compareAndSet(0, System.nanoTime)
  }
  
  /**
   * Tests if the circuit-breaker is in the open state.
   * 
   * <p>A circuit-breaker is in the open state when enough failures have
   * occurred in a configured amount of time and the opened state hasn't
   * expired yet.<p>
   */
  def isOpen = currentFailures.get >= conf.maxFailures && !isHalfOpen
  
  /**
   * Tests if the circuit-breaker is in the closed state.
   * 
   * <p>A circuit-breaker is in the closed state when it is not in the open
   * state.</p>
   */
  def isClosed = !isOpen
  
  /**
   * Tests if the circuit-breaker is in the half-open state.
   * 
   * <p>A circuit-breaker is in the half-open state when the open state has
   * expired.</p>
   */
  def isHalfOpen = {
    val timestampt = openTimestamp.get 
    timestampt != 0 && timestampt + conf.openCircuitTimeout.toMillis <= System.currentTimeMillis
  }
  
  /** Closes this circuit-breaker. */
  def close() {
    currentFailures.set(0)
    openTimestamp.set(0)
    try {
      listener.onClose(this)
    }
    catch {
      case e: Exception => throw new CircuitListenerException(name, "onClose", e) 
    }
  }
  
  def open() {
    open(new ForcedOpenException(name))
  }
  
  /** Opens this circuit-breaker. */
  def open(failure: Exception) {
    timesOpened.incrementAndGet()
    openTimestamp.set(System.currentTimeMillis)  
    currentFailures.set(conf.maxFailures)
    try {
      listener.onOpen(this, failure)
    }
    catch {
      case e: Exception => throw new CircuitListenerException(name, "onOpen", e)
    }
  }

  /** The current configuration. */
  def configuration = conf
  
  /** Reconfigures this circuit-breaker using the specified configuration. */
  def reconfigureWith(newConf: CircuitConfiguration) {
    conf = newConf
  }
  
  /**
   * The timestamp of the last transition to the open state (zero when the
   * circuit-breaker is in the closed state).
   */
  def openedTimestamp = openTimestamp.get
  
  /**
   * The number of failures since the circuit breaker entered the closed
   * state.
   */
  def numberOfCurrentFailures = currentFailures.get
  
  /**
   * The number of operations that threw an exception or took too long to
   * complete.
   */
  def numberOfFailedOperations = failures.get
  
  /** The number of operations (failed and successful). */
  def numberOfOperations = calls.get
  
  /** The number of times this circuit breaker has entered the open state. */
  def numberOfTimesOpened = timesOpened.get
  
  /**
   * Resets the statistics counters to zero.
   * 
   * <p>The values of {@code numberOfFailedOperations}, {@code numberOfOperations} and
   * {@code numberOfTimesOpened} are set to zero.</p>
   */
  def resetStatistics() {
    failures.set(0)
    calls.set(0)
    timesOpened.set(0)
  }
}
