package com.chat.common.dto;

import com.chat.common.enums.ActionType;
import com.fasterxml.jackson.databind.JsonNode;

public record RequestEnvelope(String requestId, ActionType action, JsonNode data) { }
