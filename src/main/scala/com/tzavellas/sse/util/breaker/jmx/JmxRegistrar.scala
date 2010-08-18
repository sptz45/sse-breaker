/* ----------------- sse-breaker ----------------- *\
 * Licensed under the Apache License, Version 2.0. *
 * Author: Spiros Tzavellas                        *
\* ----------------------------------------------- */
package com.tzavellas.sse.util.breaker
package jmx

import java.lang.management.ManagementFactory
import javax.management._

private[breaker] object JmxRegistrar {

  def register(executor: CircuitExecutor) {
    val mbean = new CircuitBreakerControl(executor)
    server.registerMBean(mbean, objectName(executor.circuitBreaker))
  }
  
  def unregister(executor: CircuitExecutor) {
    server.unregisterMBean(objectName(executor.circuitBreaker))
  }
  
  private def server = ManagementFactory.getPlatformMBeanServer
  
  private def objectName(circuit: CircuitBreaker) = {
    new ObjectName("com.tzavellas.sse.breaker:type=CircuitBreaker,name="+circuit.name)
  }
}