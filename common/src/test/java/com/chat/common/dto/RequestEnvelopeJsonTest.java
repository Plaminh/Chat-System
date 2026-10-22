package com.chat.common.dto;

import com.chat.common.enums.ActionType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RequestEnvelopeJsonTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test void serializesRequest() throws Exception {
        String json = mapper.writeValueAsString(new RequestEnvelope("req-1", ActionType.PING, mapper.createObjectNode()));
        assertTrue(json.contains("\"action\":\"PING\""));
    }

    @Test void deserializesRequest() throws Exception {
        RequestEnvelope request = mapper.readValue("{\"requestId\":\"req-1\",\"action\":\"PING\",\"data\":{}}", RequestEnvelope.class);
        assertEquals("req-1", request.requestId());
        assertEquals(ActionType.PING, request.action());
    }
}
