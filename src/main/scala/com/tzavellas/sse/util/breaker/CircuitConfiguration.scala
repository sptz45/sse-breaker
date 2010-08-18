/* ----------------- sse-breaker ----------------- *\
 * Licensed under the Apache License, Version 2.0. *
 * Author: Spiros Tzavellas                        *
\* ----------------------------------------------- */
package com.tzavellas.sse.util.breaker

/**
 * Holds configuration data for the circuit-breaker.
 *
 * @param maxFailures         the number of failures that must occur to open the
 *                            circuit.
 * @param openCircuitTimeout  the duration after which the circuit-breaker moves
 *                            to the <em>half-open</em> state.
 * @param failureCountTimeout the duration after which the number of failures
 *                            will get reset.
 */
case class CircuitConfiguration(
    maxFailures: Int = 5,
    openCircuitTimeout: Duration = Duration.minutes(10),
    failureCountTimeout: Duration = Duration.minutes(1))

