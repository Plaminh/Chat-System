package com.chat.common.protocol;

import java.io.IOException;
import java.io.Reader;

public final class JsonLineReader {
  private JsonLineReader() {}

  public static String readLine(Reader reader, int maximumCharacters) throws IOException {
    StringBuilder line = new StringBuilder();
    int character;
    while ((character = reader.read()) != -1) {
      if (character == '\n') return line.toString();
      if (character != '\r') {
        if (line.length() >= maximumCharacters)
          throw new MessageTooLargeException(maximumCharacters);
        line.append((char) character);
      }
    }
    return line.isEmpty() ? null : line.toString();
  }
}
