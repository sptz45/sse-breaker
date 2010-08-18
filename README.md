
# sse-breaker

An implementation of the *Circuit Breaker* stability design pattern in the
Scala programming language.


## Usage

To use all you need to do is instantiate a `CircuitExecutor` with the
appropriate configuration and call its `apply` method passing the closure
containing the code with the high error rate (usually an *Integration Point*).

	class StocksService(stocks: StocksGateway) {
	  
	  val guard = new CircuitExecutor(name="stocks-breaker")
	
	  def getQuote(ticker: String): Int = guard {
	    stocks.getQuote(ticker)
	  }
	}


## License

Licensed under the Apache License, Version 2.0. See the LICENSE and NOTICE
files for more information.