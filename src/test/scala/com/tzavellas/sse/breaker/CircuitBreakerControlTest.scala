/* ----------------- sse-breaker ----------------- *\
 * Licensed under the Apache License, Version 2.0. *
 * Author: Spiros Tzavellas                        *
\* ----------------------------------------------- */
package com.tzavellas.sse.breaker

import java.util.Date
import org.junit.Test
import org.junit.Assert._

class CircuitBreakerControlTest extends CircuitDriver {

  val defaults = new CircuitConfiguration
  val executor = new CircuitExecutor("control-test") with CircuitJmxExporter
  private val control  = new CircuitBreakerControl(circuit)
  
  
  @Test
  def the_control_can_read_the_state_of_the_circuit() {
    assertEquals(circuit.name, control.getName)
    assertFalse(control.isOpen)
    generateFaultsToOpen()
    assertTrue(control.isOpen)
    assertEquals(circuit.openedTimestamp, control.getOpenedTimestamp.getTime)
    circuit.close()
    assertFalse(control.isOpen)
  }
  
  @Test
  def the_control_can_manipulate_the_state_of_the_circuit() {
    control.open()
    assertTrue(circuit.isOpen)
    control.close()
    assertTrue(circuit.isClosed)
  }

  @Test
  def the_control_can_reconfigure_the_circuit() {
    assertEquals(config.maxFailures, control.getMaxFailures)
    control.setMaxFailures(2)
    assertEquals(2, config.maxFailures)
    
    assertEquals(config.openCircuitTimeout.toString, control.getOpenCircuitTimeout)
    control.setOpenCircuitTimeout("30sec")
    assertEquals(Duration.seconds(30), config.openCircuitTimeout)
    
    assertEquals(config.failureCountTimeout.toString, control.getFailureCountTimeout)
    control.setFailureCountTimeout("10sec")
    assertEquals(Duration.seconds(10), config.failureCountTimeout)
    
    assertEquals(config.maxMethodDuration.toString, control.getMaxMethodDuration)
    control.setMaxMethodDuration("10sec")
    assertEquals(Duration.seconds(10), config.maxMethodDuration)
  }
  
  @Test
  def the_control_can_read_statistics() {
    generateFaultsToOpen()
    assertEquals(circuit.numberOfCurrentFailures, control.getCurrentFailures)
    assertEquals(circuit.numberOfFailedOperations, control.getFailedOperations)
    assertEquals(circuit.numberOfOperations, control.getTotalOperations)
    assertEquals(circuit.numberOfTimesOpened , control.getNumberOfTimesOpened )
    control.resetStatistics()
    assertEquals(0, control.getFailedOperations)
    assertEquals(0, control.getTotalOperations)
    assertEquals(0, control.getNumberOfTimesOpened)
  }
}