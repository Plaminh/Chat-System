package com.chat.common.dto;

import com.chat.common.enums.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;

public record ResponseEnvelope(String requestId, boolean success, JsonNode data,
                               ErrorCode errorCode, String message) { }
