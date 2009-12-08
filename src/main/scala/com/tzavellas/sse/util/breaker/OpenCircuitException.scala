package com.tzavellas.sse.util.breaker

import scala.util.control.NoStackTrace

class OpenCircuitException(val circuit: CircuitBreaker) extends RuntimeException with NoStackTrace
