package com.chat.common.dto;

import com.chat.common.enums.ActionType;
import com.fasterxml.jackson.databind.JsonNode;

public record RequestEnvelope(String requestId, ActionType action, JsonNode data) {
  public RequestEnvelope {
    if (requestId == null || requestId.isBlank()) {
      throw new IllegalArgumentException("requestId must not be blank");
    }
    if (action == null) {
      throw new IllegalArgumentException("action is required");
    }
    if (data == null || !data.isObject()) {
      throw new IllegalArgumentException("data must be a JSON object");
    }
  }
}
