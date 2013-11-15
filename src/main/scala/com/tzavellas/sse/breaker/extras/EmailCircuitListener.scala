/* ----------------- sse-breaker ----------------- *\
 * Licensed under the Apache License, Version 2.0. *
 * Author: Spiros Tzavellas                        *
\* ----------------------------------------------- */
package com.tzavellas.sse.breaker.extras

import java.util.Properties
import javax.mail.internet.{ InternetAddress, MimeMessage }
import javax.mail.{ Message, Session, Transport }
import com.tzavellas.sse.breaker.CircuitBreaker
import com.tzavellas.sse.breaker.CircuitStateListener

class EmailCircuitListener(addresses: EmailConfig, config: SMTPConnectionConfig)
  extends CircuitStateListener {

  def onOpen(breaker: CircuitBreaker, error: Throwable) {
    sendEmail(
      subject = "Open circuit for " + breaker.name,
      body = "The system had lots of errors so it will stop processing.\n\n" +
    		 "Last error was: " + error.getStackTraceString)
  }

  def onClose(breaker: CircuitBreaker) {
    sendEmail(
      subject = "Closed circuit for '" + breaker.name + "'",
      body = "The system is back to normal")
  }


  private def sendEmail(subject: String, body: String) {
    val session = Session.getInstance(new Properties)
    
    val message = new MimeMessage(session)
    message.setFrom(new InternetAddress(addresses.from))
    message.setRecipients(Message.RecipientType.TO, addresses.to)
    message.setSubject(subject)
    message.setText(body)
 
    val transport = session.getTransport("smtp")
    transport.connect(config.host, config.port, config.username, config.password);
    Transport.send(message);
  }
}

case class EmailConfig(from: String, to: String)

case class SMTPConnectionConfig(
  host: String,
  port: Int,
  username: String,
  password: String
)