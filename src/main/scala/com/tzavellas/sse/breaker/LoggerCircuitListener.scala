/* ----------------- sse-breaker ----------------- *\
 * Licensed under the Apache License, Version 2.0. *
 * Author: Spiros Tzavellas                        *
\* ----------------------------------------------- */
package com.tzavellas.sse.breaker

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class LoggerCircuitListener(loggerName: String) extends CircuitStateChangeListener {

  def this(loggerName: Class[_]) {
    this(loggerName.getName)
  }

  private val log = LoggerFactory.getLogger(loggerName);

  def onOpen(breaker: CircuitBreaker, error: Exception) {
    log.error("Opened the circuit with name " + breaker.name + "'", error);
  }

  def onClose(breaker: CircuitBreaker) {
    log.info("Closed the circuit with name '" + breaker.name + "'");
  }
}