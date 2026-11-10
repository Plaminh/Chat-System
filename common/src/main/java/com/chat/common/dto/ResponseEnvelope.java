package com.chat.common.dto;

import com.chat.common.enums.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;

public record ResponseEnvelope(
    String requestId, boolean success, JsonNode data, ErrorCode errorCode, String message) {
  public ResponseEnvelope {
    if (requestId != null && requestId.isBlank()) {
      throw new IllegalArgumentException("requestId must not be blank");
    }
    if (success && requestId == null) {
      throw new IllegalArgumentException("successful responses require a requestId");
    }
    if (success && (errorCode != null || message != null)) {
      throw new IllegalArgumentException("successful responses must not contain errors");
    }
    if (!success && (errorCode == null || message == null || message.isBlank())) {
      throw new IllegalArgumentException("failed responses require an error code and message");
    }
    if (!success && data != null && !data.isNull()) {
      throw new IllegalArgumentException("failed responses must not contain data");
    }
  }
}
