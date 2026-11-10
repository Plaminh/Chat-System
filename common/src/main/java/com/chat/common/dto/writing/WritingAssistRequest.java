package com.chat.common.dto.writing;

import static com.chat.common.protocol.ContractChecks.required;

import com.chat.common.enums.WritingAssistMode;

public record WritingAssistRequest(WritingAssistMode mode, String draft, String instruction) {
  public WritingAssistRequest {
    mode = required(mode, "mode");
    if (mode == WritingAssistMode.REVISE && (draft == null || draft.isBlank())) {
      throw new IllegalArgumentException("draft is required for revision");
    }
  }
}
