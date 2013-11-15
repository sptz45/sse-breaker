package com.tzavellas.sse.breaker

import org.junit.Test
import org.junit.Assert._
import scala.runtime.NonLocalReturnControl

class CircuitConfigurationTest {

  def config = DefaultTestConfiguration
  
  @Test
  def fatal_throwables_are_not_failures() {
    assertFalse(config.isFailure(new ThreadDeath))
    assertFalse(config.isFailure(new InterruptedException))
    assertFalse(config.isFailure(new NonLocalReturnControl(null, null)))
  }
  
  @Test
  def all_exceptions_are_failures() {
    assertTrue(config.isFailure(new Exception))
  }
}