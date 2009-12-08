package com.tzavellas.sse.util.breaker

import java.util.concurrent.atomic.{AtomicInteger, AtomicLong}

/**
 * Holds the state of a circuit-breaker.
 *
 * <p>Instances of this class are thread-safe.</p>
 * 
 * @see CircuitExecutor
 * @author spiros
 */
class CircuitBreaker(initConf: CircuitConfiguration) {
  
  @volatile
  private[this] var conf = initConf
  
  private[this] val currentFailures = new AtomicInteger(0)
  private[this] val openTimestamp = new AtomicLong()
  private[this] val firstCurrentFailureTimestamp = new AtomicLong()

  
  private[breaker] def recordFailure() {
    initFirstFailureTimeStampIfNeeded()
    var tmpCurrentFailures = 0
    if (conf.failureCountTimeout.hasPastSince(firstCurrentFailureTimestamp.get())) {
      resetFailures()
      tmpCurrentFailures = 1
    } else {
      tmpCurrentFailures = currentFailures.incrementAndGet()
    }
    if (tmpCurrentFailures >= conf.maxFailures)
        open();
  }
  
  private def resetFailures() {
    currentFailures.set(1)
    firstCurrentFailureTimestamp.set(System.nanoTime())
  }
  
  private def initFirstFailureTimeStampIfNeeded() {
    firstCurrentFailureTimestamp.compareAndSet(0, System.nanoTime())
  }

  def isClosed = !isOpen
  
  def isOpen = currentFailures.get >= conf.maxFailures && !isHalfOpen
  
  def isHalfOpen = hasExpired
  
  def hasExpired = {
    val timestampt = openTimestamp.get() 
    timestampt != 0 && timestampt + conf.openCircuitTimeout.toMillis <= System.currentTimeMillis()
  }
  
  def close() {
    currentFailures.set(0)
    openTimestamp.set(0)
  }
  
  def open() {
    openTimestamp.set(System.currentTimeMillis())  
    currentFailures.set(conf.maxFailures)
  }

  def configuration = conf
  
  def reconfigure(newConf: CircuitConfiguration) {
    conf = newConf
  }

  def currentFailureCount = currentFailures.get
}