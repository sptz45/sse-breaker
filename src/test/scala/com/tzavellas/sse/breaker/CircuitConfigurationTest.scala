/* ----------------- sse-breaker ----------------- *\
 * Licensed under the Apache License, Version 2.0. *
 * Author: Spiros Tzavellas                        *
\* ----------------------------------------------- */
package com.tzavellas.sse.breaker

import org.junit.Test
import org.junit.Assert._
import scala.runtime.NonLocalReturnControl

class CircuitConfigurationTest {

  def config = DefaultTestConfiguration
  
  @Test
  def fatal_throwables_are_not_failures(): Unit = {
    assertFalse(config.isFailure(new ThreadDeath))
    assertFalse(config.isFailure(new InterruptedException))
    assertFalse(config.isFailure(new NonLocalReturnControl(null, null)))
  }
  
  @Test
  def all_exceptions_are_failures(): Unit = {
    assertTrue(config.isFailure(new Exception))
  }
}