/* ----------------- sse-breaker ----------------- *\
 * Licensed under the Apache License, Version 2.0. *
 * Author: Spiros Tzavellas                        *
\* ----------------------------------------------- */
package com.tzavellas.sse.breaker

import org.junit.Test
import org.junit.Assert._
import scala.concurrent.duration._

class CircuitBreakerControlTest extends SynchronousCircuitDriver {

  val executor = new CircuitExecutor("control-test", defaults)
  private val control  = new CircuitBreakerControl(circuit)
  
  
  @Test
  def the_control_can_read_the_state_of_the_circuit(): Unit = {
    assertEquals(circuit.name, control.getName)
    assertFalse(control.isOpen)
    generateFaultsToOpen()
    assertTrue(control.isOpen)
    assertEquals(circuit.openedTimestamp, control.getOpenedTimestamp.getTime)
    circuit.close()
    assertFalse(control.isOpen)
  }
  
  @Test
  def the_control_can_manipulate_the_state_of_the_circuit(): Unit = {
    control.open()
    assertTrue(circuit.isOpen)
    control.close()
    assertTrue(circuit.isClosed)
  }

  @Test
  def the_control_can_reconfigure_the_circuit(): Unit = {
    assertEquals(config.maxFailures, control.getMaxFailures)
    control.setMaxFailures(2)
    assertEquals(2, config.maxFailures)
    
    assertEquals(config.openCircuitTimeout.toString, control.getOpenCircuitTimeout)
    control.setOpenCircuitTimeout("30sec")
    assertEquals(30.seconds, config.openCircuitTimeout)
    
    assertEquals(config.failureCountTimeout.toString, control.getFailureCountTimeout)
    control.setFailureCountTimeout("10sec")
    assertEquals(10.seconds, config.failureCountTimeout)
    
    assertEquals(config.maxMethodDuration.toString, control.getMaxMethodDuration)
    control.setMaxMethodDuration("10sec")
    assertEquals(10.seconds, config.maxMethodDuration)
  }
  
  @Test
  def the_control_can_read_statistics(): Unit = {
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