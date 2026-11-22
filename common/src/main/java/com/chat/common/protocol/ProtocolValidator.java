package com.chat.common.protocol;

import com.chat.common.dto.EventEnvelope;
import com.chat.common.dto.RequestEnvelope;
import com.chat.common.enums.ActionType;
import com.chat.common.enums.ErrorCode;
import com.chat.common.enums.MessageType;
import com.chat.common.exception.ProtocolValidationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Objects;

public final class ProtocolValidator {
  private ProtocolValidator() {}

  public static RequestEnvelope parseRequest(ObjectMapper mapper, String jsonLine)
      throws ProtocolValidationException {
    JsonNode tree = readObject(mapper, jsonLine, "Request");
    String requestId = readRequiredText(tree, "requestId", null);
    ActionType action = readAction(tree, requestId);
    JsonNode data = readObjectPayload(tree, requestId);
    return new RequestEnvelope(requestId, action, data);
  }

  public static EventEnvelope parseEvent(ObjectMapper mapper, String jsonLine)
      throws ProtocolValidationException {
    JsonNode tree = readObject(mapper, jsonLine, "Event");
    String eventId = readRequiredText(tree, "eventId", null);
    MessageType type = readEventType(tree);
    JsonNode data = readObjectPayload(tree, null);
    return new EventEnvelope(eventId, type, data);
  }

  public static <T> T decodePayload(
      ObjectMapper mapper, RequestEnvelope request, Class<T> payloadType)
      throws ProtocolValidationException {
    Objects.requireNonNull(mapper, "mapper");
    Objects.requireNonNull(request, "request");
    Objects.requireNonNull(payloadType, "payloadType");
    try {
      return mapper.treeToValue(request.data(), payloadType);
    } catch (JsonProcessingException exception) {
      ErrorCode code =
          causedByContractViolation(exception)
              ? ErrorCode.VALIDATION_FAILED
              : ErrorCode.INVALID_PAYLOAD;
      String message =
          code == ErrorCode.VALIDATION_FAILED
              ? "Payload fields failed contract validation"
              : "Payload does not match the required JSON contract";
      throw error(code, request.requestId(), message);
    } catch (IllegalArgumentException exception) {
      throw error(
          ErrorCode.VALIDATION_FAILED,
          request.requestId(),
          "Payload fields failed contract validation");
    }
  }

  private static JsonNode readObject(ObjectMapper mapper, String jsonLine, String envelopeName)
      throws ProtocolValidationException {
    Objects.requireNonNull(mapper, "mapper");
    JsonNode tree;
    try {
      tree = mapper.readTree(jsonLine);
    } catch (JsonProcessingException | IllegalArgumentException exception) {
      throw error(ErrorCode.INVALID_JSON, null, "Malformed JSON protocol message");
    }
    if (tree == null || !tree.isObject()) {
      throw error(ErrorCode.INVALID_REQUEST, null, envelopeName + " must be a JSON object");
    }
    return tree;
  }

  private static String readRequiredText(JsonNode tree, String field, String requestId)
      throws ProtocolValidationException {
    JsonNode value = tree.get(field);
    if (value == null || !value.isTextual() || value.asText().isBlank()) {
      throw error(ErrorCode.INVALID_REQUEST, requestId, field + " is required");
    }
    return value.asText();
  }

  private static ActionType readAction(JsonNode tree, String requestId)
      throws ProtocolValidationException {
    String identifier = readRequiredText(tree, "action", requestId);
    try {
      return ActionType.valueOf(identifier);
    } catch (IllegalArgumentException exception) {
      throw error(ErrorCode.UNKNOWN_ACTION, requestId, "Unknown protocol action");
    }
  }

  private static MessageType readEventType(JsonNode tree) throws ProtocolValidationException {
    String identifier = readRequiredText(tree, "type", null);
    try {
      return MessageType.valueOf(identifier);
    } catch (IllegalArgumentException exception) {
      throw error(ErrorCode.INVALID_REQUEST, null, "Unknown protocol event type");
    }
  }

  private static JsonNode readObjectPayload(JsonNode tree, String requestId)
      throws ProtocolValidationException {
    JsonNode data = tree.get("data");
    if (data == null || !data.isObject()) {
      throw error(ErrorCode.INVALID_PAYLOAD, requestId, "data must be a JSON object");
    }
    return data;
  }

  private static boolean causedByContractViolation(Throwable exception) {
    Throwable cause = exception;
    while (cause != null) {
      if (cause instanceof IllegalArgumentException || cause instanceof NullPointerException) {
        return true;
      }
      cause = cause.getCause();
    }
    return false;
  }

  private static ProtocolValidationException error(
      ErrorCode code, String requestId, String message) {
    return new ProtocolValidationException(code, requestId, message);
  }
}
