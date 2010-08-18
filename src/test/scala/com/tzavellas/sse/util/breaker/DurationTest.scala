/* ----------------- sse-breaker ----------------- *\
 * Licensed under the Apache License, Version 2.0. *
 * Author: Spiros Tzavellas                        *
\* ----------------------------------------------- */
package com.tzavellas.sse.util.breaker

import java.util.concurrent.TimeUnit
import org.junit.Test
import org.junit.Assert._


class DurationTest {
  
  import Duration._
  
  @Test
  def has_past_since_tests() {
    assertTrue(nanos(1).hasPastSince(System.nanoTime()))
    assertFalse(millis(1).hasPastSince(System.nanoTime()))
  }
  
  @Test
  def equality_tests() {
    assertEquals(nanos(10), nanos(10))
    assertEquals(micros(10), nanos(10000))
    assertEquals(nanos(10000), micros(10))
    assertEquals(days(10), new Duration(days(10).toNanos, TimeUnit.NANOSECONDS))
    assertFalse(days(3) == null)
    assertFalse(days(1) == nanos(1))
    assertFalse(days(1) == "a string")
  }
  
  @Test
  def hashCode_equals_contract() {
    assertEquals(micros(10).hashCode, nanos(10000).hashCode)
  }
  
  @Test
  def test_with_legal_duration_strings() {
    assertEquals(10, valueOf("10ns").toNanos)
    assertEquals(10, valueOf("10us").toMicros)
    assertEquals(10, valueOf("10ms").toMillis)
    assertEquals(10, valueOf("10s").toSeconds)
    assertEquals(10, valueOf("10m").toMinutes)
    assertEquals(10, valueOf("10h").toHours)
    assertEquals(10, valueOf("10d").toDays)
    
    assertEquals(10, valueOf("10 ns").toNanos)
    assertEquals(10, valueOf("10nsec").toNanos)
  }
  
  @Test(expected=classOf[IllegalArgumentException])
  def missing_unit_in_string() {
    valueOf("10")
  }
  
  @Test(expected=classOf[IllegalArgumentException])
  def illegal_string_for_duration() {
    valueOf("I am not a duration")
  }
  
  @Test
  def test_has_unit() {
    assertTrue(days(1).hasDays)
    assertFalse(hours(1).hasDays)
    
    assertTrue(hours(1).hasHours)
    assertFalse(minutes(1).hasHours)
    
    assertTrue(minutes(1).hasMinutes)
    assertFalse(seconds(1).hasMinutes)
    
    assertTrue(seconds(1).hasSeconds)
    assertFalse(millis(1).hasSeconds)
    
    assertTrue(millis(1).hasMillis)
    assertFalse(micros(1).hasMillis)
    
    assertTrue(micros(1).hasMicros)
    assertFalse(nanos(1).hasMicros)
    
    assertTrue(nanos(1).hasNanos)
  }
  
  @Test
  def test_toString() {
    assertEquals("10ns", nanos(10).toString)
    assertEquals("10us", micros(10).toString)
    assertEquals("10ms", millis(10).toString)
    assertEquals("10s",  seconds(10).toString)
    assertEquals("10m",  minutes(10).toString)
    assertEquals("10h",  hours(10).toString)
    assertEquals("10d",  days(10).toString)
  }
}
