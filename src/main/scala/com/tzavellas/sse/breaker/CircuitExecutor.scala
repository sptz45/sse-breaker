/* ----------------- sse-breaker ----------------- *\
 * Licensed under the Apache License, Version 2.0. *
 * Author: Spiros Tzavellas                        *
\* ----------------------------------------------- */
package com.tzavellas.sse.breaker

/**
 * An executor that implements the Circuit Breaker stability design pattern.
 * 
 * The purpose of the a circuit-breaker is to keep track of the error rates
 * of dangerous operations (such as calls to an integration point) and prevent
 * the execution of those operations for a configurable amount of time when the
 * error rates are high. A circuit-breaker has three states: ''closed'',
 * ''open'' and ''half-open''.
 * 
 * During normal operation the circuit-breaker is ''closed'' and the
 * executor executes the specified operation, recording the number of failures
 * (exceptions thrown) that happen as a result of those executions. When the
 * number of failures exceeds a configured number then the circuit-breaker moves
 * to the ''open'' state.
 * 
 * In the ''open'' state, since the probability that failures will happen
 * is high, the executor when requested to execute an operation ''fails fast''
 * by throwing an `OpenCircuitException`.
 * 
 * After a configurable amount of time the circuit-breaker goes to the
 * ''half-open'' state. In that state when a request to execute an operation
 * is made, the executor executes the operation and if it succeeds the
 * circuit-breaker moves to the ''closed'' state, else it moves to the
 * ''open'' state.
 * 
 * Instances of this class are ''thread-safe''.
 *
 * @constructor Creates an executor for the specified circuit-breaker.
 *
 * @param circuitBreaker the circuit-breaker of the executor.
 *
 * @see CircuitBreaker
 * @see CircuitConfiguration
 * @see OpenCircuitException
 */
class CircuitExecutor(val circuitBreaker: CircuitBreaker) {
  
  /**
   * Create an executor.
   * 
   * @param circuitName     the name of the circuit-breaker.
   * @param circuitConfig   the configuration of the circuit-breaker.
   * @param circuitListener called when the state of the circuit-breaker changes.
   */
  def this(
    circuitName: String,
    circuitConfig: CircuitConfiguration,
    circuitListener: CircuitStateListener = CircuitStateListener.empty
  ) {
    this(new CircuitBreaker(circuitName, circuitConfig, circuitListener))
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
      val start = System.nanoTime
      val result = operation
      val duration = System.nanoTime - start
      circuitBreaker.recordExecutionTime(duration);
      result
    } catch {
      case e: Exception =>
        circuitBreaker.recordException(e)
        throw e
    }
  }

  private def assertTheCircuitIsClosed() {
    if (circuitBreaker.isOpen) throw new OpenCircuitException(this)
  }
}