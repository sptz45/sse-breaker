/* ----------------- sse-breaker ----------------- *\
 * Licensed under the Apache License, Version 2.0. *
 * Author: Spiros Tzavellas                        *
\* ----------------------------------------------- */
package com.tzavellas.sse.breaker

import scala.util.control.NoStackTrace
import scala.concurrent.duration.Duration

/**
 * A base class for all circuit breaker exceptions. 
 */
class CircuitBreakerException(message: String = null, cause: Throwable = null)
  extends RuntimeException(message, cause)

/**
 * Thrown when a `CircuitExecutor` is requested to execute an operation
 * and its associated `CircuitBreaker` is ''open''.
 *
 * @param circuitExecutor the `CircuitExecutor` that threw this exception. Can
 *                        be used for manipulating and reconfiguring the circuit
 *                        breaker.
 */
class OpenCircuitException(val circuitExecutor: CircuitExecutor)
  extends CircuitBreakerException(
      s"Cannot proceed to execution since the circuit breaker '${circuitExecutor.circuitBreaker.name}' is open!")
    with NoStackTrace

/**
 * Wraps exceptions thrown from `CircuitStateListener` implementations.
 */
class CircuitListenerException(name: String, action: String, cause: Throwable)
  extends CircuitBreakerException(s"Error while invoking $action on the listener of circuit '$name'", cause)


// -- internal exceptions -----------------------------------------------------

private class ForcedOpenException(circuitName: String)
  extends CircuitBreakerException(s"Circuit '$circuitName' opened after user's request.")


private class SlowMethodExecutionException(val maxMethodDuration: Duration) extends CircuitBreakerException

