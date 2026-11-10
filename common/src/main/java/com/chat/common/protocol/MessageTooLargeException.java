package com.chat.common.protocol;

import java.io.IOException;

public final class MessageTooLargeException extends IOException {
  public MessageTooLargeException(int limit) {
    super("Message exceeds the " + limit + " character limit");
  }
}
