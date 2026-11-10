package com.chat.common.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.StringReader;
import org.junit.jupiter.api.Test;

class JsonLineReaderTest {
  @Test
  void readsLfAndCrLfWithoutTerminators() throws Exception {
    assertEquals("one", JsonLineReader.readLine(new StringReader("one\ntwo"), 10));
    assertEquals("one", JsonLineReader.readLine(new StringReader("one\r\ntwo"), 10));
  }

  @Test
  void handlesImmediateAndUnterminatedEndOfStream() throws Exception {
    assertNull(JsonLineReader.readLine(new StringReader(""), 10));
    assertEquals("last", JsonLineReader.readLine(new StringReader("last"), 10));
  }

  @Test
  void acceptsExactLimitAndRejectsTheNextCharacter() throws Exception {
    assertEquals("abcd", JsonLineReader.readLine(new StringReader("abcd\n"), 4));
    assertThrows(
        MessageTooLargeException.class,
        () -> JsonLineReader.readLine(new StringReader("abcde\n"), 4));
  }

  @Test
  void supportsAnEmptyProtocolLine() throws Exception {
    assertEquals("", JsonLineReader.readLine(new StringReader("\n"), 4));
  }
}
