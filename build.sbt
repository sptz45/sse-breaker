
name := "sse-breaker"
description := "An implementation of the Circuit Breaker stability design pattern in Scala"
startYear := Some(2009)

crossScalaVersions := Seq("2.11.8", "2.12.2")
releaseCrossBuild := true

libraryDependencies ++= Seq(
  "org.slf4j"    % "slf4j-api"       % "1.6.4" % Optional,
  "javax.mail"   % "mail"            % "1.4.4" % Optional,
  "junit"        % "junit"           % "4.11" % Test,
  "com.novocode" % "junit-interface" % "0.11" % Test
)

//testOptions += Tests.Argument(TestFrameworks.JUnit, "-q")
