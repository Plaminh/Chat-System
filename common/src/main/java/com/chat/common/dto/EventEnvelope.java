package com.chat.common.dto;

import com.chat.common.enums.MessageType;
import com.fasterxml.jackson.databind.JsonNode;

public record EventEnvelope(String eventId, MessageType type, JsonNode data) {
  public EventEnvelope {
    if (eventId == null || eventId.isBlank()) {
      throw new IllegalArgumentException("eventId must not be blank");
    }
    if (type == null) {
      throw new IllegalArgumentException("type is required");
    }
    if (data == null || !data.isObject()) {
      throw new IllegalArgumentException("data must be a JSON object");
    }
  }
}
