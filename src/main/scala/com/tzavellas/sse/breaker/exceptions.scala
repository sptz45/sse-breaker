/* ----------------- sse-breaker ----------------- *\
 * Licensed under the Apache License, Version 2.0. *
 * Author: Spiros Tzavellas                        *
\* ----------------------------------------------- */
package com.tzavellas.sse.breaker

import scala.util.control.NoStackTrace

/**
 * A base class for all circuit breaker exceptions. 
 */
class CircuitBreakerException(message: String = null, cause: Throwable = null)
  extends RuntimeException(message, cause)

/**
 * Thrown when a {@code CircuitExecutor} is requested to execute an operation
 * and its associated {@code CircuitBreaker} is <em>open</em>.
 *
 * @param circuitExecutor the {@code CirsuitExecutor} that threw this exception.
 *        Can be used for manipulating and reconfiguring the circuit-breaker
 */
class OpenCircuitException(val circuitExecutor: CircuitExecutor)
  extends CircuitBreakerException(
      "Cannot proceed to execution since the circuit breaker '" +
      circuitExecutor.circuitBreaker.name + "' is open!")
    with NoStackTrace


class ForcedOpenException(circuitName: String)
  extends CircuitBreakerException("Ciruit '"+circuitName+"' opened after user's request.")


class SlowMethodExecutionException(val maxMethodDuration: Duration) extends CircuitBreakerException
