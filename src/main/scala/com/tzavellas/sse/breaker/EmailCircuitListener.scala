/* ----------------- sse-breaker ----------------- *\
 * Licensed under the Apache License, Version 2.0. *
 * Author: Spiros Tzavellas                        *
\* ----------------------------------------------- */
package com.tzavellas.sse.breaker

import java.util.Properties
import javax.mail.internet.{ InternetAddress, MimeMessage }
import javax.mail.{ Message, Session, Transport }

class EmailCircuitListener(address: EmailAddress, config: SMTPConnectionConfig)
  extends CircuitStateListener {

  def onOpen(breaker: CircuitBreaker, error: Exception) {
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
    message.setFrom(new InternetAddress(address.from))
    message.setRecipients(Message.RecipientType.TO, address.to)
    message.setSubject(subject)
    message.setText(body)
 
    val transport = session.getTransport("smtp")
    transport.connect(config.host, config.port, config.username, config.password);
    Transport.send(message);
  }
}

case class EmailAddress(from: String, to: String)

case class SMTPConnectionConfig(
  host: String,
  port: Int,
  username: String,
  password: String
)