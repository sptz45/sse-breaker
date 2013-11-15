package com.tzavellas.sse.breaker

import org.junit.Assert._

class TestListener extends CircuitStateListener {

  var opened, closed: Boolean = false
  var error: Throwable = _

  def onOpen(circuit: CircuitBreaker, error: Throwable)  {
    opened = true
    this.error = error
    assertNotNull(error)
    assertTrue(circuit.isOpen)
  }

  def onClose(circuit: CircuitBreaker) {
    closed = true
    assertTrue(circuit.isClosed)
  }

  def assertCalledOnOpen()  {
    assertTrue(opened)
  }

  def assertCalledOnClose() {
    assertTrue(closed)
  }
}