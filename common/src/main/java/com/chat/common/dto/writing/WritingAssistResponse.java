package com.chat.common.dto.writing;

import static com.chat.common.protocol.ContractChecks.text;

public record WritingAssistResponse(String suggestedText) {
  public WritingAssistResponse {
    suggestedText = text(suggestedText, "suggestedText");
  }
}
