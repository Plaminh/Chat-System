package com.chat.server.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.chat.common.dto.ResponseEnvelope;
import com.chat.common.enums.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class RequestHandlerTest {
  private final ObjectMapper mapper = new ObjectMapper();
  private final RequestHandler handler = new RequestHandler(mapper);

  @Test
  void pingReturnsPong() throws Exception {
    ResponseEnvelope response =
        read(handler.handle("{\"requestId\":\"r1\",\"action\":\"PING\",\"data\":{}}"));
    assertTrue(response.success());
    assertEquals("r1", response.requestId());
    assertEquals("PONG", response.data().get("message").asText());
  }

  @Test
  void malformedJsonReturnsProtocolError() throws Exception {
    assertEquals(ErrorCode.INVALID_JSON, read(handler.handle("{not-json")).errorCode());
  }

  @Test
  void nonObjectsReturnInvalidRequest() throws Exception {
    assertEquals(ErrorCode.INVALID_REQUEST, read(handler.handle("null")).errorCode());
    assertEquals(ErrorCode.INVALID_REQUEST, read(handler.handle("[]")).errorCode());
  }

  @Test
  void unknownActionIsDistinctFromKnownUnsupportedAction() throws Exception {
    ResponseEnvelope unknown =
        read(handler.handle("{\"requestId\":\"r1\",\"action\":\"NOPE\",\"data\":{}}"));
    ResponseEnvelope unsupported =
        read(handler.handle("{\"requestId\":\"r2\",\"action\":\"LOGIN\",\"data\":{}}"));

    assertEquals(ErrorCode.UNKNOWN_ACTION, unknown.errorCode());
    assertEquals(ErrorCode.UNSUPPORTED_OPERATION, unsupported.errorCode());
  }

  @Test
  void missingAndInvalidPayloadFieldsArePredictable() throws Exception {
    ResponseEnvelope missing = read(handler.handle("{\"requestId\":\"r1\",\"action\":\"PING\"}"));
    ResponseEnvelope wrongType =
        read(handler.handle("{\"requestId\":\"r2\",\"action\":\"PING\",\"data\":[]}"));

    assertFalse(missing.success());
    assertEquals(ErrorCode.INVALID_PAYLOAD, missing.errorCode());
    assertEquals(ErrorCode.INVALID_PAYLOAD, wrongType.errorCode());
  }

  private ResponseEnvelope read(String json) throws Exception {
    return mapper.readValue(json, ResponseEnvelope.class);
  }
}
