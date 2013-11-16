/* ----------------- sse-breaker ----------------- *\
 * Licensed under the Apache License, Version 2.0. *
 * Author: Spiros Tzavellas                        *
\* ----------------------------------------------- */
package com.tzavellas.sse

import scala.concurrent.duration._

package object breaker {

  val DefaultTestConfiguration = new CircuitConfiguration(
    maxFailures = 5,
    openCircuitTimeout = 10.minutes,
    failureCountTimeout = 1.minute,
    maxMethodDuration =  1.minute)
}