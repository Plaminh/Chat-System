package com.chat.common.protocol;

import com.chat.common.dto.RequestEnvelope;
import com.chat.common.enums.ActionType;
import com.chat.common.enums.ErrorCode;
import com.chat.common.exception.ProtocolValidationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class ProtocolValidator {
  private ProtocolValidator() {}

  public static RequestEnvelope parseRequest(ObjectMapper mapper, String jsonLine)
      throws ProtocolValidationException {
    JsonNode tree;
    try {
      tree = mapper.readTree(jsonLine);
    } catch (JsonProcessingException | IllegalArgumentException exception) {
      throw error(ErrorCode.INVALID_JSON, null, "Malformed JSON protocol message");
    }
    if (tree == null || !tree.isObject()) {
      throw error(ErrorCode.INVALID_REQUEST, null, "Request must be a JSON object");
    }

    String requestId = readRequestId(tree);
    ActionType action = readAction(tree, requestId);
    JsonNode data = tree.get("data");
    if (data == null || !data.isObject()) {
      throw error(ErrorCode.INVALID_PAYLOAD, requestId, "data must be a JSON object");
    }

    return new RequestEnvelope(requestId, action, data);
  }

  private static String readRequestId(JsonNode tree) throws ProtocolValidationException {
    JsonNode value = tree.get("requestId");
    if (value == null || !value.isTextual() || value.asText().isBlank()) {
      throw error(ErrorCode.INVALID_REQUEST, null, "requestId is required");
    }
    return value.asText();
  }

  private static ActionType readAction(JsonNode tree, String requestId)
      throws ProtocolValidationException {
    JsonNode value = tree.get("action");
    if (value == null || !value.isTextual() || value.asText().isBlank()) {
      throw error(ErrorCode.INVALID_REQUEST, requestId, "action is required");
    }
    try {
      return ActionType.valueOf(value.asText());
    } catch (IllegalArgumentException exception) {
      throw error(ErrorCode.UNKNOWN_ACTION, requestId, "Unknown protocol action");
    }
  }

  private static ProtocolValidationException error(
      ErrorCode code, String requestId, String message) {
    return new ProtocolValidationException(code, requestId, message);
  }
}
