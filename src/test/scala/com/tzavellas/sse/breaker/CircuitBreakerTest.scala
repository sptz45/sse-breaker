/* ----------------- sse-breaker ----------------- *\
 * Licensed under the Apache License, Version 2.0. *
 * Author: Spiros Tzavellas                        *
\* ----------------------------------------------- */
package com.tzavellas.sse.breaker

import org.junit.Test
import org.junit.Assert._

class CircuitBreakerTest extends CircuitDriver {

  val defaults = new CircuitConfiguration
  val listener = new TestListener
  val executor = new CircuitExecutor("test-circuit", circuitListener = listener)
  
  @Test
  def normal_operation_while_closed() {
    assertEquals(normalOperation, executor(normalOperation))
  }
  
  @Test(expected=classOf[OpenCircuitException])
  def after_a_number_of_faults_the_circuit_opens() {
    generateFaultsToOpen()
    assertTrue(circuit.isOpen)
    assertTrue(circuit.openedTimestamp > 0)
    makeNormalCall(circuitIsOpen=true)
  }
  
  @Test
  def the_circuit_can_be_closed_from_the_thrown_exception_data_after_opened() {
    generateFaultsToOpen()
    try {
      makeNormalCall(circuitIsOpen=true)
    } catch {
      case e: OpenCircuitException =>
       val circuit = e.circuitExecutor.circuitBreaker
       assertTrue(circuit.isOpen)
       circuit.close()
       assertEquals(0, circuit.openedTimestamp)
       assertFalse(circuit.isOpen)
       assertEquals(normalOperation, makeNormalCall())
       return
    }
    fail("The call to the open circuit should have raised an OpenCircuitException")
  }
  
  @Test
  def the_circuit_is_half_open_after_the_timeout() {
    reconfigureWith(openCircuitTimeout = Duration.millis(1))
    generateFaultsToOpen()
    Thread.sleep(2)
    assertTrue(circuit.isHalfOpen)
  }
  
  @Test
  def the_circuit_moves_from_half_open_to_closed_on_first_successful_operation() {
    reconfigureWith(openCircuitTimeout = Duration.millis(1))
    generateFaultsToOpen()
    Thread.sleep(2)
    assertTrue(circuit.isHalfOpen)
    makeNormalCall()
    assertTrue(circuit.isClosed)
    assertEquals(0, circuit.numberOfCurrentFailures)
  }
  
  @Test
  def the_circuit_moves_from_half_open_to_open_on_first_failure() {
    reconfigureWith(openCircuitTimeout = Duration.millis(1))
    generateFaultsToOpen()
    Thread.sleep(2)
    assertTrue(circuit.isHalfOpen)
    generateFaults(1)
    assertTrue(circuit.isOpen)
  }
  
  @Test
  def slow_methods_do_not_close_the_circuit_when_half_open() {
    reconfigureWith(openCircuitTimeout = Duration.millis(1))
    generateFaultsToOpen()
    Thread.sleep(2)
    assertTrue(circuit.isHalfOpen)
    makeSlowCall()
    assertFalse(circuit.isHalfOpen)
    assertTrue(circuit.isOpen)
  }
  
  @Test
  def the_failure_count_gets_reset_after_an_amount_of_time() {
    reconfigureWith(failureCountTimeout = Duration.millis(1))
    generateFaults(defaults.maxFailures - 1)
    assertTrue(circuit.isClosed)
    Thread.sleep(2)
    generateFaults(1)
    assertTrue(circuit.isClosed)
    makeNormalCall()
  }
  
  @Test
  def disable_breaker_by_setting_extremely_low_failure_count_timeout() {
    reconfigureWith(failureCountTimeout = Duration.nanos(1))  
    generateFaultsToOpen()
    assertTrue(circuit.isClosed)
  }
  
  @Test
  def ignored_exceptions_do_not_open_the_circuit() {
    executor.ignoreException(classOf[IllegalStateException])
    generateFaultsToOpen()
    makeNormalCall()
    assertTrue(circuit.isClosed)
    executor.stopIgnoringException(classOf[IllegalStateException])
  }
  
  @Test
  def ignored_exceptions_capture_subclasses() {
    executor.ignoreException(classOf[RuntimeException])
    generateFaultsToOpen()
    makeNormalCall()
    assertTrue(circuit.isClosed)
    executor.stopIgnoringException(classOf[RuntimeException])
  }
  
  @Test
  def slow_metnod_executions_count_as_failures() {
    for (i <- 0 until defaults.maxFailures) makeSlowCall()
    assertTrue(circuit.isOpen)
  }
  
  @Test
  def non_local_returns_are_not_recorded_as_failures() {
    for (i <- 0 until defaults.maxFailures)
      makeCallWithNonLocalReturn()
    assertTrue(circuit.isClosed)
  }
  
  @Test
  def circuit_listener_gets_called_when_the_circuits_state_changes() {
    generateFaultsToOpen()
    listener.assertCalledOnOpen()
    circuit.close()
    listener.assertCalledOnClose()
  }
  
  @Test
  def statistics_get_updated_as_the_ciruit_breaker_gets_used() {
    makeNormalCall()
    assertEquals(1, circuit.numberOfOperations)
    generateFaults(1)
    assertEquals(1, circuit.numberOfCurrentFailures)
    generateFaultsToOpen()
    assertEquals(1, circuit.numberOfTimesOpened)
    assertEquals(defaults.maxFailures, circuit.numberOfFailedOperations)
  }


  class TestListener extends CircuitStateChangeListener {
    
    var opened, closed: Boolean = false
    
    def onOpen(circuit: CircuitBreaker)  {
      opened = true
      assert(circuit.isOpen)
    }
    def onClose(circuit: CircuitBreaker) {
      closed = true
      assert(circuit.isClosed)
    }
    
    def assertCalledOnOpen()  { assert(opened) }
    def assertCalledOnClose() { assert(closed) }
  }
}
