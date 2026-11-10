package com.chat.server.handler;

import com.chat.common.dto.RequestEnvelope;
import com.chat.common.dto.ResponseEnvelope;
import com.chat.common.enums.ActionType;
import com.chat.common.enums.ErrorCode;
import com.chat.common.exception.ProtocolValidationException;
import com.chat.common.protocol.ProtocolValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class RequestHandler {
  private final ObjectMapper mapper;

  public RequestHandler(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  public String handle(String jsonLine) {
    try {
      RequestEnvelope request = ProtocolValidator.parseRequest(mapper, jsonLine);
      if (request.action() == ActionType.PING) {
        JsonNode data = mapper.createObjectNode().put("message", "PONG");
        return write(new ResponseEnvelope(request.requestId(), true, data, null, null));
      }
      return write(
          error(request.requestId(), ErrorCode.UNSUPPORTED_OPERATION, "Unsupported action"));
    } catch (ProtocolValidationException exception) {
      return write(error(exception.requestId(), exception.errorCode(), exception.getMessage()));
    }
  }

  public String messageTooLarge() {
    return write(error(null, ErrorCode.MESSAGE_TOO_LARGE, "Protocol message is too large"));
  }

  private ResponseEnvelope error(String id, ErrorCode code, String message) {
    return new ResponseEnvelope(id, false, null, code, message);
  }

  private String write(ResponseEnvelope response) {
    try {
      return mapper.writeValueAsString(response);
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Could not serialize protocol response", exception);
    }
  }
}
