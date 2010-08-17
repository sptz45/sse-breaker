package com.tzavellas.sse.util.breaker

import org.junit.Test
import org.junit.Assert._

class CircuitBreakerTest {

  val defaults = new CircuitConfiguration
  val executor = new CircuitExecutor
  
  @Test
  def normal_operation_while_closed() {
    assertEquals(normalOperation, executor(normalOperation))
  }
  
  @Test(expected=classOf[OpenCircuitException])
  def after_a_number_of_faults_the_circuit_opens() {
    generateFaultsToOpen()
    makeNormalCall(circuitIsOpen=true)
  }
  
  @Test
  def the_circuit_can_be_opened_after_being_closed() {
    generateFaultsToOpen()
    try {
      makeNormalCall(circuitIsOpen=true)
    } catch {
      case e: OpenCircuitException =>
       val c = e.circuit
       assertTrue("The circuit should be open after an OpenCircuitException", c.isOpen)
       c.close()
       assertFalse("The circuit should be closed after a call to close()", c.isOpen)
       assertEquals(normalOperation, makeNormalCall())
       return
    }
    fail("Should have raised an OpenCircuitException")
  }
  
  
  @Test
  def the_circuit_is_half_open_after_the_timeout() {
    val circuit = reconfigureWithShortTimeout()
    generateFaultsToOpen()
    Thread.sleep(2)
    assertTrue("The circuit should have been half-open after the timeout", circuit.isHalfOpen)
    makeNormalCall()
    assertTrue(circuit.isClosed)
    assertEquals("From half-open to closed resets the current failures counter", 0, circuit.currentFailureCount)
  }
  
  @Test
  def the_circuit_moves_from_half_open_to_open_on_first_failure() {
    val circuit = reconfigureWithShortTimeout()
    generateFaultsToOpen()
    Thread.sleep(2)
    assertTrue(circuit.isHalfOpen)
    generateFaults(1)
    assertTrue(circuit.isOpen)
  }
  
  @Test
  def slow_methods_do_not_close_the_circuit_when_half_open() {
    val circuit = reconfigureWithShortTimeout()
    generateFaultsToOpen()
    Thread.sleep(2)
    assertTrue(circuit.isHalfOpen)
    executor.maxMethodDuration = Duration.nanos(1)
    makeSlowCall()
    assertFalse(circuit.isHalfOpen)
    assertTrue(circuit.isOpen)
  }
  
  @Test
  def the_failure_count_gets_reset_after_an_amount_of_time() {
    val circuit = executor.breaker
    val shortFailureCountTimeout = circuit.configuration.copy(failureCountTimeout = Duration.millis(1))  
    circuit.reconfigure(shortFailureCountTimeout)
    
    generateFaults(defaults.maxFailures - 1)
    assertFalse(circuit.isOpen)
    Thread.sleep(5)
    
    generateFaults(1)
    assertFalse("Must be open since the failure count must have been expired", circuit.isOpen)
    makeNormalCall()
  }
  
  @Test
  def disable_breaker_by_setting_extremly_low_failure_count_timeout() {
    val circuit = executor.breaker
    val shortFailureCountTimeout = circuit.configuration.copy(failureCountTimeout = Duration.nanos(1))  
    circuit.reconfigure(shortFailureCountTimeout)
    generateFaultsToOpen()
    makeNormalCall()
    assertFalse(circuit.isOpen)
  }
  
  @Test
  def ignored_exceptions_do_not_open_the_circuit() {
    executor.ignoreException(classOf[IllegalStateException])
    generateFaultsToOpen()
    makeNormalCall()
    assertFalse(executor.breaker.isOpen)
    executor.removeIgnoredExcpetion(classOf[IllegalStateException])
  }
  
  @Test
  def ignored_exceptions_capture_subclasses() {
    executor.ignoreException(classOf[RuntimeException])
    generateFaultsToOpen()
    makeNormalCall()
    assertFalse(executor.breaker.isOpen)
    executor.removeIgnoredExcpetion(classOf[RuntimeException])
  }
  
  @Test
  def slow_metnod_executions_count_as_failures() {
    executor.maxMethodDuration = Duration.nanos(1)
    for (i <- 0 until defaults.maxFailures)
      makeNormalCall()
    assertTrue("Should be open since the maxMethodDuration is to short", executor.breaker.isOpen)
  }
  
  
  def reconfigureWithShortTimeout() = {
    val circuit = executor.breaker
    val shortTimeout = circuit.configuration.copy(openCircuitTimeout = Duration.millis(1))  
    circuit.reconfigure(shortTimeout)
    circuit
  }
  
  def makeNormalCall(circuitIsOpen: Boolean = false) = {
    try {
      executor(normalOperation)
    } catch {
      case e: OpenCircuitException => 
        if (circuitIsOpen) throw e
        else throw new AssertionError("Unexpected OpenCircuitException!", e)
    }
  }
  
  def makeSlowCall() {
    val previous = executor.maxMethodDuration 
    executor.maxMethodDuration = Duration.nanos(1)
    makeNormalCall()
    executor.maxMethodDuration = previous
  }
  
  def generateFaultsToOpen() {
    generateFaults(defaults.maxFailures)
  }
  
  def generateFaults(numOfFaults: Int) {
    for (i <- 0 until numOfFaults)
      try { executor(faultyOperation) } catch { case _ => () }
  }
  
  def normalOperation = 42 
  def faultyOperation = throw new IllegalStateException
}
