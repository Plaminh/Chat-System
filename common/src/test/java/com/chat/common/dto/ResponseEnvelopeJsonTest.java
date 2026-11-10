package com.chat.common.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.chat.common.enums.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class ResponseEnvelopeJsonTest {
  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  void roundTripsSuccess() throws Exception {
    ResponseEnvelope response =
        new ResponseEnvelope(
            "req-1", true, mapper.createObjectNode().put("message", "PONG"), null, null);
    ResponseEnvelope decoded =
        mapper.readValue(mapper.writeValueAsString(response), ResponseEnvelope.class);

    assertTrue(decoded.success());
    assertEquals("PONG", decoded.data().get("message").asText());
  }

  @Test
  void roundTripsFailureWithNullData() throws Exception {
    ResponseEnvelope response =
        new ResponseEnvelope("req-1", false, null, ErrorCode.INVALID_REQUEST, "Invalid request");
    ResponseEnvelope decoded =
        mapper.readValue(mapper.writeValueAsString(response), ResponseEnvelope.class);

    assertFalse(decoded.success());
    assertTrue(decoded.data().isNull());
    assertEquals(ErrorCode.INVALID_REQUEST, decoded.errorCode());
  }

  @Test
  void rejectsContradictoryStates() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new ResponseEnvelope(
                "req-1", true, null, ErrorCode.INTERNAL_ERROR, "must not be present"));
    assertThrows(
        IllegalArgumentException.class,
        () -> new ResponseEnvelope("req-1", false, null, null, "missing code"));
  }
}
