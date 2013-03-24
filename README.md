
# sse-breaker

[![Build Status](https://secure.travis-ci.org/sptz45/sse-breaker.png)](http://travis-ci.org/sptz45/sse-breaker)

An implementation of the **Circuit Breaker** stability design pattern in the
*Scala* programming language.


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
