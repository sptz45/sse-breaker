/* ----------------- sse-breaker ----------------- *\
 * Licensed under the Apache License, Version 2.0. *
 * Author: Spiros Tzavellas                        *
\* ----------------------------------------------- */
package com.tzavellas.sse.breaker.extras

import org.slf4j.LoggerFactory
import com.tzavellas.sse.breaker.CircuitBreaker
import com.tzavellas.sse.breaker.CircuitStateListener

class LoggerCircuitListener(loggerName: String) extends CircuitStateListener {

  def this(loggerName: Class[_]) = this(loggerName.getName)

  private val log = LoggerFactory.getLogger(loggerName)

  def onOpen(breaker: CircuitBreaker, error: Throwable): Unit = {
    log.error(s"Opened the circuit with name '${breaker.name}'", error)
  }

  def onClose(breaker: CircuitBreaker): Unit = {
    log.info(s"Closed the circuit with name '${breaker.name}'")
  }
}