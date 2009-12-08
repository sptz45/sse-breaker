package com.tzavellas.sse.util.breaker

import CircuitConfiguration._

case class CircuitConfiguration(
    maxFailures: Int = defaultMaxFailures,
    openCircuitTimeout: Duration = defaultOpenCircuitTimeout,
    failureCountTimeout: Duration = defaultFailureCountTimeout)

object CircuitConfiguration {
  val defaultMaxFailures = 5
  val defaultOpenCircuitTimeout = Duration.minutes(10)
  val defaultFailureCountTimeout = Duration.minutes(1)
}
