/* ----------------- sse-breaker ----------------- *\
 * Licensed under the Apache License, Version 2.0. *
 * Author: Spiros Tzavellas                        *
\* ----------------------------------------------- */
package com.tzavellas.sse.breaker

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.control.NonFatal
import scala.util.{Success, Failure}
import java.util.concurrent.Executor

/**
 * An executor that implements the Circuit Breaker stability design pattern.
 * 
 * The purpose of the a circuit-breaker is to keep track of the error rates
 * of dangerous operations (such as calls to an integration point) and prevent
 * the execution of those operations for a configurable amount of time when the
 * error rates are high by failing fast. A circuit-breaker has three states:
 * ''closed'', ''open'' and ''half-open''.
 * 
 * During normal operation the circuit-breaker is ''closed'' and the
 * executor executes the specified operation, recording the number of failures
 * (exceptions thrown) that happen as a result of those executions. When the
 * number of failures exceeds a configured number then the circuit-breaker moves
 * to the ''open'' state.
 * 
 * In the ''open'' state, since the probability that failures will happen
 * is high, the executor when requested to execute an operation ''fails fast''
 * by throwing an `OpenCircuitException` or by returning a `Future` that
 * contains `OpenCircuitException` if the operation is asynchronous.
 * 
 * After a configurable amount of time the circuit-breaker goes to the
 * ''half-open'' state. In that state when a request to execute an operation
 * is made, the executor executes the operation and if it succeeds the
 * circuit-breaker moves to the ''closed'' state, else it moves to the
 * ''open'' state.
 * 
 * Instances of this class are ''thread-safe''.
 *
 * @see CircuitBreaker
 * @see CircuitConfiguration
 * @see OpenCircuitException
 */
class CircuitExecutor private (val circuitBreaker: CircuitBreaker) {

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
  ) = {
    this(new CircuitBreaker(circuitName, circuitConfig, circuitListener))
  }

  // -- public methods --------------------------------------------------------

  /**
   * Executes the specified operation depending on the state of the
   * circuit-breaker.
   * 
   * @param operation the operation to execute.
   * 
   * @return the result of the operation execution.
   * @throws OpenCircuitException if the circuit-breaker is ''open''.
   */
  def apply[T](operation: => T): T = {
    val start = System.nanoTime
    onStart()
    try {
      val result = operation
      onSuccess(start)
      result
    } catch {
      case e: Exception =>
        onFailure(e)
        throw e
    }
  }

  /**
   * Executes the specified operation depending on the state of the
   * circuit-breaker.
   *
   * @param operation the operation to execute.
   *
   * @return the result of the operation execution or a failed `Future` that
   *         contains `OpenCircuitException` if the circuit-breaker is ''open''.
   */
  def apply[T](operation: => Future[T]): Future[T] = {
    val start = System.nanoTime
    try onStart() catch { case e: OpenCircuitException => return Future.failed(e) }
    val result = try operation catch { case NonFatal(e) => Future.failed(e) }
    result.onComplete({
      case Success(_) => onSuccess(start)
      case Failure(e) => onFailure(e)
    })(CircuitExecutor.currentThreadExecutor)
    result
  }

  /**
   * Executes the specified operation asynchronously depending on the state of
   * the circuit-breaker using the given `ExecutionContext`.
   *
   * Use this method if you have a synchronous operation that you want to execute
   * in a different thread. If the specified `operation` is already asynchronous
   * (returns a `Future`) then use the `apply` method.
   *
   * @param operation the operation to execute.
   *
   * @return the result of the operation execution or a failed `Future` that
   *         contains `OpenCircuitException` if the circuit-breaker is ''open''.
   */
  def async[T](operation: => T)(implicit executor: ExecutionContext): Future[T] = {
    val start = System.nanoTime
    try onStart() catch { case e: OpenCircuitException => return Future.failed(e) }
    val result = Future(operation)(executor)
    result.onComplete({
      case Success(_) => onSuccess(start)
      case Failure(e) => onFailure(e)
    })(CircuitExecutor.currentThreadExecutor)
    result
  }

  // -- private methods -------------------------------------------------------

  private def onStart() = {
    circuitBreaker.recordCall()
    if (circuitBreaker.isOpen) throw new OpenCircuitException(this)
  }

  private def onSuccess(startNanosTsamp: Long) = {
    circuitBreaker.recordExecutionTime(System.nanoTime - startNanosTsamp)
  }

  private def onFailure(e: Throwable) = circuitBreaker.recordThrowable(e)
}

private object CircuitExecutor {
  val currentThreadExecutor: ExecutionContext = ExecutionContext.fromExecutor(new Executor {
    def execute(command: Runnable): Unit = {
      command.run()
    }
  })
}