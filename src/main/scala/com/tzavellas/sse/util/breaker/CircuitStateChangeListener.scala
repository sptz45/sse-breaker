package com.tzavellas.sse.util.breaker

/**
 * Observes state changes of ciruit-breakers.
 * 
 * <p>Tha API of this trait allows you to associate a single listener instance
 * with more that one circuit-breaker. If you do this then your implementations
 * must be <em>thread-safe</em>.</p> 
 */
trait CircuitStateChangeListener {
  
  /**
   * Called when a circuit-breaker moves into the <em>open</em> state.
   * 
   * @param circuit the circuit-breaker that got opened
   */
  def onOpen(circuit: CircuitBreaker)
  
  /**
   * Called when a circuit-breaker moves into the <em>closed</em> state.
   * 
   * @param circuit the circuit-breaker that got closed
   */
  def onClose(circuit: CircuitBreaker)
}


object CircuitStateChangeListener {
  
  /** A <em>null-object</em> implementation for CircuitStateChangeListener. */
  object NULL extends CircuitStateChangeListener  {
    def onOpen(circuit: CircuitBreaker) { }
    def onClose(circuit: CircuitBreaker) { }
  }
}