package com.chat.common.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.chat.common.enums.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class ResponseEnvelopeJsonTest {
  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  void roundTripsPingSuccessWithStableCorrelation() throws Exception {
    ResponseEnvelope response =
        new ResponseEnvelope(
            "req-1", true, mapper.createObjectNode().put("message", "PONG"), null, null);
    ResponseEnvelope decoded =
        mapper.readValue(mapper.writeValueAsString(response), ResponseEnvelope.class);

    assertTrue(decoded.success());
    assertEquals("req-1", decoded.requestId());
    assertEquals("PONG", decoded.data().get("message").asText());
    assertNull(decoded.errorCode());
  }

  @Test
  void roundTripsFailureWithDocumentedErrorState() throws Exception {
    ResponseEnvelope response =
        new ResponseEnvelope("req-1", false, null, ErrorCode.INVALID_REQUEST, "Invalid request");
    ResponseEnvelope decoded =
        mapper.readValue(mapper.writeValueAsString(response), ResponseEnvelope.class);

    assertFalse(decoded.success());
    assertEquals("req-1", decoded.requestId());
    assertTrue(decoded.data() == null || decoded.data().isNull());
    assertEquals(ErrorCode.INVALID_REQUEST, decoded.errorCode());
  }

  @Test
  void permitsUncorrelatedFailuresForUnreadableRequests() {
    ResponseEnvelope response =
        new ResponseEnvelope(null, false, null, ErrorCode.INVALID_JSON, "Malformed request");

    assertNull(response.requestId());
  }

  @Test
  void rejectsContradictoryResponseStates() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new ResponseEnvelope(
                "req-1", true, null, ErrorCode.INTERNAL_ERROR, "must not be present"));
    assertThrows(
        IllegalArgumentException.class,
        () -> new ResponseEnvelope("req-1", false, null, null, "missing code"));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new ResponseEnvelope(
                "req-1",
                false,
                mapper.createObjectNode(),
                ErrorCode.INVALID_REQUEST,
                "data is contradictory"));
  }
}
