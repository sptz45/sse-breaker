/* ----------------- sse-breaker ----------------- *\
 * Licensed under the Apache License, Version 2.0. *
 * Author: Spiros Tzavellas                        *
\* ----------------------------------------------- */
package com.tzavellas.sse.breaker

import scala.concurrent.duration._

/**
 * Holds configuration data for the circuit-breaker.
 *
 * @param maxFailures         the number of failures that must occur to open the
 *                            circuit.
 * @param openCircuitTimeout  the duration after which the circuit-breaker moves
 *                            to the ''half-open'' state.
 * @param failureCountTimeout the duration after which the number of failures
 *                            will get reset.
 * @param isFailure           a function that decides whether to recored a thrown
 *                            exception as a failure.
 * @param maxMethodDuration   the duration after which a method execution is
 *                            considered a failure.
 */
case class CircuitConfiguration(
    maxFailures: Int,
    openCircuitTimeout: Duration,
    failureCountTimeout: Duration,
    maxMethodDuration: Duration,
    isFailure: Exception => Boolean = _ => true)

