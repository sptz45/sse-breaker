/* ----------------- sse-breaker ----------------- *\
 * Licensed under the Apache License, Version 2.0. *
 * Author: Spiros Tzavellas                        *
\* ----------------------------------------------- */
package com.tzavellas.sse.breaker

import scala.util.control.NoStackTrace

/**
 * Thrown when a {@code CircuitExecutor} is requested to execute an operation
 * and its associated {@code CircuitBreaker} is <em>open</em>.
 *
 * @param circuitExecutor the {@code CirsuitExecutor} that threw this exception.
 *        Can be used for manipulating and reconfiguring the circuit-breaker
 */
class OpenCircuitException(val circuitExecutor: CircuitExecutor)
  extends RuntimeException
     with NoStackTrace
