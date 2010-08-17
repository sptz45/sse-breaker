package com.tzavellas.sse.util.breaker

import java.util.Scanner
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit._

/**
 * A value object that holds a time duration in some {@link TimeUnit}.
 */
@serializable
@SerialVersionUID(515348490883208361L)
sealed class Duration(val duration: Long, val unit: TimeUnit) {
  
  def hasPastSince(nanos: Long) = 
    System.nanoTime - nanos >= toNanos
  
  override def equals(any: Any) = any match {
    case that: Duration => this.toNanos == that.toNanos
    case _              => false
  }
  
  override def hashCode() =
    37 * toNanos.hashCode * NANOSECONDS.hashCode  
  
  override def toString() = {
    val unitString: String = unit match {
      case NANOSECONDS  => "ns"
      case MICROSECONDS => "us"
      case MILLISECONDS => "ms"
      case SECONDS      => "s"
      case MINUTES      => "m"
      case HOURS        => "h"
      case DAYS         => "d" 
    }
    duration + unitString 
  }

  def toNanos   = unit.toNanos(duration)
  def toMicros  = unit.toMicros(duration)
  def toMillis  = unit.toMillis(duration)
  def toSeconds = unit.toSeconds(duration)
  def toMinutes = unit.toMinutes(duration)
  def toHours   = unit.toHours(duration)
  def toDays    = unit.toDays(duration)
  
  def hasNanos   = true
  def hasMicros  = unit.toMicros(duration) > 0
  def hasMillis  = unit.toMillis(duration) > 0
  def hasSeconds = unit.toSeconds(duration) > 0
  def hasMinutes = unit.toMinutes(duration) > 0
  def hasHours   = unit.toHours(duration) > 0
  def hasDays    = unit.toDays(duration) > 0
}

object Duration {
  
  def nanos(duration: Long)   = new Duration(duration, NANOSECONDS)
  def micros(duration: Long)  = new Duration(duration, MICROSECONDS)
  def millis(duration: Long)  = new Duration(duration, MILLISECONDS)
  def seconds(duration: Long) = new Duration(duration, SECONDS)
  def minutes(duration: Long) = new Duration(duration, MINUTES)
  def hours(duration: Long)   = new Duration(duration, HOURS)
  def days(duration: Long)    = new Duration(duration, DAYS)
  
  def valueOf(text: String): Duration = {
    try {
      val s = new Scanner(text)
      s.findInLine("(\\d+)[\\s]*(\\w+)")
      val result = s.`match`()
      val duration = result.group(1).toLong
      val us = result.group(2)
      var unit: TimeUnit = null
      // Note: the order in the below comparisons is important
      // because we have 2 matches for startsWith("m"): "m" and "ms"
      if (us.startsWith("d"))  unit = DAYS
      if (us.startsWith("h"))  unit = HOURS
      if (us.startsWith("m"))  unit = MINUTES
      if (us.startsWith("s"))  unit = SECONDS
      if (us.startsWith("ms")) unit = MILLISECONDS
      if (us.startsWith("us")) unit = MICROSECONDS
      if (us.startsWith("ns")) unit = NANOSECONDS
      require(unit ne null)
      new Duration(duration, unit)
    } catch {
      case e: RuntimeException =>
        throw new IllegalArgumentException("Could not parse ["+text+"] in a Duration object", e)
    }
  }
}
