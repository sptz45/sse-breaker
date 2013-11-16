# sse-breaker

[![Build Status](https://secure.travis-ci.org/sptz45/sse-breaker.png)](http://travis-ci.org/sptz45/sse-breaker)

An implementation of the **Circuit Breaker** stability design pattern in the
*Scala* programming language.

## Description

At any point in time a circuit-breaker can be in any of the following three
states: *closed*, *open* or *half-open*.

During normal operation a circuit-breaker is in the *closed* state and allows
the execution of the requested operations recording the number of failures
that happen as a result of those operations. A failure is either a thrown
exception for which the configured `CircuitConfiguration.isFailure` function
returns `true` or a sucessful operation execution that takes more than the
configured `CircuitConfiguration.maxMethodDuration` duration to complete.

When the number of failures that occur within the configured `CircuitConfiguration.failureCountTimeout`
duration exceeds the `CircuitConfiguration.maxFailures` then the circuit-breaker
moves to the *open* state. In the *open* state, since the probability that
failures will happen is high, the circuit-breaker does not permit the execution
of any requested operation by *failing fast*. This is achieved by throwing an
`OpenCircuitException` or by returning a failed `Future` that contains
`OpenCircuitException` if the operation is asynchronous.

After the configured `CircuitConfiguration.openCircuitTimeout` duration has
passed the circuit-breaker moves from the *open* to the *half-open* state. In
this state when a request to execute an operation is made, the circuit breaker
allows execution of the operation and if it succeeds it moves to the *closed*
state, else it moves back to the *open* state.

## Usage

To use the *Circuit Breaker*, all you need to do is instantiate a
`CircuitExecutor` with the appropriate configuration and call its `apply` method
passing the closure containing the code with the high error rate (usually an
*Integration Point*). If the circuit is *closed* or *half-open* then the
executor will execute the closure else (if it is in the *open* state) the
executor will throw an `OpenCircuitExeption` without executing the closure.

```scala
class StocksService(stocks: StocksGateway) {

  val failFast = new CircuitExecutor(name="stocks-breaker")

  def getQuote(ticker: String): Int = failFast {
    stocks.getQuote(ticker)
  }

}
```

For more information see the scaladoc of `CircuitExecutor`, `CircuitBreaker` and
`CircuitConfiguration`.


## License

Licensed under the Apache License, Version 2.0. See the LICENSE and NOTICE
files for more information.
