package com.chat.common.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.chat.common.dto.RequestEnvelope;
import com.chat.common.enums.ActionType;
import com.chat.common.enums.ErrorCode;
import com.chat.common.exception.ProtocolValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class ProtocolValidatorTest {
  private final ObjectMapper mapper = ProtocolJson.newObjectMapper();

  @Test
  void parsesAValidRequest() throws Exception {
    RequestEnvelope request =
        ProtocolValidator.parseRequest(
            mapper, "{\"requestId\":\"r1\",\"action\":\"PING\",\"data\":{}}");
    assertEquals("r1", request.requestId());
    assertEquals(ActionType.PING, request.action());
  }

  @Test
  void distinguishesMalformedUnknownAndInvalidPayload() {
    assertCode(ErrorCode.INVALID_JSON, "{not-json");
    assertCode(ErrorCode.UNKNOWN_ACTION, "{\"requestId\":\"r1\",\"action\":\"NOPE\",\"data\":{}}");
    assertCode(ErrorCode.INVALID_PAYLOAD, "{\"requestId\":\"r1\",\"action\":\"PING\",\"data\":[]}");
    assertCode(ErrorCode.INVALID_REQUEST, "{}");
  }

  private void assertCode(ErrorCode expected, String json) {
    ProtocolValidationException exception =
        assertThrows(
            ProtocolValidationException.class, () -> ProtocolValidator.parseRequest(mapper, json));
    assertEquals(expected, exception.errorCode());
  }
}
