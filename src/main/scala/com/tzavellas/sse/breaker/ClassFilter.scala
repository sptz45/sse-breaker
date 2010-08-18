/* ----------------- sse-breaker ----------------- *\
 * Licensed under the Apache License, Version 2.0. *
 * Author: Spiros Tzavellas                        *
\* ----------------------------------------------- */
package com.tzavellas.sse.breaker

private class ClassFilter {
  
  @volatile
  private var classes = List[Class[_]]()

  def +=(c: Class[_]) {
    if (c == null) return
    for (cls <- classes) {
      if (c.isAssignableFrom(cls)) {
        this -= cls
      } else if (cls.isAssignableFrom(c)) {
        return
      }
    }
    classes = c :: classes
  }

  def ++=(classes: Iterable[Class[_]]) {
    if (classes == null) return
    for (c <- classes) this += (c)
  }
  
  def contains(c: Class[_]) = classes.exists(_ isAssignableFrom c)

  def isEmpty = classes.isEmpty
  
  def size = classes.size
  
  def clear() {
    classes = Nil
  }

  def -=(c: Class[_]) {
    classes = classes.filterNot(_ == c)
  }
  
  def toSeq: Seq[Class[_]] = classes
}

