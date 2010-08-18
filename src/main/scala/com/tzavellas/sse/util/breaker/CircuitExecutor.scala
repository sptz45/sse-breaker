/* ----------------- sse-breaker ----------------- *\
 * Licensed under the Apache License, Version 2.0. *
 * Author: Spiros Tzavellas                        *
\* ----------------------------------------------- */
package com.tzavellas.sse.util.breaker

/**
 * An executor that implements the Circuit Breaker stability design pattern.
 * 
 * <p>The purpose of the a circuit-breaker is to keep track of the error rates
 * of dangerous operations (such as calls to an integration point) and prevent
 * the execution of those operations for a configurable amount of time when the
 * error rates are high. A circuit-breaker has three states: <em>closed</em>,
 * <em>open</em> and <em>half-open</em>.
 * 
 * <p>During normal operation the circuit-breaker is <em>closed</em> and the
 * executor executes the specified operation, recording the number of failures
 * (exceptions thrown) that happen as a result of those executions. When the
 * number of failures exceeds a configured number then the circuit-breaker moves
 * to the <em>open</em> state.</p> 
 * 
 * <p>In the <em>open</em> state, since the probability that failures will happen
 * is high, the executor when requested to execute an operation <em>fails fast</em>
 * by throwing an {@code OpenCircuitException}.</p>
 * 
 * <p>After a configurable amount of time the circuit-breaker goes to the
 * <em>half-open</em> state. In that state when a request to execute an operation
 * is made, the executor executes the operation and if it succeeds the
 * circuit-breaker moves to the <em>closed</em> state, else it moves to the
 * <em>open</em> state.</p>
 * 
 * <p>Instances of this class are thread-safe.</p>
 * 
 * 
 * @param circuitName     the name of the circuit-breaker.
 * @param circuitConfig   the configuration of the circuit-breaker.
 * @param circuitListener called when the state of the circuit-breaker changes.
 * 
 * @see CircuitBreaker
 * @see CircuitConfiguration
 * @see OpenCircuitException
 */
class CircuitExecutor(
  circuitName: String,
  circuitConfig: CircuitConfiguration = new CircuitConfiguration,
  circuitListener: CircuitStateChangeListener = CircuitStateChangeListener.NULL) {
  
  private val ignoredExceptions = new ClassFilter
  
  /** The circuit-breaker of this executor. */
  val circuitBreaker = new CircuitBreaker(circuitName, circuitConfig, circuitListener)
  
  /**
   * The duration after which a method execution is considered a failure.
   * 
   * <p>The default value is 1 minute.</p>
   */
  @volatile
  var maxMethodDuration = Duration.minutes(1)
  
  /**
   * When an exception of the specified type gets thrown as a result of an
   * operation execution not increment the failure counter.
   * 
   * <p>Please note that subclasses of the specified exception will also
   * be ignored.</p>
   * 
   * @param ignored the exception to ignore
   */
  def ignoreException[T <: Throwable](exception: Class[T]) {
    ignoredExceptions += exception
  }
  
  /**
   * Stop ignoring exceptions of the specified type when recording failures.
   * 
   * @param exception the exception to stop ignoring
   */
  def removeIgnoredException[T <: Throwable](exception: Class[T]) {
    ignoredExceptions -= exception
  }
  
  /**
   * Executes the specified operation depending on the state of the
   * circuit-breaker.
   * 
   * @param operation the operation to execute.
   * 
   * @return the result of the operation execution.
   * @throws OpenCircuitException if the circuit-breaker is open.
   */
  def apply[T](operation: => T): T = {
    circuitBreaker.recordCall()
    assertTheCircuitIsClosed()
    try {
      val result = execute(operation)
      val wasNotSlow = recordAsFailureIfItWasSlow(result.duration)
      if (wasNotSlow)
        closeTheCircuitIfItIsHalfOpen()
      result.value
    } catch {
      case e =>
        recordIfNotIgnored(e)
        throw e
    }
  }
  
  private def execute[T](operation: => T) = ExecutionTimer.time(operation) 

  private def assertTheCircuitIsClosed() {
    if (circuitBreaker.isOpen) throw new OpenCircuitException(circuitBreaker)
  }
  
  private def recordAsFailureIfItWasSlow(duration: Long) = {
    if (duration >= maxMethodDuration.toNanos) {
      circuitBreaker.recordFailure()
      false
    } else {
      true
    }
  }
  
  private def closeTheCircuitIfItIsHalfOpen() {
    if (circuitBreaker.isHalfOpen) circuitBreaker.close()
  }
  
  private def recordIfNotIgnored(e: Throwable) {
    if (! ignoredExceptions.contains(e.getClass))
      circuitBreaker.recordFailure()
  }
}

private object ExecutionTimer {
  
  def time[T](operation: => T) = {
    val start = System.nanoTime()
    new { 
      val value = operation
      val duration = System.nanoTime() - start
    }
  }
}
