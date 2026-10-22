package com.chat.server.handler;

import com.chat.common.dto.RequestEnvelope;
import com.chat.common.dto.ResponseEnvelope;
import com.chat.common.enums.ActionType;
import com.chat.common.enums.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class RequestHandler {
    private final ObjectMapper mapper;
    public RequestHandler(ObjectMapper mapper) { this.mapper = mapper; }

    public String handle(String jsonLine) {
        String requestId = null;
        try {
            JsonNode tree = mapper.readTree(jsonLine);
            if (tree == null || !tree.isObject())
                return write(error(null, ErrorCode.INVALID_REQUEST, "Request must be a JSON object"));
            if (tree != null && tree.hasNonNull("requestId")) requestId = tree.get("requestId").asText();
            RequestEnvelope request = mapper.treeToValue(tree, RequestEnvelope.class);
            if (request.requestId() == null || request.requestId().isBlank() || request.action() == null)
                return write(error(requestId, ErrorCode.INVALID_REQUEST, "requestId and action are required"));
            if (request.action() == ActionType.PING) {
                JsonNode data = mapper.createObjectNode().put("message", "PONG");
                return write(new ResponseEnvelope(request.requestId(), true, data, null, null));
            }
            return write(error(request.requestId(), ErrorCode.INVALID_REQUEST, "Unsupported action"));
        } catch (JsonProcessingException | IllegalArgumentException exception) {
            return write(error(requestId, ErrorCode.INVALID_JSON, "Malformed JSON protocol message"));
        }
    }

    public String messageTooLarge() { return write(error(null, ErrorCode.MESSAGE_TOO_LARGE, "Protocol message is too large")); }
    private ResponseEnvelope error(String id, ErrorCode code, String message) { return new ResponseEnvelope(id, false, null, code, message); }
    private String write(ResponseEnvelope response) {
        try { return mapper.writeValueAsString(response); }
        catch (JsonProcessingException exception) { throw new IllegalStateException("Could not serialize protocol response", exception); }
    }
}
