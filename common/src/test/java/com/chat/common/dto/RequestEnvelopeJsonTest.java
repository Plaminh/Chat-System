package com.chat.common.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.chat.common.enums.ActionType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class RequestEnvelopeJsonTest {
  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  void roundTripsPingRequest() throws Exception {
    RequestEnvelope request =
        new RequestEnvelope("req-1", ActionType.PING, mapper.createObjectNode());
    RequestEnvelope decoded =
        mapper.readValue(mapper.writeValueAsString(request), RequestEnvelope.class);

    assertEquals(request, decoded);
    assertTrue(decoded.data().isObject());
  }

  @Test
  void rejectsMissingFieldsAndNonObjectPayloads() {
    assertThrows(
        JsonMappingException.class,
        () -> mapper.readValue("{\"action\":\"PING\",\"data\":{}}", RequestEnvelope.class));
    assertThrows(
        JsonMappingException.class,
        () ->
            mapper.readValue(
                "{\"requestId\":\"req-1\",\"action\":\"PING\",\"data\":[]}",
                RequestEnvelope.class));
  }
}
