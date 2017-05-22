/* ----------------- sse-breaker ----------------- *\
 * Licensed under the Apache License, Version 2.0. *
 * Author: Spiros Tzavellas                        *
\* ----------------------------------------------- */
package com.tzavellas.sse.breaker

import java.lang.management.ManagementFactory
import javax.management.{JMX, ObjectName}

import org.junit.Test
import org.junit.Assert._

class JmxIntegrationTest {

  private val server = ManagementFactory.getPlatformMBeanServer
  private val executor = new CircuitExecutor("jmx-test", DefaultTestConfiguration) with CircuitJmxExporter
  private def circuit = executor.circuitBreaker
  
  @Test
  def register_and_use_the_mbean(): Unit = {
    executor.exportToJmx()
    val mbean = JMX.newMBeanProxy(server,
                                  CircuitJmxExporter.objectNameOf(circuit),
                                  classOf[CircuitBreakerControlMBean])
    assertTrue(circuit.isClosed)
    assertFalse(mbean.isOpen)
    
    mbean.open()
    assertFalse(circuit.isClosed)
    assertTrue(mbean.isOpen)
    
    executor.removeFromJmx()
    val set = server.queryMBeans(CircuitJmxExporter.objectNameOf(circuit), null)
    assertTrue(set.isEmpty)
  }

  @Test
  def register_using_non_default_object_name(): Unit = {
    val executor = new CircuitExecutor("jmx-test", DefaultTestConfiguration) with MyExporter

    executor.exportToJmx()

    val mbean = JMX.newMBeanProxy(
      server,
      new ObjectName(s"package:type=CircuitBreaker,name=${executor.circuitBreaker.name}"),
      classOf[CircuitBreakerControlMBean])
    assertFalse(mbean.isOpen)

    executor.removeFromJmx()
  }

  trait MyExporter extends CircuitJmxExporter {
    override protected def objectName: ObjectName =
      new ObjectName(s"package:type=CircuitBreaker,name=${circuit.name}")
  }
}