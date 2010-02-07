package com.tzavellas.sse.util.breaker

import org.junit.Test
import org.junit.Assert._

class ClassFilterTest {

  val classes = new ClassFilter
  
  @Test
  def normal_operation() {
    assertTrue(classes.isEmpty)
    
    classes += classOf[RuntimeException]
    assertEquals(1, classes.size)
    assertFalse(classes.isEmpty)
    
    classes -= classOf[RuntimeException]
    assertTrue(classes.isEmpty)
  }
  
  @Test
  def adding_null_does_nothing() {
    classes += null
    assertTrue(classes.isEmpty)
    classes ++= null
    assertTrue(classes.isEmpty)
  }
  
  @Test
  def operation_while_empty() {
    assertTrue(classes.isEmpty)
    assertEquals(0, classes.size)
    assertFalse(classes.contains(classOf[Exception]))
  }
  
  @Test
  def simple_contains_test() {
    classes += classOf[ArithmeticException]
    assertFalse(classes.contains(classOf[RuntimeException]))
    assertTrue(classes.contains(classOf[ArithmeticException]))
    assertFalse(classes.contains(classOf[IllegalStateException]))
  }
  
  @Test
  def contains_also_checks_for_subclasses() {
    classes += classOf[RuntimeException]
    assertTrue(classes.contains(classOf[RuntimeException]))
    assertTrue(classes.contains(classOf[ArithmeticException]))
  }
  
  @Test
  def adding_a_subclass_does_nothing() {
    classes += classOf[RuntimeException]
    assertEquals(1, classes.size)
    
    classes += classOf[ArithmeticException]
    assertEquals(1, classes.size)
  }
  
  @Test
  def adding_a_superclass_removes_subclasses() {
    classes += classOf[ArithmeticException]
    assertEquals(1, classes.size)
    
    classes += classOf[RuntimeException]
    assertEquals(1, classes.size)
  }
}
