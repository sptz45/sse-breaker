/* ----------------- sse-breaker ----------------- *\
 * Licensed under the Apache License, Version 2.0. *
 * Author: Spiros Tzavellas                        *
\* ----------------------------------------------- */
package com.tzavellas.sse.breaker

import org.junit.Assert._

class TestListener extends CircuitStateListener {

  var opened, closed: Boolean = false
  var error: Throwable = _

  def onOpen(circuit: CircuitBreaker, error: Throwable): Unit = {
    opened = true
    this.error = error
    assertNotNull(error)
    assertTrue(circuit.isOpen)
  }

  def onClose(circuit: CircuitBreaker): Unit = {
    closed = true
    assertTrue(circuit.isClosed)
  }

  def assertCalledOnOpen(): Unit = {
    assertTrue(opened)
  }

  def assertCalledOnClose(): Unit = {
    assertTrue(closed)
  }
}