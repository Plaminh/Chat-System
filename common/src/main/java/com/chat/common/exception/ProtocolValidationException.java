package com.chat.common.exception;

import com.chat.common.enums.ErrorCode;

public final class ProtocolValidationException extends Exception {
  private final ErrorCode errorCode;
  private final String requestId;

  public ProtocolValidationException(ErrorCode errorCode, String requestId, String message) {
    super(message);
    this.errorCode = errorCode;
    this.requestId = requestId;
  }

  public ErrorCode errorCode() {
    return errorCode;
  }

  public String requestId() {
    return requestId;
  }
}
