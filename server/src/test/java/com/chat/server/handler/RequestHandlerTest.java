package com.chat.server.handler;

import com.chat.common.dto.ResponseEnvelope;
import com.chat.common.enums.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RequestHandlerTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final RequestHandler handler = new RequestHandler(mapper);

    @Test void pingReturnsPong() throws Exception {
        ResponseEnvelope response = mapper.readValue(handler.handle("{\"requestId\":\"r1\",\"action\":\"PING\",\"data\":{}}"), ResponseEnvelope.class);
        assertTrue(response.success()); assertEquals("r1", response.requestId()); assertEquals("PONG", response.data().get("message").asText());
    }
    @Test void malformedJsonReturnsProtocolError() throws Exception {
        ResponseEnvelope response = mapper.readValue(handler.handle("{not-json"), ResponseEnvelope.class);
        assertFalse(response.success()); assertEquals(ErrorCode.INVALID_JSON, response.errorCode());
    }
    @Test void jsonNullReturnsStructuredProtocolError() throws Exception {
        ResponseEnvelope response = mapper.readValue(handler.handle("null"), ResponseEnvelope.class);
        assertFalse(response.success()); assertEquals(ErrorCode.INVALID_REQUEST, response.errorCode());
        assertEquals("Request must be a JSON object", response.message());
    }
    @Test void nonObjectJsonReturnsStructuredProtocolError() throws Exception {
        ResponseEnvelope response = mapper.readValue(handler.handle("[]"), ResponseEnvelope.class);
        assertFalse(response.success()); assertEquals(ErrorCode.INVALID_REQUEST, response.errorCode());
        assertEquals("Request must be a JSON object", response.message());
    }
}
