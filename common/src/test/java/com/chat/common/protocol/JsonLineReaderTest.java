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
  void supportsEmptyAndBelowLimitLines() throws Exception {
    assertEquals("", JsonLineReader.readLine(new StringReader("\n"), 4));
    assertEquals("abc", JsonLineReader.readLine(new StringReader("abc\n"), 4));
  }

  @Test
  void acceptsExactLimitAndRejectsTheNextCharacter() throws Exception {
    String exact = "x".repeat(ProtocolConstants.MAX_MESSAGE_CHARS);
    assertEquals(
        exact,
        JsonLineReader.readLine(
            new StringReader(exact + "\n"), ProtocolConstants.MAX_MESSAGE_CHARS));
    assertThrows(
        MessageTooLargeException.class,
        () ->
            JsonLineReader.readLine(
                new StringReader(exact + "x\n"), ProtocolConstants.MAX_MESSAGE_CHARS));
  }

  @Test
  void supportsRepeatedReadsFromTheSameStream() throws Exception {
    StringReader reader = new StringReader("one\r\ntwo\nlast");

    assertEquals("one", JsonLineReader.readLine(reader, 10));
    assertEquals("two", JsonLineReader.readLine(reader, 10));
    assertEquals("last", JsonLineReader.readLine(reader, 10));
    assertNull(JsonLineReader.readLine(reader, 10));
  }
}
