/* ----------------- sse-breaker ----------------- *\
 * Licensed under the Apache License, Version 2.0. *
 * Author: Spiros Tzavellas                        *
\* ----------------------------------------------- */
package com.tzavellas.sse.breaker
package jmx

import java.lang.management.ManagementFactory
import javax.management.JMX
import org.junit.Test
import org.junit.Assert._

class JmxIntegrationTest {

  val server = ManagementFactory.getPlatformMBeanServer
  val executor = new CircuitExecutor("jmx-test")
  def circuit = executor.circuitBreaker
  
  @Test
  def register_and_use_the_mbean() {
    executor.exportToJmx()
    val mbean = JMX.newMBeanProxy(server,
                                  JmxRegistrar.objectName(circuit),
                                  classOf[CircuitBreakerControlMBean])
    assertTrue(circuit.isClosed)
    assertFalse(mbean.isOpen)
    
    mbean.open()
    assertFalse(circuit.isClosed)
    assertTrue(mbean.isOpen)
    
    executor.removeFromJmx()
    val set = server.queryMBeans(JmxRegistrar.objectName(circuit), null)
    assertTrue(set.isEmpty)
  }
}