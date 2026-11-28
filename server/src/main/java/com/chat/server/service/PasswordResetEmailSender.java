package com.chat.server.service;

public interface PasswordResetEmailSender {
  void sendTemporaryPassword(String recipient, String temporaryPassword);
}
