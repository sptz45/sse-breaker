/* ----------------- sse-breaker ----------------- *\
 * Licensed under the Apache License, Version 2.0. *
 * Author: Spiros Tzavellas                        *
\* ----------------------------------------------- */
package com.tzavellas.sse.breaker

class FailureDefinition extends (Throwable => Boolean) {
  
  private val ignoredExceptions = new ClassFilter

  def apply(exception: Throwable) = ! ignoredExceptions.contains(exception.getClass)
  
  /**
   * When an exception of the specified type gets thrown as a result of an
   * operation execution not increment the failure counter.
   * 
   * Please note that subclasses of the specified exception will also
   * be ignored.
   * 
   * @param ignored the exception to ignore
   */
  def ignoreException[T <: Exception](exception: Class[T]) {
    ignoredExceptions += exception
  }
  
  /**
   * Stop ignoring exceptions of the specified type when recording failures.
   * 
   * @param exception the exception to stop ignoring
   */
  def stopIgnoringException[T <: Exception](exception: Class[T]) {
    ignoredExceptions -= exception
  }
}