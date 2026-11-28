package com.chat.server.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class SmtpPasswordResetEmailSender implements PasswordResetEmailSender {
  private final JavaMailSender mailSender;
  private final String senderAddress;

  public SmtpPasswordResetEmailSender(
      JavaMailSender mailSender, @Value("${spring.mail.username:}") String senderAddress) {
    this.mailSender = mailSender;
    this.senderAddress = senderAddress;
  }

  @Override
  public void sendTemporaryPassword(String recipient, String temporaryPassword) {
    if (senderAddress.isBlank()) {
      throw new IllegalStateException("SMTP username must be configured in server.properties");
    }

    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(senderAddress);
    message.setTo(recipient);
    message.setSubject("Your Chat System temporary password");
    message.setText(
        "Your Chat System password was reset. Your temporary password is:\n\n"
            + temporaryPassword
            + "\n\nUse it to sign in to your account.");
    mailSender.send(message);
  }
}
