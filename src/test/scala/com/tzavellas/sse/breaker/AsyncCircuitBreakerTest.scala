/* ----------------- sse-breaker ----------------- *\
 * Licensed under the Apache License, Version 2.0. *
 * Author: Spiros Tzavellas                        *
\* ----------------------------------------------- */
package com.tzavellas.sse.breaker

import org.junit.Test

class AsyncCircuitBreakerTest extends CircuitBreakerTest with AsynchronousCircuitDriver {

  @Test
  def dummy_test() { }
}