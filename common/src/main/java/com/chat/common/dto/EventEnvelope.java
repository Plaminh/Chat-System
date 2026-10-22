package com.chat.common.dto;

import com.chat.common.enums.MessageType;
import com.fasterxml.jackson.databind.JsonNode;

public record EventEnvelope(String eventId, MessageType type, JsonNode data) { }
