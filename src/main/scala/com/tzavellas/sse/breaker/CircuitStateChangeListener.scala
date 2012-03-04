/* ----------------- sse-breaker ----------------- *\
 * Licensed under the Apache License, Version 2.0. *
 * Author: Spiros Tzavellas                        *
\* ----------------------------------------------- */
package com.tzavellas.sse.breaker

/**
 * Observes state changes of circuit-breakers.
 * 
 * <p>Implementations of this trait must be <em>thread-safe</em>.</p> 
 */
trait CircuitStateChangeListener {
  
  /**
   * Called when a circuit-breaker moves into the <em>open</em> state.
   * 
   * @param circuit the circuit-breaker that got opened
   */
  def onOpen(circuit: CircuitBreaker, exception: Exception)
  
  /**
   * Called when a circuit-breaker moves into the <em>closed</em> state.
   * 
   * @param circuit the circuit-breaker that got closed
   */
  def onClose(circuit: CircuitBreaker)
}


object CircuitStateChangeListener {
  
  /** A <em>null-object</em> implementation for CircuitStateChangeListener. */
  object empty extends CircuitStateChangeListener {
    def onOpen(circuit: CircuitBreaker, exception: Exception) { }
    def onClose(circuit: CircuitBreaker) { }
  }
  
  def of(listeners: CircuitStateChangeListener*): CircuitStateChangeListener =
    new CircuitStateChangeListener {
	  def onOpen(circuit: CircuitBreaker, exception: Exception) {
	    listeners foreach { _.onOpen(circuit, exception) }
	  }
	  def onClose(circuit: CircuitBreaker) {
	    listeners foreach { _.onClose(circuit) }
	  }
	}
}