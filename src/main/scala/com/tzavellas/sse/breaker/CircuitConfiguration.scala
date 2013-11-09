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
 *                            to the <em>half-open</em> state.
 * @param failureCountTimeout the duration after which the number of failures
 *                            will get reset.
 * @param isFailure           a function that decides whether to recored a thrown
 *                            exception as a failure.
 * @param maxMethodDuration   the duration after which a method execution is
 *                            considered a failure.
 */
case class CircuitConfiguration(
    maxFailures: Int = 5,
    openCircuitTimeout: Duration = 10 minutes,
    failureCountTimeout: Duration = 1 minute,
    isFailure: Exception => Boolean = _ => true,
    maxMethodDuration: Duration =  1 minute)

