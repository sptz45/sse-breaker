/* ----------------- sse-breaker ----------------- *\
 * Licensed under the Apache License, Version 2.0. *
 * Author: Spiros Tzavellas                        *
\* ----------------------------------------------- */
package com.tzavellas.sse.breaker

/**
 * Observes state changes of circuit-breakers.
 * 
 * Implementations of this trait must be ''thread-safe''. 
 */
trait CircuitStateListener {
  
  /**
   * Called when a circuit-breaker moves into the ''open'' state.
   * 
   * @param circuit the circuit-breaker that got opened
   */
  def onOpen(circuit: CircuitBreaker, error: Throwable)
  
  /**
   * Called when a circuit-breaker moves into the ''closed'' state.
   * 
   * @param circuit the circuit-breaker that got closed
   */
  def onClose(circuit: CircuitBreaker)
}


object CircuitStateListener {
  
  /** A ''null object'' implementation for CircuitStateChangeListener. */
  object empty extends CircuitStateListener {
    def onOpen(circuit: CircuitBreaker, error: Throwable) { }
    def onClose(circuit: CircuitBreaker) { }
  }
  
  def of(listeners: CircuitStateListener*): CircuitStateListener = {
    new CircuitStateListener {
	  def onOpen(circuit: CircuitBreaker, error: Throwable) {
	    listeners foreach { _.onOpen(circuit, error) }
	  }
	  def onClose(circuit: CircuitBreaker) {
	    listeners foreach { _.onClose(circuit) }
	  }
	}
  }
}