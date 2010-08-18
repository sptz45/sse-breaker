/* ----------------- sse-breaker ----------------- *\
 * Licensed under the Apache License, Version 2.0. *
 * Author: Spiros Tzavellas                        *
\* ----------------------------------------------- */
package com.tzavellas.sse.util.breaker

import scala.util.control.NoStackTrace

/**
 * Thrown when a {@code CircuitExecutor} is requested to execute an operation
 * and its associated {@code CircuitBreaker} is <em>open</em>.
 *
 * @param circuit the {@code CirsuitBreaker} that is in the open state and
 *                caused this exception.
 */
class OpenCircuitException(val circuit: CircuitBreaker) extends RuntimeException with NoStackTrace
