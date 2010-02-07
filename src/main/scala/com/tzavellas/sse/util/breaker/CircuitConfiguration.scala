package com.tzavellas.sse.util.breaker

case class CircuitConfiguration(
    maxFailures: Int = 5,
    openCircuitTimeout: Duration = Duration.minutes(10),
    failureCountTimeout: Duration = Duration.minutes(1))

