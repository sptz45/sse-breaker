package com.tzavellas.sse.util.breaker

private[breaker] class ClassFilter {
  
  @volatile
  private var classes = List[Class[_]]()

  def +=(c: Class[_]) {
    if (c == null) return
    for (cls <- classes)
      if (c.isAssignableFrom(cls)) {
        this -= cls
      } else if (cls.isAssignableFrom(c)) {
        return;
      }
    classes = c :: classes
  }

  def ++=(classes: Iterable[Class[_]]) {
    if (classes == null) return
    for (c <- classes) this += (c)
  }
  
  def contains(c: Class[_]) = classes.find(_.isAssignableFrom(c)) != None

  def isEmpty = classes.isEmpty
  
  def size = classes.size
  
  def clear() {
    classes = Nil
  }

  def -=(c: Class[_]) {
    classes = classes.filterNot(_ == c)
  }
}

