/* ----------------- sse-breaker ----------------- *\
 * Licensed under the Apache License, Version 2.0. *
 * Author: Spiros Tzavellas                        *
\* ----------------------------------------------- */
package com.tzavellas.sse.breaker

import java.lang.management.ManagementFactory
import javax.management._

trait CircuitJmxExporter {
  
  val circuitBreaker: CircuitBreaker
  
  import CircuitJmxExporter._

  /**
   * Register a `CircuitBreakerControlMBean` for this executor in the platform
   * MBean server.
   */
  def exportToJmx(): Unit = {
    val mbean = new CircuitBreakerControl(circuitBreaker)
    server.registerMBean(mbean, objectNameOf(circuitBreaker))
  }
  
  /**
   * Unregister the `CircuitBreakerControlMBean` that is associated with this
   * executor from the platform MBean server.
   */
  def removeFromJmx(): Unit = server.unregisterMBean(objectNameOf(circuitBreaker))
  
  private def server = ManagementFactory.getPlatformMBeanServer
}

private object CircuitJmxExporter {
  def objectNameOf(circuit: CircuitBreaker): ObjectName = {
    new ObjectName(s"com.tzavellas.sse.breaker:type=CircuitBreaker,name=${circuit.name}")
  }  
}