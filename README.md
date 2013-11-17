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
returns `true` or a successful operation execution that takes more than the
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

To execute operations using a circuit-breaker you need to construct a
`CircuitExecutor` and pass it the pieces of code you want to execute. This
executor has an associated `CircuitBreaker` object which holds the state of the
circuit-breaker and is consulted to decide whether to execute the requested
operations or to *fail-fast*.

To construct a `CircuitExecutor` you can use the following code:

```scala

import com.tzavellas.sse.breaker.{CircuitExecutor, CircuitConfiguration}
import scala.concurrent.duration._

val failFast = new CircuitExecutor(
    name="tweets-breaker",
    CircuitConfiguration(
      maxFailures = 5,
      openCircuitTimeout = 30.seconds,
      failureCountTimeout = 1.minute,
      maxMethodDuration =  10.seconds)
  )
```
The above code will construct a `CircuitExecutor` with the name *"tweets-breaker"*
using the specified configuration. The configuration says that if 5 failures
(`maxFailures`) occur within 1 minute (`failureCountTimeout`) then the circuit breaker
will move to the *open* state and move to the *half-open* state 30 seconds later
(`openCircuitTimeout`). Also the maximum amount of time an operation might take without
recording it as failure is 10 seconds (`maxMethodDuration`).

Using the executor with a synchronous operation:

```scala
def getTweets(user: String): Seq[Tweet] = ...

val tweets =
  try failFast { getTweets("sptz45") }
  catch { case e: OpenCircuitException => Seq() }
```
Using the executor with an asynchronous operation (one that returns a `Future`):

```scala
import scala.concurrent.Future

def getTweets(user: String): Future[Seq[Tweet]] = ...

val tweets = failFast(getTweets("sptz45"))
               .recover { case _: OpenCircuitException => Seq() }
```
Using the executor by launching a synchronous operation in an `ExecutionContext` and returning a `Future`:

```scala
import scala.concurrent.{Future, ExecutionContext}

implicit val executionContext = ExecutionContext.fromExecutor(...)

def getTweets(user: String): Seq[Tweet]] = ...

val tweets = failFast.async(getTweets("sptz45"))
               .recover { case _: OpenCircuitException => Seq() }
```

## License

Licensed under the Apache License, Version 2.0. See the LICENSE and NOTICE
files for more information.
