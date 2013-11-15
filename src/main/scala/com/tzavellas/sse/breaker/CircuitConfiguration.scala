/* ----------------- sse-breaker ----------------- *\
 * Licensed under the Apache License, Version 2.0. *
 * Author: Spiros Tzavellas                        *
\* ----------------------------------------------- */
package com.tzavellas.sse.breaker

import scala.concurrent.duration._
import scala.util.control.NonFatal

/**
 * Holds configuration data for the circuit-breaker.
 *
 * @param maxFailures         the number of failures that must occur to open the
 *                            circuit.
 * @param openCircuitTimeout  the duration after which the circuit-breaker moves
 *                            to the ''half-open'' state.
 * @param failureCountTimeout the duration after which the number of failures
 *                            will get reset.
 * @param maxMethodDuration   the duration after which a method execution is
 *                            considered a failure.
 * @param isFailure           a function that decides whether to record a thrown
 *                            `Throwable` as a failure. By default all `NonFatal`
 *                            throwables are considered failures.
 */
case class CircuitConfiguration(
    maxFailures: Int,
    openCircuitTimeout: Duration,
    failureCountTimeout: Duration,
    maxMethodDuration: Duration,
    isFailure: Throwable => Boolean = { case NonFatal(e) => true; case _ => false })

