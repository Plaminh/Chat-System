package com.chat.common.dto.event;

import static com.chat.common.protocol.ContractChecks.required;

import com.chat.common.dto.chat.MessageInfo;

public record MessageEvent(MessageInfo message) {
  public MessageEvent {
    message = required(message, "message");
  }
}
