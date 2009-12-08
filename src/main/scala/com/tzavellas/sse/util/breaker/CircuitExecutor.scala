package com.tzavellas.sse.util.breaker

/**
 * An executor that implements the Circuit Breaker stability design pattern.
 * 
 * <p>The purpose of the a circuit-breaker is to keep track of the error rates of dangerous
 * operations (such as calls to an integration point) and prevent the execution of those
 * operations for a configurable amount of time when the error rates are high. A circuit-breaker
 * has three states: <i>closed</i>, <i>open</i> and <i>half-open</i>.
 * 
 * <p>During normal operation the circuit-breaker is <i>closed</i> and the executor executes
 * the specified operation, recording the number of failures (exceptions thrown) that happen as a
 * result of those executions. When the number of failures exceeds a configured number then the
 * circuit-breaker moves to the <i>open</i> state.</p> 
 * 
 * <p>In the <i>open</i> state, since the probability that failures will happen is high, the
 * executor when requested to execute an operation <i>fails fast</i> by throwing an
 * {@code OpenCircuitException}.</p>
 * 
 * <p>After a configurable amount of time the circuit-breaker goes to the <i>half-open</i> state. In
 * that state when a request to execute an operation is made, the executor executes the operation and if
 * it succeeds the circuit-breaker moves to the <i>closed</i> state, else it moves to the
 * <i>open</i> state.</p>
 * 
 * <p>Instances of this class are thread-safe.</p>
 * 
 * @param configuration The configuration of the circuit-breaker.
 * 
 * @see OpenCircuitException
 * @see CircuitBreaker
 * @see CircuitConfiguration
 * 
 * @author spiros
 */
class CircuitExecutor(configuration: CircuitConfiguration = new CircuitConfiguration) {
  
  private val ignoredExceptions = new ClassFilter
  
  val breaker = new CircuitBreaker(configuration)
  
  /**
   * The duration after which a method execution is considered a failure.
   * 
   * <p>The default value is 10 minutes.</p>
   */
  @volatile
  var maxMethodDuration = Duration.minutes(10)
  
  /**
   * When an exception of the specified type gets thrown as a result of an
   * operation execution not increment the failure counter.
   * 
   * <p>Please note that subclasses of the specified exception will also
   * be ignored.</p>
   * 
   * @param ignored the exception to ignore
   */
  def ignoreException[T <: Throwable](exception: Class[T]) {
    ignoredExceptions += exception
  }
  
  /**
   * Stop ignoring exceptions of the specified type when recording failures.
   * 
   * @param exception the exception to stop ignoring
   */
  def removeIgnoredExcpetion[T <: Throwable](exception: Class[T]) {
    ignoredExceptions -= exception
  }
  
  /**
   * Executes the specified operation depending on the state of the circuit-breaker.
   * 
   * @param operation the operation to execute.
   * @return the result of the operation execution
   * @throws OpenCircuitException if the circuit-breaker is open.
   */
  def apply[T](operation: => T): T = {
    try {
      assertTheCircuitIsClosed()
      val result = ExecutionTimer.time(operation)
      recordAsFailureIfItWasSlow(result.duration)
      closeTheCircuitIfItIsHalfOpen()
      result.value
    } catch {
      case e =>
        recordIfNotIgnored(e)
        throw e
    }
  }
  
  private def assertTheCircuitIsClosed() {
    if (breaker.isOpen) throw new OpenCircuitException(breaker)
  }
  private def recordAsFailureIfItWasSlow(duration: Long) {
    if (duration >= maxMethodDuration.toNanos)
      breaker.recordFailure()
  }
  private def closeTheCircuitIfItIsHalfOpen() {
    if (breaker.isHalfOpen) breaker.close()
  }
  private def recordIfNotIgnored(e: Throwable) {
    if (! ignoredExceptions.contains(e.getClass))
      breaker.recordFailure()
  }
}

private object ExecutionTimer {
  
  def time[T](operation: => T) = {
    val start = System.nanoTime()
    new { 
      val value = operation
      val duration = System.nanoTime() - start
    }
  }
}
